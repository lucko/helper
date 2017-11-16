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
 * A {@link PartitioningStrategy} which allocates partitions without reference
 * to the object being added.
 */
@FunctionalInterface
public interface GenericPartitioningStrategy extends PartitioningStrategy<Object> {

    /**
     * Calculates the index of the partition to use for any given object.
     *
     * @param bucket the bucket
     * @return the index
     */
    int allocate(Bucket<?> bucket);

    /**
     * Casts this {@link GenericPartitioningStrategy} to a {@link PartitioningStrategy} of type T.
     *
     * @param <T> the type
     * @return a casted strategy
     */
    default <T> PartitioningStrategy<T> cast() {
        //noinspection unchecked
        return (PartitioningStrategy<T>) this;
    }

    @Override
    @Deprecated
    default int allocate(Object object, Bucket<Object> bucket) {
        return allocate(bucket);
    }

}
