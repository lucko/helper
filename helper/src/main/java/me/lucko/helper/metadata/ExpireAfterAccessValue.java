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

package me.lucko.helper.metadata;

import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * Represents a value which will expire a set amount of time after the last access
 *
 * @param <T> the wrapped value type
 */
public class ExpireAfterAccessValue<T> implements TransientValue<T> {

    public static <T> ExpireAfterAccessValue<T> of(T value, long duration, TimeUnit unit) {
        Preconditions.checkArgument(duration >= 0, "duration must be >= 0");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(unit, "unit");

        long millis = unit.toMillis(duration);
        return new ExpireAfterAccessValue<>(value, millis);
    }

    public static <T> Supplier<ExpireAfterAccessValue<T>> supplied(Supplier<? extends T> supplier, long duration, TimeUnit unit) {
        Preconditions.checkArgument(duration >= 0, "duration must be >= 0");
        Objects.requireNonNull(supplier, "supplier");
        Objects.requireNonNull(unit, "unit");

        long millis = unit.toMillis(duration);

        return () -> {
            T value = supplier.get();
            Objects.requireNonNull(value, "value");

            return new ExpireAfterAccessValue<>(value, millis);
        };
    }

    private final T value;
    private final long millis;
    private long expireAt;

    private ExpireAfterAccessValue(T value, long millis) {
        this.value = value;
        this.millis = millis;
        this.expireAt = System.currentTimeMillis() + this.millis;
    }

    @Nullable
    @Override
    public T getOrNull() {
        if (shouldExpire()) {
            return null;
        }

        // reset expiry time
        this.expireAt = System.currentTimeMillis() + this.millis;
        return this.value;
    }

    @Override
    public boolean shouldExpire() {
        return System.currentTimeMillis() > this.expireAt;
    }

}
