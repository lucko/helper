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
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.utils.Cooldown;
import me.lucko.helper.utils.CooldownCollection;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

class SingleBuilder<T extends Event> implements SingleSubscriptionBuilder<T> {
    final Class<T> eventClass;
    final EventPriority priority;

    long expiry = -1;
    long maxCalls = -1;
    BiConsumer<? super T, Throwable> exceptionConsumer = DEFAULT_EXCEPTION_CONSUMER;
    final List<Predicate<T>> filters = new ArrayList<>();

    SingleBuilder(Class<T> eventClass, EventPriority priority) {
        this.eventClass = eventClass;
        this.priority = priority;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> expireAfter(long duration, @Nonnull TimeUnit unit) {
        Preconditions.checkNotNull(unit, "unit");
        Preconditions.checkArgument(duration >= 1, "duration >= 1");
        this.expiry = Math.addExact(System.currentTimeMillis(), unit.toMillis(duration));
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> expireAfter(long maxCalls) {
        Preconditions.checkArgument(maxCalls >= 1, "maxCalls >= 1");
        this.maxCalls = maxCalls;
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> exceptionConsumer(@Nonnull BiConsumer<? super T, Throwable> exceptionConsumer) {
        Preconditions.checkNotNull(exceptionConsumer, "exceptionConsumer");
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> filter(@Nonnull Predicate<T> predicate) {
        Preconditions.checkNotNull(predicate, "predicate");
        this.filters.add(predicate);
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> withCooldown(@Nonnull Cooldown cooldown) {
        Preconditions.checkNotNull(cooldown, "cooldown");
        filter(t -> cooldown.test());
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> withCooldown(@Nonnull Cooldown cooldown, @Nonnull BiConsumer<Cooldown, ? super T> cooldownFailConsumer) {
        Preconditions.checkNotNull(cooldown, "cooldown");
        Preconditions.checkNotNull(cooldownFailConsumer, "cooldownFailConsumer");
        filter(t -> {
            if (cooldown.test()) {
                return true;
            }

            cooldownFailConsumer.accept(cooldown, t);
            return false;
        });
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> withCooldown(@Nonnull CooldownCollection<? super T> cooldown) {
        Preconditions.checkNotNull(cooldown, "cooldown");
        filter(t -> cooldown.get(t).test());
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscriptionBuilder<T> withCooldown(@Nonnull CooldownCollection<? super T> cooldown, @Nonnull BiConsumer<Cooldown, ? super T> cooldownFailConsumer) {
        Preconditions.checkNotNull(cooldown, "cooldown");
        Preconditions.checkNotNull(cooldownFailConsumer, "cooldownFailConsumer");
        filter(t -> {
            if (cooldown.get(t).test()) {
                return true;
            }

            cooldownFailConsumer.accept(cooldown.get(t), t);
            return false;
        });
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscription<T> biHandler(@Nonnull BiConsumer<SingleSubscription<T>, ? super T> handler) {
        Preconditions.checkNotNull(handler, "handler");

        HelperEventListener<T> impl = new HelperEventListener<>(this, handler);
        impl.register(LoaderUtils.getPlugin());
        return impl;
    }
}
