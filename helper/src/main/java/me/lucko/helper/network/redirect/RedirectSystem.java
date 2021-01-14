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

import me.lucko.helper.Services;
import me.lucko.helper.messaging.InstanceData;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.profiles.Profile;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.Terminable;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements a system for controlled redirects between servers.
 */
public interface RedirectSystem extends Terminable {

    /**
     * Creates a new {@link RedirectSystem} instance. These should be shared if possible.
     *
     * @param messenger the messenger
     * @param instanceData the instance data
     * @param redirecter the redirecter
     * @return the new RedirectSystem
     */
    static RedirectSystem create(Messenger messenger, InstanceData instanceData, PlayerRedirector redirecter) {
        return new AbstractRedirectSystem(messenger, instanceData, redirecter);
    }

    /**
     * Creates a new {@link RedirectSystem} instance. These should be shared if possible.
     *
     * @param messenger the messenger
     * @return the new RedirectSystem
     */
    static <M extends Messenger & InstanceData & PlayerRedirector> RedirectSystem create(M messenger) {
        return new AbstractRedirectSystem(messenger, messenger, messenger);
    }

    /**
     * Tries to obtain an instance of RedirectSystem from the services manager, falling
     * back to given supplier if one is not already present.
     *
     * @param ifElse the supplier
     * @return the RedirectSystem instance
     */
    static RedirectSystem obtain(Supplier<RedirectSystem> ifElse) {
        RedirectSystem network = Services.get(RedirectSystem.class).orElse(null);
        if (network == null) {
            network = ifElse.get();
            Services.provide(RedirectSystem.class, network);
        }
        return network;
    }

    /**
     * Makes a request to redirect the given player to the given server.
     *
     * @param serverId the server to redirect to
     * @param profile the player to be redirected
     * @param params the parameters for the request
     * @return a promise for the redirect response
     */
    Promise<ReceivedResponse> redirectPlayer(@Nonnull String serverId, @Nonnull Profile profile, @Nonnull Map<String, String> params);

    /**
     * Makes a request to redirect the given player to the given server.
     *
     * @param serverId the server to redirect to
     * @param player the player to be redirected
     * @param params the parameters for the request
     * @return a promise for the redirect response
     */
    default Promise<ReceivedResponse> redirectPlayer(@Nonnull String serverId, @Nonnull Player player, @Nonnull Map<String, String> params) {
        return redirectPlayer(serverId, Profile.create(player), params);
    }

    /**
     * Sets the {@link RequestHandler} for this instance.
     *
     * @param handler the handler
     */
    void setHandler(@Nonnull RequestHandler handler);

    /**
     * Adds a default parameter provider.
     *
     * @param provider the provider
     */
    void addDefaultParameterProvider(@Nonnull RedirectParameterProvider provider);

    /**
     * Sets if the system should ensure that incoming connections were made (and accepted) by the
     * redirect system.
     *
     * @param ensureJoinedViaQueue if the system should ensure all joins are in the redirect queue
     */
    void setEnsure(boolean ensureJoinedViaQueue);

    /**
     * Gets the number of connections which have been allowed, but not yet fully established.
     *
     * @return the number of expected connections
     */
    int getExpectedConnectionsCount();

    @Override
    void close();

    /**
     * Encapsulates a redirect request
     */
    interface Request {

        /**
         * Gets the profile sending the request
         *
         * @return the profile sending the request
         */
        @Nonnull
        Profile getProfile();

        /**
         * Gets the parameters included with the request.
         *
         * @return the parameters
         */
        @Nonnull
        Map<String, String> getParams();

    }

    /**
     * Encapsulates the response to a {@link Request}.
     */
    final class Response {
        private static final Response ALLOW = new Response(true, null, ImmutableMap.of());
        private static final Response DENY = new Response(false, null, ImmutableMap.of());

        public static Response allow() {
            return ALLOW;
        }

        public static Response allow(@Nonnull Map<String, String> params) {
            return new Response(true, null, params);
        }

        public static Response deny() {
            return DENY;
        }

        public static Response deny(@Nonnull String reason) {
            return new Response(false, reason, ImmutableMap.of());
        }

        public static Response deny(@Nonnull Map<String, String> params) {
            return new Response(false, null, params);
        }

        public static Response deny(@Nonnull String reason, @Nonnull Map<String, String> params) {
            return new Response(false, reason, params);
        }

        private final boolean allowed;
        private final String reason;
        private final Map<String, String> params;

        public Response(boolean allowed, @Nullable String reason, @Nonnull Map<String, String> params) {
            this.allowed = allowed;
            this.reason = reason;
            this.params = ImmutableMap.copyOf(params);
        }

        public boolean isAllowed() {
            return this.allowed;
        }

        @Nullable
        public String getReason() {
            return this.reason;
        }

        public Map<String, String> getParams() {
            return this.params;
        }
    }

    /**
     * Handles incoming redirect requests for this server
     */
    interface RequestHandler {

        /**
         * Handles the request and produces a result.
         *
         * @param request the request
         * @return the response
         */
        @Nonnull
        Promise<Response> handle(@Nonnull Request request);

    }

    /**
     * Represents the response to a redirect request.
     */
    interface ReceivedResponse {

        /**
         * Gets the status of the response
         *
         * @return the status
         */
        @Nonnull
        Status getStatus();

        /**
         * Gets the reason for the response.
         *
         * @return the reason
         */
        @Nonnull
        Optional<String> getReason();

        /**
         * Gets the parameters included with the response.
         *
         * @return the parameters
         */
        @Nonnull
        Map<String, String> getParams();

        enum Status {
            ALLOWED,
            DENIED,
            NO_REPLY
        }
    }

}
