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

package me.lucko.helper.cooldown;

import com.google.common.base.Preconditions;

import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * A self-populating, composed map of cooldown instances
 *
 * @param <I> input type
 * @param <O> internal type
 */
public interface ComposedCooldownMap<I, O> {

    /**
     * Creates a new collection with the cooldown properties defined by the base instance
     *
     * @param base the cooldown to base off
     * @return a new collection
     */
    @Nonnull
    static <I, O> ComposedCooldownMap<I, O> create(@Nonnull Cooldown base, @Nonnull Function<I, O> composeFunction) {
        Preconditions.checkNotNull(base, "base");
        Preconditions.checkNotNull(composeFunction, "composeFunction");
        return new ComposedCooldownMapImpl<>(base, composeFunction);
    }

    /**
     * Gets the base cooldown
     *
     * @return the base cooldown
     */
    @Nonnull
    Cooldown getBase();

    /**
     * Gets the internal cooldown instance associated with the given key
     *
     * @param key the key
     * @return a cooldown instance
     */
    @Nonnull
    Cooldown get(@Nonnull I key);

    void put(@Nonnull O key, @Nonnull Cooldown cooldown);

    /**
     * Gets the cooldowns contained within this collection.
     *
     * @return the backing map
     */
    @Nonnull
    Map<O, Cooldown> getAll();

    /* methods from Cooldown */

    boolean test(@Nonnull I key);

    boolean testSilently(@Nonnull I key);

    long elapsed(@Nonnull I key);

    void reset(@Nonnull I key);

    long remainingMillis(@Nonnull I key);

    long remainingTime(@Nonnull I key, @Nonnull TimeUnit unit);

    @Nonnull
    OptionalLong getLastTested(@Nonnull I key);

}
