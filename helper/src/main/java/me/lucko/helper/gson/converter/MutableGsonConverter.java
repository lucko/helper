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

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nullable;

@NonnullByDefault
class MutableGsonConverter extends AbstractGsonConverter<HashMap<String, Object>, ArrayList<Object>, HashSet<Object>> {
    public static final MutableGsonConverter INSTANCE = new MutableGsonConverter();

    private MutableGsonConverter() {

    }

    @Override
    protected MapBuilder<HashMap<String, Object>, String, Object> newMapBuilder() {
        return new MutableMapBuilder<>();
    }

    @Override
    protected ListBuilder<ArrayList<Object>, Object> newListBuilder() {
        return new MutableListBuilder<>();
    }

    @Override
    protected SetBuilder<HashSet<Object>, Object> newSetBuilder() {
        return new MutableSetBuilder<>();
    }

    private static final class MutableMapBuilder<K, V> implements MapBuilder<HashMap<K, V>, K, V> {
        private final HashMap<K, V> builder = new HashMap<>();

        @Override
        public void put(@Nullable K key, @Nullable V value) {
            this.builder.put(key, value);
        }

        @Override
        public HashMap<K, V> build() {
            return this.builder;
        }
    }

    private static final class MutableListBuilder<E> implements ListBuilder<ArrayList<E>, E> {
        private final ArrayList<E> builder = new ArrayList<>();

        @Override
        public void add(@Nullable E element) {
            this.builder.add(element);
        }

        @Override
        public ArrayList<E> build() {
            return this.builder;
        }
    }

    private static final class MutableSetBuilder<E> implements SetBuilder<HashSet<E>, E> {
        private final HashSet<E> builder = new HashSet<>();

        @Override
        public void add(@Nullable E element) {
            this.builder.add(element);
        }

        @Override
        public HashSet<E> build() {
            return this.builder;
        }
    }
}
