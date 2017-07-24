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

import java.util.concurrent.CompletableFuture;

/**
 * Represents an individual messaging channel.
 *
 * <p>Channels can be subscribed to through a {@link ChannelAgent}.</p>
 *
 * @param <T> the channel message type
 */
public interface Channel<T> {

    /**
     * Gets the name of the channel.
     *
     * @return the channel name
     */
    String getName();

    /**
     * Gets the channels message type.
     *
     * @return the channels message type.
     */
    TypeToken<T> getType();

    /**
     * Creates a new {@link ChannelAgent} for this channel.
     *
     * @return a new channel agent.
     */
    ChannelAgent<T> newAgent();

    /**
     * Sends a new message to the channel.
     *
     * <p>This method will return immediately, and the future will be completed
     * once the message has been sent.</p>
     *
     * <p>The future will return true if the message was sent successfully.</p>
     *
     * @param message the message to dispatch
     * @return a future which will complete when the message has sent.
     */
    CompletableFuture<Boolean> sendMessage(T message);

}
