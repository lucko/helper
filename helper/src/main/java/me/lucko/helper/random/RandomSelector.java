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

package me.lucko.helper.random;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

/**
 * A tool to randomly select elements from collections.
 *
 * @param <E> the element type
 */
public interface RandomSelector<E> {

    /**
     * Creates a uniform selector which picks elements randomly.
     *
     * @param elements the elements to pick from
     * @param <E> the element type
     * @return the selector instance
     */
    static <E> RandomSelector<E> uniform(Collection<E> elements) {
        return RandomSelectorImpl.uniform(elements);
    }

    /**
     * Creates a weighted selector which picks elements according to the value of their {@link Weighted#getWeight()}.
     *
     * @param elements the elements to pick from
     * @param <E> the element type
     * @return the selector instance
     */
    static <E extends Weighted> RandomSelector<E> weighted(Collection<E> elements) {
        return weighted(elements, Weighted.WEIGHER);
    }

    /**
     * Creates a weighted selector which picks elements using their weight,
     * according to the weigher function.
     *
     * @param elements the elements to pick from
     * @param <E> the element type
     * @return the selector instance
     */
    static <E> RandomSelector<E> weighted(Collection<E> elements, Weigher<? super E> weigher) {
        return RandomSelectorImpl.weighted(elements, weigher);
    }

    /**
     * Randomly pick an element.
     *
     * @param random the random instance to use for selection
     * @return an element
     */
    E pick(Random random);

    /**
     * Randomly pick an element.
     *
     * @return an element
     */
    default E pick() {
        return pick(ThreadLocalRandom.current());
    }

    /**
     * Returns an effectively unlimited stream of random elements from this selector.
     *
     * @param random the random instance to use for selection
     * @return a stream of elements
     */
    Stream<E> stream(Random random);

    /**
     * Returns an effectively unlimited stream of random elements from this selector.
     *
     * @return a stream of elements
     */
    default Stream<E> stream() {
        return stream(ThreadLocalRandom.current());
    }
}