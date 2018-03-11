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

package me.lucko.helper.gson.converter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import javax.annotation.Nullable;

@NonnullByDefault
class ImmutableGsonConverter extends AbstractGsonConverter<ImmutableMap<String, Object>, ImmutableList<Object>, ImmutableSet<Object>> {
    public static final ImmutableGsonConverter INSTANCE = new ImmutableGsonConverter();

    private ImmutableGsonConverter() {

    }

    @Override
    protected MapBuilder<ImmutableMap<String, Object>, String, Object> newMapBuilder() {
        return new ImmutableMapBuilder<>();
    }

    @Override
    protected ListBuilder<ImmutableList<Object>, Object> newListBuilder() {
        return new ImmutableListBuilder<>();
    }

    @Override
    protected SetBuilder<ImmutableSet<Object>, Object> newSetBuilder() {
        return new ImmutableSetBuilder<>();
    }

    private static final class ImmutableMapBuilder<K, V> implements MapBuilder<ImmutableMap<K, V>, K, V> {
        private final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();

        @Override
        public void put(@Nullable K key, @Nullable V value) {
            if (key == null || value == null) {
                return;
            }
            this.builder.put(key, value);
        }

        @Override
        public ImmutableMap<K, V> build() {
            return this.builder.build();
        }
    }

    private static final class ImmutableListBuilder<E> implements ListBuilder<ImmutableList<E>, E> {
        private final ImmutableList.Builder<E> builder = ImmutableList.builder();

        @Override
        public void add(@Nullable E element) {
            if (element == null) {
                return;
            }
            this.builder.add(element);
        }

        @Override
        public ImmutableList<E> build() {
            return this.builder.build();
        }
    }

    private static final class ImmutableSetBuilder<E> implements SetBuilder<ImmutableSet<E>, E> {
        private final ImmutableSet.Builder<E> builder = ImmutableSet.builder();

        @Override
        public void add(@Nullable E element) {
            if (element == null) {
                return;
            }
            this.builder.add(element);
        }

        @Override
        public ImmutableSet<E> build() {
            return this.builder.build();
        }
    }

}
