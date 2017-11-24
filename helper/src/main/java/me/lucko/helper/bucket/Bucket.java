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

import me.lucko.helper.bucket.partitioning.PartitioningStrategy;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * A bucket is an extension of {@link Set}, which allows contained elements
 * to be separated into parts by a {@link PartitioningStrategy}.
 *
 * <p>The performance of {@link Bucket} should be largely similar to the performance
 * of the underlying {@link Set}. Elements are stored twice - once in a set
 * containing all elements in the bucket, and again in a set representing each partition.</p>
 *
 * @param <E> the element type
 */
public interface Bucket<E> extends Set<E> {

    /**
     * Gets the number of partitions used to form this bucket.
     *
     * @return the number of partitions in this bucket
     */
    int getPartitionCount();

    /**
     * Gets the partition with the given index value
     *
     * @param i the partition index
     * @return the partition
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= getPartitionCount()</tt>)
     */
    @Nonnull
    BucketPartition<E> getPartition(int i);

    /**
     * Gets the partitions which form this bucket.
     *
     * @return the partitions within the bucket
     */
    @Nonnull
    List<BucketPartition<E>> getPartitions();

    /**
     * Returns a cycle instance unique to this bucket.
     *
     * <p>This method is provided as a utility for operating deterministically on
     * all elements within the bucket over a period of time.</p>
     *
     * <p>The same cycle instance is returned for each bucket.</p>
     *
     * @return a cycle of partitions
     */
    @Nonnull
    Cycle<BucketPartition<E>> asCycle();

}
