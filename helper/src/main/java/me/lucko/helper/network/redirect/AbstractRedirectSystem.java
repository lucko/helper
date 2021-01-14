/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.helper.network.redirect;

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.Events;
import me.lucko.helper.event.SingleSubscription;
import me.lucko.helper.messaging.InstanceData;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.messaging.conversation.ConversationChannel;
import me.lucko.helper.messaging.conversation.ConversationChannelAgent;
import me.lucko.helper.messaging.conversation.ConversationMessage;
import me.lucko.helper.messaging.conversation.ConversationReply;
import me.lucko.helper.messaging.conversation.ConversationReplyListener;
import me.lucko.helper.profiles.Profile;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.text3.Text;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class AbstractRedirectSystem implements RedirectSystem {
    private final InstanceData instanceData;
    private final PlayerRedirector redirector;

    private final ConversationChannel<RequestMessage, ResponseMessage> channel;
    private final ConversationChannelAgent<RequestMessage, ResponseMessage> agent;

    private final ExpiringMap<UUID, Response> expectedPlayers = ExpiringMap.builder()
            .expiration(5, TimeUnit.SECONDS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();

    private final SingleSubscription<AsyncPlayerPreLoginEvent> loginEventListener;
    private boolean ensureJoinedViaQueue = true;

    private RequestHandler handler = new AllowAllHandler();
    private final List<RedirectParameterProvider> defaultParameters = new CopyOnWriteArrayList<>();

    public AbstractRedirectSystem(Messenger messenger, InstanceData instanceData, PlayerRedirector redirector) {
        this.instanceData = instanceData;
        this.redirector = redirector;

        this.channel = messenger.getConversationChannel("hlp-redirect", RequestMessage.class, ResponseMessage.class);

        this.agent = this.channel.newAgent();
        this.agent.addListener((agent, message) -> {
            if (!this.instanceData.getId().equalsIgnoreCase(message.targetServer)) {
                return ConversationReply.noReply();
            }

            // call the handler
            Promise<Response> response = this.handler.handle(message);

            // process the redirect
            response.thenAcceptAsync(r -> {
                if (!r.isAllowed()) {
                    return;
                }

                // add player to the expected players queue
                this.expectedPlayers.put(message.uuid, r);

                // tell the connect server to move the player
                this.redirector.redirectPlayer(this.instanceData.getId(), Profile.create(message.uuid, message.username));
            });

            // send the response
            return ConversationReply.ofPromise(response.thenApplyAsync(r -> {
                ResponseMessage resp = new ResponseMessage();
                resp.convoId = message.convoId;
                resp.allowed = r.isAllowed();
                resp.reason = r.getReason();
                resp.params = new HashMap<>(r.getParams());
                return resp;
            }));
        });

        this.loginEventListener = Events.subscribe(AsyncPlayerPreLoginEvent.class)
                .filter(e -> this.ensureJoinedViaQueue)
                .handler(e -> {
                    Response response = this.expectedPlayers.remove(e.getUniqueId());
                    if (response == null || !response.isAllowed()) {
                        e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, Text.colorize("&cSorry! The server is unable to process your login at this time. (queue error)"));
                    }
                });
    }

    @Override
    public Promise<ReceivedResponse> redirectPlayer(@Nonnull String serverId, @Nonnull Profile profile, @Nonnull Map<String, String> params) {
        RequestMessage req = new RequestMessage();
        req.convoId = UUID.randomUUID();
        req.targetServer = serverId;
        req.uuid = profile.getUniqueId();
        req.username = profile.getName().orElse(null);
        req.params = new HashMap<>(params);

        // include default parameters
        for (RedirectParameterProvider defaultProvider : this.defaultParameters) {
            for (Map.Entry<String, String> ent : defaultProvider.provide(profile, serverId).entrySet()) {
                req.params.putIfAbsent(ent.getKey(), ent.getValue());
            }
        }

        Promise<ReceivedResponse> promise = Promise.empty();

        // send req and await reply.
        this.channel.sendMessage(req, new ConversationReplyListener<ResponseMessage>() {
            @Nonnull
            @Override
            public RegistrationAction onReply(@Nonnull ResponseMessage reply) {
                promise.supply(reply);
                return RegistrationAction.STOP_LISTENING;
            }

            @Override
            public void onTimeout(@Nonnull List<ResponseMessage> replies) {
                promise.supply(MissingResponse.INSTANCE);
            }
        }, 5, TimeUnit.SECONDS);

        return promise;
    }

    @Override
    public void setHandler(@Nonnull RequestHandler handler) {
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public void addDefaultParameterProvider(@Nonnull RedirectParameterProvider provider) {
        this.defaultParameters.add(provider);
    }

    @Override
    public void setEnsure(boolean ensureJoinedViaQueue) {
        this.ensureJoinedViaQueue = ensureJoinedViaQueue;
    }

    @Override
    public int getExpectedConnectionsCount() {
        return this.expectedPlayers.size();
    }

    @Override
    public void close() {
        this.agent.close();
        this.loginEventListener.close();
    }

    private static final class RequestMessage implements ConversationMessage, Request {
        private UUID convoId;
        private String targetServer;
        private UUID uuid;
        private String username;
        private Map<String, String> params;

        @Nonnull
        @Override
        public UUID getConversationId() {
            return this.convoId;
        }

        @Nonnull
        @Override
        public Profile getProfile() {
            return Profile.create(this.uuid, this.username);
        }

        @Nonnull
        @Override
        public Map<String, String> getParams() {
            return ImmutableMap.copyOf(this.params);
        }
    }

    private static final class ResponseMessage implements ConversationMessage, ReceivedResponse {
        private UUID convoId;
        private boolean allowed;
        private String reason;
        private Map<String, String> params;

        @Nonnull
        @Override
        public UUID getConversationId() {
            return this.convoId;
        }

        @Nonnull
        @Override
        public Status getStatus() {
            return this.allowed ? Status.ALLOWED : Status.DENIED;
        }

        @Nonnull
        @Override
        public Optional<String> getReason() {
            return Optional.ofNullable(this.reason);
        }

        @Nonnull
        @Override
        public Map<String, String> getParams() {
            return ImmutableMap.copyOf(this.params);
        }
    }

    private static final class MissingResponse implements ReceivedResponse {
        private static final MissingResponse INSTANCE = new MissingResponse();

        @Nonnull
        @Override
        public Status getStatus() {
            return Status.NO_REPLY;
        }

        @Nonnull
        @Override
        public Optional<String> getReason() {
            return Optional.empty();
        }

        @Nonnull
        @Override
        public Map<String, String> getParams() {
            return ImmutableMap.of();
        }
    }

    private static final class AllowAllHandler implements RequestHandler {

        @Nonnull
        @Override
        public Promise<Response> handle(@Nonnull Request request) {
            return Promise.completed(Response.allow());
        }
    }

}
