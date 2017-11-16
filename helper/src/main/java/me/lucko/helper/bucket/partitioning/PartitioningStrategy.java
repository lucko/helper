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

package me.lucko.helper.bucket.partitioning;

import me.lucko.helper.bucket.Bucket;

/**
 * A function which determines the position of an object within a {@link Bucket}.
 *
 * <p>Functions will not necessarily return consistent results for subsequent
 * calls using the same parameters, as their behaviour usually depends heavily on
 * current bucket state.</p>
 *
 * @param <T> the object type
 */
@FunctionalInterface
public interface PartitioningStrategy<T> {

    /**
     * Calculates the index of the partition to use for the object.
     *
     * <p>The index must be within range of the buckets size.</p>
     *
     * @param object the object
     * @param bucket the bucket
     * @return the index
     */
    int allocate(T object, Bucket<T> bucket);

}
