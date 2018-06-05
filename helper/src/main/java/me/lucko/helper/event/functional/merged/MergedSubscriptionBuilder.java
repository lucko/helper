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

package me.lucko.helper.event.functional.merged;

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import me.lucko.helper.event.MergedSubscription;
import me.lucko.helper.event.functional.ExpiryTestStage;
import me.lucko.helper.event.functional.SubscriptionBuilder;
import me.lucko.helper.utils.Delegates;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * Functional builder for {@link MergedSubscription}s.
 *
 * @param <T> the handled type
 */
public interface MergedSubscriptionBuilder<T> extends SubscriptionBuilder<T> {

    /**
     * Makes a MergedHandlerBuilder for a given super type
     *
     * @param handledClass the super type of the event handler
     * @param <T>          the super type class
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    static <T> MergedSubscriptionBuilder<T> newBuilder(@Nonnull Class<T> handledClass) {
        Objects.requireNonNull(handledClass, "handledClass");
        return new MergedSubscriptionBuilderImpl<>(TypeToken.of(handledClass));
    }

    /**
     * Makes a MergedHandlerBuilder for a given super type
     *
     * @param type the super type of the event handler
     * @param <T>  the super type class
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    static <T> MergedSubscriptionBuilder<T> newBuilder(@Nonnull TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        return new MergedSubscriptionBuilderImpl<>(type);
    }

    /**
     * Makes a MergedHandlerBuilder for a super event class
     *
     * @param superClass   the abstract super event class
     * @param eventClasses the event classes to be bound to
     * @param <S>          the super class type
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    @SafeVarargs
    static <S extends Event> MergedSubscriptionBuilder<S> newBuilder(@Nonnull Class<S> superClass, @Nonnull Class<? extends S>... eventClasses) {
        return newBuilder(superClass, EventPriority.NORMAL, eventClasses);
    }

    /**
     * Makes a MergedHandlerBuilder for a super event class
     *
     * @param superClass   the abstract super event class
     * @param priority     the priority to listen at
     * @param eventClasses the event classes to be bound to
     * @param <S>          the super class type
     * @return a {@link MergedSubscriptionBuilder} to construct the event handler
     */
    @Nonnull
    @SafeVarargs
    static <S extends Event> MergedSubscriptionBuilder<S> newBuilder(@Nonnull Class<S> superClass, @Nonnull EventPriority priority, @Nonnull Class<? extends S>... eventClasses) {
        Objects.requireNonNull(superClass, "superClass");
        Objects.requireNonNull(eventClasses, "eventClasses");
        Objects.requireNonNull(priority, "priority");
        if (eventClasses.length < 2) {
            throw new IllegalArgumentException("merge method used for only one subclass");
        }

        MergedSubscriptionBuilderImpl<S> h = new MergedSubscriptionBuilderImpl<>(TypeToken.of(superClass));
        for (Class<? extends S> clazz : eventClasses) {
            h.bindEvent(clazz, priority, e -> e);
        }
        return h;
    }

    // override return type - we return MergedSubscriptionBuilder, not SubscriptionBuilder

    @Nonnull
    @Override
    default MergedSubscriptionBuilder<T> expireIf(@Nonnull Predicate<T> predicate) {
        return expireIf(Delegates.predicateToBiPredicateSecond(predicate), ExpiryTestStage.PRE, ExpiryTestStage.POST_HANDLE);
    }

    @Nonnull
    @Override
    default MergedSubscriptionBuilder<T> expireAfter(long duration, @Nonnull TimeUnit unit) {
        Objects.requireNonNull(unit, "unit");
        Preconditions.checkArgument(duration >= 1, "duration < 1");
        long expiry = Math.addExact(System.currentTimeMillis(), unit.toMillis(duration));
        return expireIf((handler, event) -> System.currentTimeMillis() > expiry, ExpiryTestStage.PRE);
    }

    @Nonnull
    @Override
    default MergedSubscriptionBuilder<T> expireAfter(long maxCalls) {
        Preconditions.checkArgument(maxCalls >= 1, "maxCalls < 1");
        return expireIf((handler, event) -> handler.getCallCounter() >= maxCalls, ExpiryTestStage.PRE, ExpiryTestStage.POST_HANDLE);
    }

    @Nonnull
    @Override
    MergedSubscriptionBuilder<T> filter(@Nonnull Predicate<T> predicate);

    /**
     * Add a expiry predicate.
     *
     * @param predicate the expiry test
     * @param testPoints when to test the expiry predicate
     * @return ths builder instance
     */
    @Nonnull
    MergedSubscriptionBuilder<T> expireIf(@Nonnull BiPredicate<MergedSubscription<T>, T> predicate, @Nonnull ExpiryTestStage... testPoints);

    /**
     * Binds this handler to an event
     *
     * @param eventClass the event class to bind to
     * @param function   the function to remap the event
     * @param <E>        the event class
     * @return the builder instance
     */
    @Nonnull
    <E extends Event> MergedSubscriptionBuilder<T> bindEvent(@Nonnull Class<E> eventClass, @Nonnull Function<E, T> function);

    /**
     * Binds this handler to an event
     *
     * @param eventClass the event class to bind to
     * @param priority   the priority to listen at
     * @param function   the function to remap the event
     * @param <E>        the event class
     * @return the builder instance
     */
    @Nonnull
    <E extends Event> MergedSubscriptionBuilder<T> bindEvent(@Nonnull Class<E> eventClass, @Nonnull EventPriority priority, @Nonnull Function<E, T> function);

    /**
     * Sets the exception consumer for the handler.
     *
     * <p> If an exception is thrown in the handler, it is passed to this consumer to be swallowed.
     *
     * @param consumer the consumer
     * @return the builder instance
     * @throws NullPointerException if the consumer is null
     */
    @Nonnull
    MergedSubscriptionBuilder<T> exceptionConsumer(@Nonnull BiConsumer<Event, Throwable> consumer);

    /**
     * Return the handler list builder to append handlers for the event.
     *
     * @return the handler list
     */
    @Nonnull
    MergedHandlerList<T> handlers();

    /**
     * Builds and registers the Handler.
     *
     * @param handler the consumer responsible for handling the event.
     * @return a registered {@link MergedSubscription} instance.
     * @throws NullPointerException  if the handler is null
     * @throws IllegalStateException if no events have been bound to
     */
    @Nonnull
    default MergedSubscription<T> handler(@Nonnull Consumer<? super T> handler) {
        return handlers().consumer(handler).register();
    }

    /**
     * Builds and registers the Handler.
     *
     * @param handler the bi-consumer responsible for handling the event.
     * @return a registered {@link MergedSubscription} instance.
     * @throws NullPointerException  if the handler is null
     * @throws IllegalStateException if no events have been bound to
     */
    @Nonnull
    default MergedSubscription<T> biHandler(@Nonnull BiConsumer<MergedSubscription<T>, ? super T> handler) {
        return handlers().biConsumer(handler).register();
    }
    
}
