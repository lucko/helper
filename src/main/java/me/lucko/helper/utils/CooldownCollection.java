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

package me.lucko.helper.utils;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * A collection of Cooldown instances
 * @param <T> the key type
 */
public class CooldownCollection<T> {

    /**
     * Creates a new collection with the cooldown properties defined by the base instance
     *
     * @param base the cooldown to base off
     * @return a new collection
     */
    public static CooldownCollection<String> create(Cooldown base) {
        Preconditions.checkNotNull(base, "base");
        return new CooldownCollection<>(s -> s, base);
    }

    /**
     * Creates a new collection with the cooldown properties defined by the base instance
     *
     * @param mappingFunc the mapping function from the key type to String
     * @param base the cooldown to base off
     * @param <T> the key type
     * @return a new collection
     */
    public static <T> CooldownCollection<T> create(Function<T, String> mappingFunc, Cooldown base) {
        Preconditions.checkNotNull(mappingFunc, "mappingFunc");
        Preconditions.checkNotNull(base, "base");
        return new CooldownCollection<>(mappingFunc, base);
    }

    /**
     * Creates a new collection with the cooldown properties defined by the base instance
     *
     * <p>The mapping from key type to string is defined by the behaviour of {@link Object#toString()}</p>
     *
     * @param base the cooldown to base off
     * @param <T> the key type
     * @return a new collection
     */
    public static <T> CooldownCollection<T> createWithToString(Cooldown base) {
        Preconditions.checkNotNull(base, "base");
        return new CooldownCollection<>(Object::toString, base);
    }

    private final LoadingCache<String, Cooldown> cache;
    private final Function<T, String> mappingFunc;

    private CooldownCollection(Function<T, String> mappingFunc, Cooldown base) {
        cache = CacheBuilder.newBuilder()
                // remove from the cache 10 seconds after the cooldown expires
                .expireAfterAccess(base.getTimeout() + 10000L, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, Cooldown>() {
                    @Override
                    public Cooldown load(String s) {
                        return base.copy();
                    }
                });
        this.mappingFunc = mappingFunc;
    }

    /**
     * Gets the internal cooldown instance associated with the given key
     *
     * <p>The inline Cooldown methods in this class should be used to access the functionality of the cooldown as opposed
     * to calling the methods directly via the instance returned by this method.</p>
     *
     * @param t the key
     * @return a cooldown instance
     */
    public Cooldown get(T t) {
        return cache.getUnchecked(mappingFunc.apply(t));
    }

    /* methods from Cooldown */

    public boolean test(T t) {
        return get(t).test();
    }

    public boolean testSilently(T t) {
        return get(t).testSilently();
    }

    public long elapsed(T t) {
        return get(t).elapsed();
    }

    public void reset(T t) {
        get(t).reset();
    }

    public long remainingMillis(T t) {
        return get(t).remainingMillis();
    }

    public long remainingTime(T t, TimeUnit unit) {
        return get(t).remainingTime(unit);
    }
}
