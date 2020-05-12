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

import java.util.concurrent.TimeUnit;

public final class SelfExpiring<T> {

    private final T defaultValue;
    private final long durationNanos;

    private volatile T value;

    // when to expire. 0 means "not yet initialized".
    private volatile long expirationNanos;

    public SelfExpiring(T value, long duration, TimeUnit unit) {
        this.defaultValue = value;
        this.durationNanos = unit.toNanos(duration);
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public T get() {
        long nanos = this.expirationNanos;
        long now = System.nanoTime();

        if (nanos == 0 || now - nanos >= 0) {
            synchronized (this) {
                if (nanos == this.expirationNanos) {
                    return this.defaultValue;
                }
            }
        }
        return this.value;
    }

    public boolean set(T value) {
        long nanos = this.expirationNanos;
        long now = System.nanoTime();
        if (nanos == 0 || now - nanos >= 0) {
            synchronized (this) {
                if (nanos == this.expirationNanos) {
                    this.value = value;

                    // reset expiration timer
                    nanos = now + this.durationNanos;
                    // In the very unlikely event that nanos is 0, set it to 1;
                    // no one will notice 1 ns of tardiness.
                    this.expirationNanos = (nanos == 0) ? 1 : nanos;
                    return true;
                }
            }
        }
        return false;
    }
}
