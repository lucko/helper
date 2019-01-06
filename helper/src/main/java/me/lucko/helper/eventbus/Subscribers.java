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

package me.lucko.helper.eventbus;

import me.lucko.helper.terminable.Terminable;

import net.kyori.event.EventBus;
import net.kyori.event.EventSubscriber;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Utilities for working with {@link EventSubscriber}s.
 */
public final class Subscribers {

    private Subscribers() {}

    /**
     * Registers the given {@code subscriber} with the {@code bus}, and returns
     * a {@link Terminable} to encapsulate the subscription.
     *
     * @param bus the event bus
     * @param clazz the registration class
     * @param subscriber the subscriber
     * @param <E> the event type
     * @param <T> the subscriber type
     * @return a terminable to encapsulate the subscription
     */
    public static <E, T extends E> Terminable register(EventBus<E> bus, @NonNull final Class<T> clazz, @NonNull final EventSubscriber<? super T> subscriber) {
        bus.register(clazz, subscriber);
        return () -> bus.unregister(subscriber);
    }

}
