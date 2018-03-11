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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Map;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Nonnull;

class ComposedCooldownMapImpl<I, O> implements ComposedCooldownMap<I, O> {

    private final Cooldown base;
    private final LoadingCache<O, Cooldown> cache;
    private final Function<I, O> composeFunction;

    ComposedCooldownMapImpl(Cooldown base, Function<I, O> composeFunction) {
        this.base = base;
        this.composeFunction = composeFunction;
        this.cache = CacheBuilder.newBuilder()
                // remove from the cache 10 seconds after the cooldown expires
                .expireAfterAccess(base.getTimeout() + 10000L, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<O, Cooldown>() {
                    @Override
                    public Cooldown load(@Nonnull O key) {
                        return base.copy();
                    }
                });
    }

    @Nonnull
    @Override
    public Cooldown getBase() {
        return this.base;
    }

    /**
     * Gets the internal cooldown instance associated with the given key
     *
     * <p>The inline Cooldown methods in this class should be used to access the functionality of the cooldown as opposed
     * to calling the methods directly via the instance returned by this method.</p>
     *
     * @param key the key
     * @return a cooldown instance
     */
    @Nonnull
    public Cooldown get(@Nonnull I key) {
        Preconditions.checkNotNull(key, "key");
        return this.cache.getUnchecked(this.composeFunction.apply(key));
    }

    @Override
    public void put(@Nonnull O key, @Nonnull Cooldown cooldown) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkArgument(cooldown.getTimeout() == this.base.getTimeout(), "different timeout");
        this.cache.put(key, cooldown);
    }

    /**
     * Gets the cooldowns contained within this collection.
     *
     * @return the backing map
     */
    @Nonnull
    public Map<O, Cooldown> getAll() {
        return this.cache.asMap();
    }

    /* methods from Cooldown */

    @Override
    public boolean test(@Nonnull I key) {
        return get(key).test();
    }

    @Override
    public boolean testSilently(@Nonnull I key) {
        return get(key).testSilently();
    }

    @Override
    public long elapsed(@Nonnull I key) {
        return get(key).elapsed();
    }

    @Override
    public void reset(@Nonnull I key) {
        get(key).reset();
    }

    @Override
    public long remainingMillis(@Nonnull I key) {
        return get(key).remainingMillis();
    }

    @Override
    public long remainingTime(@Nonnull I key, @Nonnull TimeUnit unit) {
        return get(key).remainingTime(unit);
    }

    @Nonnull
    @Override
    public OptionalLong getLastTested(@Nonnull I key) {
        return get(key).getLastTested();
    }
}
