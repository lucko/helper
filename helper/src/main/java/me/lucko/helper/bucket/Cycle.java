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

import java.util.List;

import javax.annotation.Nonnull;

/**
 * An infinite cycle of elements.
 *
 * @param <E> the element type
 */
public interface Cycle<E> {

    /**
     * Creates a new cycle of elements.
     *
     * <p>Changes to the supplying list are not reflected in the cycle.</p>
     *
     * @param objects the objects to form the cycle from
     * @param <E> the element type
     * @return the cycle
     */
    @Nonnull
    static <E> Cycle<E> of(@Nonnull List<E> objects) {
        //noinspection deprecation
        return new CycleImpl<>(objects);
    }

    /**
     * Gets the current position of the cursor, as as index relating to a
     * position in the backing list.
     *
     * @return the cursor position
     */
    int cursor();

    /**
     * Sets the cursor to a given index
     *
     * @param index the index to set the cursor to
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    void setCursor(int index);

    /**
     * Gets the current element
     *
     * @return the current element
     */
    @Nonnull
    E current();

    /**
     * Advances the cursor, and returns the next element.
     *
     * @return the next element
     */
    @Nonnull
    E next();

    /**
     * Retreats the counter, and returns the previous element.
     *
     * @return the previous element
     */
    @Nonnull
    E previous();

    /**
     * Returns the index of the next position in the cycle.
     *
     * @return the next position
     */
    int nextPosition();

    /**
     * Returns the index of the previous position in the cycle.
     *
     * @return the previous position
     */
    int previousPosition();

    /**
     * Returns the next element without advancing the cursor.
     *
     * @return the next element
     */
    @Nonnull
    E peekNext();

    /**
     * Returns the previous element without retreating the cursor.
     *
     * @return the previous element
     */
    @Nonnull
    E peekPrevious();

    /**
     * Gets the list currently backing this cycle
     *
     * <p>The returned list is immutable.</p>
     *
     * @return the backing list
     */
    @Nonnull
    List<E> getBacking();

    /**
     * Creates a copy of this cycle.
     *
     * <p>The returned cycle will contain the same elements as this cycle, but
     * its cursor will be reset to zero.</p>
     *
     * @return a copy of this cycle
     */
    Cycle<E> copy();

}
