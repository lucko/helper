/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me>
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CooldownCollection<T> {

    public static CooldownCollection<String> create(Cooldown base) {
        return new CooldownCollection<>(s -> s, base);
    }

    public static <T> CooldownCollection<T> create(Function<T, String> mappingFunc, Cooldown base) {
        return new CooldownCollection<>(mappingFunc, base);
    }

    public static <T> CooldownCollection<T> createWithToString(Cooldown base) {
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
