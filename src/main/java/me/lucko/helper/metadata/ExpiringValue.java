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

import java.util.concurrent.TimeUnit;

/**
 * Represents a value which will expire in the future
 *
 * @param <T> the wrapped value type
 */
public final class ExpiringValue<T> implements TransientValue<T> {

    public static <T> ExpiringValue<T> of(T value, long duration, TimeUnit unit) {
        Preconditions.checkArgument(duration >= 0, "duration must be >= 0");
        Preconditions.checkNotNull(value, "value");
        Preconditions.checkNotNull(unit, "unit");

        long millis = unit.toMillis(duration);
        long expireAt = System.currentTimeMillis() + millis;

        return new ExpiringValue<>(value, expireAt);
    }

    private final T value;
    private final long expireAt;

    private ExpiringValue(T value, long expireAt) {
        this.value = value;
        this.expireAt = expireAt;
    }

    @Override
    public T getOrNull() {
        return shouldExpire() ? null : value;
    }

    @Override
    public boolean shouldExpire() {
        return System.currentTimeMillis() > expireAt;
    }

}
