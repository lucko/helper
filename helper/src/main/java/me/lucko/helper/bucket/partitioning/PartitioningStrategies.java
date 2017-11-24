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
import me.lucko.helper.bucket.BucketPartition;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Some standard partitioning strategies for use in {@link Bucket}s.
 */
public final class PartitioningStrategies {

    public static <T> PartitioningStrategy<T> random() {
        return Strategies.RANDOM.cast();
    }

    public static <T> PartitioningStrategy<T> lowestSize() {
        return Strategies.LOWEST_SIZE.cast();
    }

    public static <T> PartitioningStrategy<T> nextInCycle() {
        return Strategies.NEXT_IN_CYCLE.cast();
    }

    public static <T> PartitioningStrategy<T> previousInCycle() {
        return Strategies.PREVIOUS_IN_CYCLE.cast();
    }

    private enum Strategies implements GenericPartitioningStrategy {
        RANDOM {
            @Override
            public int allocate(Bucket<?> bucket) {
                return ThreadLocalRandom.current().nextInt(bucket.getPartitionCount());
            }
        },
        LOWEST_SIZE {
            @Override
            public int allocate(Bucket<?> bucket) {
                int index = -1;
                int lowestSize = Integer.MAX_VALUE;

                for (BucketPartition<?> partition : bucket.getPartitions()) {
                    int size = partition.size();
                    int i = partition.getPartitionIndex();

                    if (size == 0) {
                        return i;
                    }

                    if (size < lowestSize) {
                        lowestSize = size;
                        index = i;
                    }
                }

                if (index == -1) {
                    throw new AssertionError();
                }
                return index;
            }
        },
        NEXT_IN_CYCLE {
            @Override
            public int allocate(Bucket<?> bucket) {
                return bucket.asCycle().next().getPartitionIndex();
            }
        },
        PREVIOUS_IN_CYCLE {
            @Override
            public int allocate(Bucket<?> bucket) {
                return bucket.asCycle().previous().getPartitionIndex();
            }
        }
    }

    private PartitioningStrategies() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
