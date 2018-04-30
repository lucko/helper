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

package me.lucko.helper.utils;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Immutable implementations of {@link Collector} using Guava's immutable collections.
 */
@NonnullByDefault
public final class ImmutableCollectors {

    private static final Collector<Object, ImmutableList.Builder<Object>, ImmutableList<Object>> LIST = Collector.of(
            ImmutableList.Builder::new,
            ImmutableList.Builder::add,
            (l, r) -> l.addAll(r.build()),
            ImmutableList.Builder::build
    );

    private static final Collector<Object, ImmutableSet.Builder<Object>, ImmutableSet<Object>> SET = Collector.of(
            ImmutableSet.Builder::new,
            ImmutableSet.Builder::add,
            (l, r) -> l.addAll(r.build()),
            ImmutableSet.Builder::build
    );

    private static final Collector<Map.Entry<Object, Object>, ImmutableMap.Builder<Object, Object>, ImmutableMap<Object, Object>> MAP = Collector.of(
            ImmutableMap.Builder::new,
            (r, t) -> r.put(t.getKey(), t.getValue()),
            (l, r) -> l.putAll(r.build()),
            ImmutableMap.Builder::build
    );

    private static final Collector<Map.Entry<Object, Object>, ImmutableBiMap.Builder<Object, Object>, ImmutableBiMap<Object, Object>> BIMAP = Collector.of(
            ImmutableBiMap.Builder::new,
            (r, t) -> r.put(t.getKey(), t.getValue()),
            (l, r) -> l.putAll(r.build()),
            ImmutableBiMap.Builder::build
    );

    public static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> toList() {
        //noinspection unchecked
        return (Collector) LIST;
    }

    public static <T> Collector<T, ImmutableSet.Builder<T>, ImmutableSet<T>> toSet() {
        //noinspection unchecked
        return (Collector) SET;
    }

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> toMap() {
        //noinspection unchecked
        return (Collector) MAP;
    }

    public static <K, V> Collector<Map.Entry<? extends K, ? extends V>, ImmutableBiMap.Builder<K, V>, ImmutableBiMap<K, V>> toBiMap() {
        //noinspection unchecked
        return (Collector) BIMAP;
    }

    public static <E> Collector<E, ?, ImmutableSortedSet<E>> toSortedSet(Comparator<? super E> comparator) {
        return Collector.of(
                () -> new ImmutableSortedSet.Builder<E>(comparator),
                ImmutableSortedSet.Builder::add,
                (l, r) -> l.addAll(r.build()),
                ImmutableSortedSet.Builder::build
        );
    }

    public static <T, K, V> Collector<T, ImmutableMap.Builder<K, V>, ImmutableMap<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                ImmutableMap.Builder<K, V>::new,
                (r, t) -> r.put(keyMapper.apply(t), valueMapper.apply(t)),
                (l, r) -> l.putAll(r.build()),
                ImmutableMap.Builder::build
        );
    }

    public static <T, K, V> Collector<T, ?, ImmutableBiMap<K, V>> toBiMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                ImmutableBiMap.Builder<K, V>::new,
                (builder, input) -> builder.put(keyMapper.apply(input), valueMapper.apply(input)),
                (l, r) -> l.putAll(r.build()),
                ImmutableBiMap.Builder::build
        );
    }

    public static <T, K, V> Collector<T, ?, ImmutableSortedMap<K, V>> toSortedMap(Comparator<? super K> comparator, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Collector.of(
                () -> new ImmutableSortedMap.Builder<K, V>(comparator),
                (builder, input) -> builder.put(keyMapper.apply(input), valueMapper.apply(input)),
                (l, r) -> l.putAll(r.build()),
                ImmutableSortedMap.Builder::build,
                Collector.Characteristics.UNORDERED
        );
    }

    private ImmutableCollectors() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
