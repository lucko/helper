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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A cached supplier extension.
 *
 * <p>The delegate supplier is only called on executions of {@link #get()} if the
 * result is not cached. Subsequent calls will block until the value is calculated.</p>
 *
 * @param <T> the supplied type
 */
public final class Cache<T> implements Supplier<T> {

    public static <T> Cache<T> suppliedBy(Supplier<T> supplier) {
        return new Cache<>(Objects.requireNonNull(supplier, "supplier"));
    }

    private final Supplier<T> supplier;
    private volatile T value = null;

    private Cache(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public final T get() {
        T val = this.value;

        // double checked locking
        if (val == null) {
            synchronized (this) {
                val = this.value;
                if (val == null) {
                    val = this.supplier.get();
                    this.value = val;
                }
            }
        }

        return val;
    }

    public final Optional<T> getIfPresent() {
        return Optional.ofNullable(this.value);
    }

    public final void invalidate() {
        this.value = null;
    }
}
