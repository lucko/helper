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

package me.lucko.helper.messaging.conversation;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;

import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.messaging.ChannelListener;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.promise.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Simple implementation of {@link ConversationChannel}.
 *
 * @param <T> the outgoing message type
 * @param <R> the reply message type
 */
public class SimpleConversationChannel<T extends ConversationMessage, R extends ConversationMessage> implements ConversationChannel<T, R> {
    private final String name;
    private final Channel<T> outgoingChannel;
    private final Channel<R> replyChannel;

    private final Set<Agent<T, R>> agents = ConcurrentHashMap.newKeySet();

    private final ScheduledExecutorService replyTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ChannelAgent<R> replyAgent;
    private final SetMultimap<UUID, ReplyListenerRegistration<R>> replyListeners = Multimaps.newSetMultimap(new ConcurrentHashMap<>(), ConcurrentHashMap::newKeySet);

    public SimpleConversationChannel(Messenger messenger, String name, TypeToken<T> outgoingType, TypeToken<R> replyType) {
        this.name = name;
        this.outgoingChannel = messenger.getChannel(name + "-o", outgoingType);
        this.replyChannel = messenger.getChannel(name + "-r", replyType);

        this.replyAgent = this.replyChannel.newAgent(new ReplyListener());
    }

    private final class ReplyListener implements ChannelListener<R> {
        @Override
        public void onMessage(@Nonnull ChannelAgent<R> agent, @Nonnull R message) {
            SimpleConversationChannel.this.replyListeners.get(message.getConversationId()).removeIf(l -> l.onReply(message));
        }
    }

    private static final class ReplyListenerRegistration<R extends ConversationMessage> {
        private final ConversationReplyListener<R> listener;
        private final List<R> replies = new ArrayList<>();
        private ScheduledFuture<?> timeoutFuture;

        private boolean active = true;

        private ReplyListenerRegistration(ConversationReplyListener<R> listener) {
            this.listener = listener;

        }

        /**
         * Passes the incoming reply to the listener, and returns true if the listener should be
         * unregistered
         *
         * @param message the message
         * @return if the listener should be unregistered
         */
        public boolean onReply(R message) {
            synchronized (this) {
                if (!this.active) {
                    return true;
                }

                this.replies.add(message);
                ConversationReplyListener.RegistrationAction action = this.listener.onReply(message);
                if (action == ConversationReplyListener.RegistrationAction.STOP_LISTENING) {

                    // unregister
                    this.active = false;
                    this.timeoutFuture.cancel(false);

                    return true;
                } else {
                    return false;
                }
            }
        }

        public void timeout() {
            synchronized (this) {
                if (!this.active) {
                    return;
                }

                this.listener.onTimeout(this.replies);
                this.active = false;
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return this.name;
    }

    @Nonnull
    @Override
    public Channel<T> getOutgoingChannel() {
        return this.outgoingChannel;
    }

    @Nonnull
    @Override
    public Channel<R> getReplyChannel() {
        return this.replyChannel;
    }

    @Nonnull
    @Override
    public ConversationChannelAgent<T, R> newAgent() {
        Agent<T, R> agent = new Agent<>(this);
        this.agents.add(agent);
        return agent;
    }

    @Nonnull
    @Override
    public Promise<Void> sendMessage(@Nonnull T message, @Nonnull ConversationReplyListener<R> replyListener, long timeoutDuration, @Nonnull TimeUnit unit) {
        // register the listener
        ReplyListenerRegistration<R> listenerRegistration = new ReplyListenerRegistration<>(replyListener);
        listenerRegistration.timeoutFuture = this.replyTimeoutExecutor.schedule(listenerRegistration::timeout, timeoutDuration, unit);
        this.replyListeners.put(message.getConversationId(), listenerRegistration);

        // send the outgoing message
        return this.outgoingChannel.sendMessage(message);
    }

    @Override
    public void close() {
        this.replyAgent.close();
        this.replyTimeoutExecutor.shutdown();
        this.agents.forEach(Agent::close);
    }

    private static final class Agent<T extends ConversationMessage, R extends ConversationMessage> implements ConversationChannelAgent<T, R> {
        private final SimpleConversationChannel<T, R> channel;
        private final ChannelAgent<T> delegateAgent;

        private Agent(@Nonnull SimpleConversationChannel<T, R> channel) {
            this.channel = channel;
            this.delegateAgent = this.channel.getOutgoingChannel().newAgent();
        }

        @Nonnull
        @Override
        public ConversationChannel<T, R> getChannel() {
            this.delegateAgent.getChannel(); // ensure this agent is still active
            return this.channel;
        }

        @Nonnull
        @Override
        public Set<ConversationChannelListener<T, R>> getListeners() {
            Set<ChannelListener<T>> listeners = this.delegateAgent.getListeners();

            ImmutableSet.Builder<ConversationChannelListener<T, R>> ret = ImmutableSet.builder();
            for (ChannelListener<T> listener : listeners) {
                //noinspection unchecked
                ret.add(((WrappedListener) listener).delegate);
            }
            return ret.build();
        }

        @Override
        public boolean hasListeners() {
            return this.delegateAgent.hasListeners();
        }

        @Override
        public boolean addListener(@Nonnull ConversationChannelListener<T, R> listener) {
            return this.delegateAgent.addListener(new WrappedListener(listener));
        }

        @Override
        public boolean removeListener(@Nonnull ConversationChannelListener<T, R> listener) {
            Set<ChannelListener<T>> listeners = this.delegateAgent.getListeners();
            for (ChannelListener<T> other : listeners) {
                WrappedListener wrapped = (WrappedListener) other;
                if (wrapped.delegate == listener) {
                    return this.delegateAgent.removeListener(other);
                }
            }
            return false;
        }

        @Override
        public void close() {
            this.delegateAgent.close();
        }

        private final class WrappedListener implements ChannelListener<T> {
            private final ConversationChannelListener<T, R> delegate;

            private WrappedListener(ConversationChannelListener<T, R> delegate) {
                this.delegate = delegate;
            }

            @Override
            public void onMessage(@Nonnull ChannelAgent<T> agent, @Nonnull T message) {
                ConversationReply<R> reply = this.delegate.onMessage(Agent.this, message);
                if (reply.hasReply()) {
                    reply.getReply().thenAcceptAsync(m -> {
                        if (m != null) {
                            Agent.this.channel.replyChannel.sendMessage(m);
                        }
                    });
                }
            }
        }
    }
}
