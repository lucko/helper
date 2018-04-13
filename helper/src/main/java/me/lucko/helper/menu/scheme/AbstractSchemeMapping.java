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

package me.lucko.helper.menu.scheme;

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.menu.Item;

import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements {@link SchemeMapping} using an immutable map.
 */
public class AbstractSchemeMapping implements SchemeMapping {
    private final Map<Integer, Item> mapping;

    @Nonnull
    public static SchemeMapping of(@Nonnull Map<Integer, Item> mapping) {
        return new AbstractSchemeMapping(mapping);
    }

    private AbstractSchemeMapping(@Nonnull Map<Integer, Item> mapping) {
        Objects.requireNonNull(mapping, "mapping");
        this.mapping = ImmutableMap.copyOf(mapping);
    }

    @Override
    @Nullable
    public Item getNullable(int key) {
        return this.mapping.get(key);
    }

    @Override
    public boolean hasMappingFor(int key) {
        return this.mapping.containsKey(key);
    }

    @Nonnull
    @Override
    public SchemeMapping copy() {
        return this; // no need to make a copy, the backing data is immutable
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractSchemeMapping && ((AbstractSchemeMapping) obj).mapping.equals(this.mapping);
    }

    @Override
    public int hashCode() {
        return this.mapping.hashCode();
    }
}
