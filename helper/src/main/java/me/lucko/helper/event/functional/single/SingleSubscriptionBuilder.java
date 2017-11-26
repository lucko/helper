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

package me.lucko.helper.event.functional.single;

import com.google.common.base.Preconditions;

import me.lucko.helper.event.SingleSubscription;
import me.lucko.helper.event.functional.SubscriptionBuilder;
import me.lucko.helper.utils.Cooldown;
import me.lucko.helper.utils.CooldownCollection;
import me.lucko.helper.utils.Delegates;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * Functional builder for {@link SingleSubscription}s.
 *
 * @param <T> the event type
 */
public interface SingleSubscriptionBuilder<T extends Event> extends SubscriptionBuilder<T> {

    /**
     * Makes a HandlerBuilder for a given event
     *
     * @param eventClass the class of the event
     * @param <T>        the event type
     * @return a {@link SingleSubscriptionBuilder} to construct the event handler
     * @throws NullPointerException if eventClass is null
     */
    @Nonnull
    static <T extends Event> SingleSubscriptionBuilder<T> newBuilder(@Nonnull Class<T> eventClass) {
        return newBuilder(eventClass, EventPriority.NORMAL);
    }

    /**
     * Makes a HandlerBuilder for a given event
     *
     * @param eventClass the class of the event
     * @param priority   the priority to listen at
     * @param <T>        the event type
     * @return a {@link SingleSubscriptionBuilder} to construct the event handler
     * @throws NullPointerException if eventClass or priority is null
     */
    @Nonnull
    static <T extends Event> SingleSubscriptionBuilder<T> newBuilder(@Nonnull Class<T> eventClass, @Nonnull EventPriority priority) {
        Preconditions.checkNotNull(eventClass, "eventClass");
        Preconditions.checkNotNull(priority, "priority");
        return new SingleBuilder<>(eventClass, priority);
    }

    // override return type - we return SingleSubscriptionBuilder, not SubscriptionBuilder
    @Nonnull @Override SingleSubscriptionBuilder<T> expireAfter(long duration, @Nonnull TimeUnit unit);
    @Nonnull @Override SingleSubscriptionBuilder<T> expireAfter(long maxCalls);
    @Nonnull @Override SingleSubscriptionBuilder<T> filter(@Nonnull Predicate<T> predicate);
    @Nonnull @Override SingleSubscriptionBuilder<T> withCooldown(@Nonnull Cooldown cooldown);
    @Nonnull @Override SingleSubscriptionBuilder<T> withCooldown(@Nonnull Cooldown cooldown, @Nonnull BiConsumer<Cooldown, ? super T> cooldownFailConsumer);
    @Nonnull @Override SingleSubscriptionBuilder<T> withCooldown(@Nonnull CooldownCollection<? super T> cooldown);
    @Nonnull @Override SingleSubscriptionBuilder<T> withCooldown(@Nonnull CooldownCollection<? super T> cooldown, @Nonnull BiConsumer<Cooldown, ? super T> cooldownFailConsumer);

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
    SingleSubscriptionBuilder<T> exceptionConsumer(@Nonnull BiConsumer<? super T, Throwable> consumer);

    /**
     * Builds and registers the Handler.
     *
     * @param handler the consumer responsible for handling the event.
     * @return a registered {@link SingleSubscription} instance.
     * @throws NullPointerException if the handler is null
     */
    @Nonnull
    default SingleSubscription<T> handler(@Nonnull Consumer<? super T> handler) {
        return biHandler(Delegates.consumerToBiConsumerSecond(handler));
    }

    /**
     * Builds and registers the Handler.
     *
     * @param handler the bi-consumer responsible for handling the event.
     * @return a registered {@link SingleSubscription} instance.
     * @throws NullPointerException if the handler is null
     */
    @Nonnull
    SingleSubscription<T> biHandler(@Nonnull BiConsumer<SingleSubscription<T>, ? super T> handler);
    
}
