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

package me.lucko.helper.bucket;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

final class CycleImpl<E> implements Cycle<E> {

    /**
     * The list that backs this instance
     */
    private final List<E> objects;

    /**
     * The number of elements in the cycle
     */
    private final int size;

    /**
     * The current position of the cursor
     */
    private AtomicInteger cursor = new AtomicInteger(0);

    CycleImpl(@Nonnull List<E> objects) {
        if (objects == null || objects.isEmpty()) {
            throw new IllegalArgumentException("List of objects cannot be null/empty.");
        }
        this.objects = ImmutableList.copyOf(objects);
        this.size = this.objects.size();
    }

    private CycleImpl(CycleImpl<E> other) {
        this.objects = other.objects;
        this.size = other.size;
    }

    @Override
    public int cursor() {
        return this.cursor.get();
    }

    @Override
    public void setCursor(int index) {
        if (index < 0 || index >= this.size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
        }
        this.cursor.set(index);
    }

    @Nonnull
    @Override
    public E current() {
        return this.objects.get(cursor());
    }

    @Nonnull
    @Override
    public E next() {
        return this.objects.get(this.cursor.updateAndGet(i -> {
            int n = i + 1;
            if (n >= this.size) {
                return 0;
            }
            return n;
        }));
    }

    @Nonnull
    @Override
    public E previous() {
        return this.objects.get(this.cursor.updateAndGet(i -> {
            if (i == 0) {
                return this.size - 1;
            }
            return i - 1;
        }));
    }

    @Override
    public int nextPosition() {
        int n = this.cursor.get() + 1;
        if (n >= this.size) {
            return 0;
        }
        return n;
    }

    @Override
    public int previousPosition() {
        int i = this.cursor.get();
        if (i == 0) {
            return this.size - 1;
        }
        return i - 1;
    }

    @Nonnull
    @Override
    public E peekNext() {
        return this.objects.get(nextPosition());
    }

    @Nonnull
    @Override
    public E peekPrevious() {
        return this.objects.get(previousPosition());
    }

    @Nonnull
    @Override
    public List<E> getBacking() {
        return this.objects;
    }

    @Override
    public Cycle<E> copy() {
        return new CycleImpl<>(this);
    }
}
