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

package me.lucko.helper;

import com.google.common.reflect.TypeToken;

import me.lucko.helper.event.functional.merged.MergedSubscriptionBuilder;
import me.lucko.helper.event.functional.single.SingleSubscriptionBuilder;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import javax.annotation.Nonnull;

/**
 * A functional event listening utility.
 */
public final class Events {

    /**
     * Makes a SingleSubscriptionBuilder for a given event
     *
     * @param eventClass the class of the event
     * @param <T>        the event type
     * @return a {@link SingleSubscriptionBuilder} to construct the event handler
     * @throws NullPointerException if eventClass is null
     */
    @Nonnull
    public static <T extends Event> SingleSubscriptionBuilder<T> subscribe(@Nonnull Class<T> eventClass) {
        return SingleSubscriptionBuilder.newBuilder(eventClass);
    }

    /**
     * Makes a SingleSubscriptionBuilder for a given event
     *
     * @param eventClass the class of the event
     * @param priority   the priority to listen at
     * @param <T>        the event type
     * @return a {@link SingleSubscriptionBuilder} to construct the event handler
     * @throws NullPointerException if eventClass or priority is null
     */
    @Nonnull
    public static <T extends Event> SingleSubscriptionBuilder<T> subscribe(@Nonnull Class<T> eventClass, @Nonnull EventPriority priority) {
        return SingleSubscriptionBuilder.newBuilder(eventClass, priority);
    }

    /**
     * Makes a MergedSubscriptionBuilder for a given super type
     *
     * @param handledClass the super type of the event handler
     * @param <T>          the super type class
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    public static <T> MergedSubscriptionBuilder<T> merge(@Nonnull Class<T> handledClass) {
        return MergedSubscriptionBuilder.newBuilder(handledClass);
    }

    /**
     * Makes a MergedSubscriptionBuilder for a given super type
     *
     * @param type the super type of the event handler
     * @param <T>  the super type class
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    public static <T> MergedSubscriptionBuilder<T> merge(@Nonnull TypeToken<T> type) {
        return MergedSubscriptionBuilder.newBuilder(type);
    }

    /**
     * Makes a MergedSubscriptionBuilder for a super event class
     *
     * @param superClass   the abstract super event class
     * @param eventClasses the event classes to be bound to
     * @param <S>          the super class type
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    @SafeVarargs
    public static <S extends Event> MergedSubscriptionBuilder<S> merge(@Nonnull Class<S> superClass, @Nonnull Class<? extends S>... eventClasses) {
        return MergedSubscriptionBuilder.newBuilder(superClass, eventClasses);
    }

    /**
     * Makes a MergedSubscriptionBuilder for a super event class
     *
     * @param superClass   the abstract super event class
     * @param priority     the priority to listen at
     * @param eventClasses the event classes to be bound to
     * @param <S>          the super class type
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    @SafeVarargs
    public static <S extends Event> MergedSubscriptionBuilder<S> merge(@Nonnull Class<S> superClass, @Nonnull EventPriority priority, @Nonnull Class<? extends S>... eventClasses) {
        return MergedSubscriptionBuilder.newBuilder(superClass, priority, eventClasses);
    }

    /**
     * Submit the event on the current thread
     *
     * @param event the event to call
     */
    public static void call(@Nonnull Event event) {
        Helper.plugins().callEvent(event);
    }

    /**
     * Submit the event on a new async thread.
     *
     * @param event the event to call
     */
    public static void callAsync(@Nonnull Event event) {
        Schedulers.async().run(() -> call(event));
    }

    /**
     * Submit the event on the main server thread.
     *
     * @param event the event to call
     */
    public static void callSync(@Nonnull Event event) {
        Schedulers.sync().run(() -> call(event));
    }

    /**
     * Submit the event on the current thread
     *
     * @param event the event to call
     */
    @Nonnull
    public static <T extends Event> T callAndReturn(@Nonnull T event) {
        Helper.plugins().callEvent(event);
        return event;
    }

    /**
     * Submit the event on a new async thread.
     *
     * @param event the event to call
     */
    @Nonnull
    public static <T extends Event> T callAsyncAndJoin(@Nonnull T event) {
        return Schedulers.async().supply(() -> callAndReturn(event)).join();
    }

    /**
     * Submit the event on the main server thread.
     *
     * @param event the event to call
     */
    @Nonnull
    public static <T extends Event> T callSyncAndJoin(@Nonnull T event) {
        return Schedulers.sync().supply(() -> callAndReturn(event)).join();
    }

    private Events() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
