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

package me.lucko.helper.messaging;

import com.google.common.reflect.TypeToken;

import me.lucko.helper.messaging.conversation.ConversationChannel;
import me.lucko.helper.messaging.conversation.ConversationMessage;
import me.lucko.helper.messaging.conversation.SimpleConversationChannel;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Represents an object which manages messaging {@link Channel}s.
 */
public interface Messenger {

    /**
     * Gets a channel by name.
     *
     * @param name the name of the channel.
     * @param type the channel message typetoken
     * @param <T> the channel message type
     * @return a channel
     */
    @Nonnull
    <T> Channel<T> getChannel(@Nonnull String name, @Nonnull TypeToken<T> type);


    /**
     * Gets a conversation channel by name.
     *
     * @param name the name of the channel
     * @param type the channel outgoing message typetoken
     * @param replyType the channel incoming (reply) message typetoken
     * @param <T> the channel message type
     * @param <R> the channel reply type
     * @return a conversation channel
     */
    @Nonnull
    default <T extends ConversationMessage, R extends ConversationMessage> ConversationChannel<T, R> getConversationChannel(@Nonnull String name, @Nonnull TypeToken<T> type, @Nonnull TypeToken<R> replyType) {
        return new SimpleConversationChannel<>(this, name, type, replyType);
    }

    /**
     * Gets a channel by name.
     *
     * @param name the name of the channel.
     * @param clazz the channel message class
     * @param <T> the channel message type
     * @return a channel
     */
    @Nonnull
    default <T> Channel<T> getChannel(@Nonnull String name, @Nonnull Class<T> clazz) {
        return getChannel(name, TypeToken.of(Objects.requireNonNull(clazz)));
    }

    /**
     * Gets a conversation channel by name.
     *
     * @param name the name of the channel
     * @param clazz the channel outgoing message class
     * @param replyClazz the channel incoming (reply) message class
     * @param <T> the channel message type
     * @param <R> the channel reply type
     * @return a conversation channel
     */
    @Nonnull
    default <T extends ConversationMessage, R extends ConversationMessage> ConversationChannel<T, R> getConversationChannel(@Nonnull String name, @Nonnull Class<T> clazz, @Nonnull Class<R> replyClazz) {
        return getConversationChannel(name, TypeToken.of(Objects.requireNonNull(clazz)), TypeToken.of(Objects.requireNonNull(replyClazz)));
    }

}
