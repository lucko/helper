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

import me.lucko.helper.utils.NullableOptional;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Supplier<T> supplier;
    private T value;
    private boolean hasValue = false;

    private Cache(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        // try to just read from the cached value
        this.lock.readLock().lock();
        try {
            if (this.hasValue) {
                return this.value;
            }
        } finally {
            // we have to release the read lock, as it is not possible
            // to acquire the write lock whilst holding a read lock
            this.lock.readLock().unlock();
        }

        this.lock.writeLock().lock();
        try {
            // Since the lock was unlocked momentarily, we need
            // to check again for a cached value
            if (this.hasValue) {
                return this.value;
            }

            // call the supplier and set the cached value
            this.value = this.supplier.get();
            return this.value;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    public NullableOptional<T> getIfPresent() {
        this.lock.readLock().lock();
        try {
            return this.hasValue ? NullableOptional.of(this.value) : NullableOptional.empty();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    public void invalidate() {
        this.lock.writeLock().lock();
        try {
            this.hasValue = false;
            this.value = null;
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}
