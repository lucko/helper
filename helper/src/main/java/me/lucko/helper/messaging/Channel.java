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

import me.lucko.helper.interfaces.TypeAware;
import me.lucko.helper.messaging.codec.Codec;
import me.lucko.helper.promise.Promise;

import javax.annotation.Nonnull;

/**
 * Represents an individual messaging channel.
 *
 * <p>Channels can be subscribed to through a {@link ChannelAgent}.</p>
 *
 * @param <T> the channel message type
 */
public interface Channel<T> extends TypeAware<T> {

    /**
     * Gets the name of the channel.
     *
     * @return the channel name
     */
    @Nonnull
    String getName();

    /**
     * Gets the channels message type.
     *
     * @return the channels message type.
     */
    @Override
    @Nonnull
    TypeToken<T> getType();

    /**
     * Gets the channels codec.
     *
     * @return the codec
     */
    @Nonnull
    Codec<T> getCodec();

    /**
     * Creates a new {@link ChannelAgent} for this channel.
     *
     * @return a new channel agent.
     */
    @Nonnull
    ChannelAgent<T> newAgent();

    /**
     * Creates a new {@link ChannelAgent} for this channel, and immediately
     * adds the given {@link ChannelListener} to it.
     *
     * @param listener the listener to register
     * @return the resultant agent
     */
    @Nonnull
    default ChannelAgent<T> newAgent(ChannelListener<T> listener) {
        ChannelAgent<T> agent = newAgent();
        agent.addListener(listener);
        return agent;
    }

    /**
     * Sends a new message to the channel.
     *
     * <p>This method will return immediately, and the future will be completed
     * once the message has been sent.</p>
     *
     * @param message the message to dispatch
     * @return a promise which will complete when the message has sent.
     */
    @Nonnull
    Promise<Void> sendMessage(@Nonnull T message);

}
