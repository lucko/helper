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

package me.lucko.helper.cache;

import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * An expiring supplier extension.
 *
 * <p>The delegate supplier is only called on executions of {@link #get()} if the
 * result isn't already calculated.</p>
 *
 * @param <T> the supplied type
 */
public final class Expiring<T> implements Supplier<T> {

    public static <T> Expiring<T> suppliedBy(Supplier<T> supplier, long duration, TimeUnit unit) {
        Objects.requireNonNull(supplier, "supplier");
        Preconditions.checkArgument(duration > 0);
        Objects.requireNonNull(unit, "unit");

        return new Expiring<>(supplier, duration, unit);
    }

    private final Supplier<T> supplier;
    private final long durationNanos;

    private volatile T value;

    // when to expire. 0 means "not yet initialized".
    private volatile long expirationNanos;

    private Expiring(Supplier<T> supplier, long duration, TimeUnit unit) {
        this.supplier = supplier;
        this.durationNanos = unit.toNanos(duration);
    }

    @Override
    public T get() {
        long nanos = this.expirationNanos;
        long now = System.nanoTime();

        if (nanos == 0 || now - nanos >= 0) {
            synchronized (this) {
                if (nanos == this.expirationNanos) { // recheck for lost race
                    // compute the value using the delegate
                    T t = this.supplier.get();
                    this.value = t;

                    // reset expiration timer
                    nanos = now + this.durationNanos;
                    // In the very unlikely event that nanos is 0, set it to 1;
                    // no one will notice 1 ns of tardiness.
                    this.expirationNanos = (nanos == 0) ? 1 : nanos;
                    return t;
                }
            }
        }
        return this.value;
    }
}
