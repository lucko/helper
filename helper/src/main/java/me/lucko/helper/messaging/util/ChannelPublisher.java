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

package me.lucko.helper.messaging.util;

import me.lucko.helper.Schedulers;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.promise.ThreadContext;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.terminable.Terminable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Periodically publishes a message to a channel.
 *
 * @param <T> the message type
 */
public final class ChannelPublisher<T> implements Terminable {

    /**
     * Creates a new channel publisher.
     *
     * @param channel the channel
     * @param duration the duration to wait between publishing
     * @param unit the unit of duration
     * @param threadContext the context to call the supplier in
     * @param supplier the message supplier
     * @param <T> the type of the message
     * @return a channel publisher
     */
    @Nonnull
    public static <T> ChannelPublisher<T> create(@Nonnull Channel<T> channel, long duration, @Nonnull TimeUnit unit, @Nonnull ThreadContext threadContext, @Nonnull Supplier<? extends T> supplier) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(unit, "unit");
        Objects.requireNonNull(threadContext, "threadContext");
        Objects.requireNonNull(supplier, "supplier");

        return new ChannelPublisher<>(channel, supplier, duration, unit, threadContext);
    }

    /**
     * Creates a new channel publisher.
     *
     * @param channel the channel
     * @param duration the duration to wait between publishing
     * @param unit the unit of duration
     * @param supplier the message supplier
     * @param <T> the type of the message
     * @return a channel publisher
     */
    @Nonnull
    public static <T> ChannelPublisher<T> create(@Nonnull Channel<T> channel, long duration, @Nonnull TimeUnit unit, @Nonnull Supplier<? extends T> supplier) {
        return create(channel, duration, unit, ThreadContext.ASYNC, supplier);
    }

    private final Channel<T> channel;
    private final Supplier<? extends T> supplier;
    private final Task task;

    private ChannelPublisher(Channel<T> channel, Supplier<? extends T> supplier, long duration, TimeUnit unit, ThreadContext threadContext) {
        this.channel = channel;
        this.supplier = supplier;
        this.task = Schedulers.builder()
                .on(threadContext)
                .afterAndEvery(duration, unit)
                .run(this::submit);
    }

    /**
     * Gets the channel
     *
     * @return the channel
     */
    public Channel<T> getChannel() {
        return this.channel;
    }

    private void submit() {
        this.channel.sendMessage(this.supplier.get());
    }

    @Override
    public void close() {
        this.task.close();
    }
}
