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

import com.google.common.collect.Range;
import me.lucko.helper.menu.Item;

import java.util.Objects;
import java.util.function.IntFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implements {@link SchemeMapping} using a function.
 */
public final class FunctionalSchemeMapping implements SchemeMapping {
    private final IntFunction<Item> function;
    private final Range<Integer> validRange;

    public static @Nonnull SchemeMapping of(@Nonnull IntFunction<Item> function, @Nonnull Range<Integer> validRange) {
        return new FunctionalSchemeMapping(function, validRange);
    }

    private FunctionalSchemeMapping(@Nonnull IntFunction<Item> function, @Nonnull Range<Integer> validRange) {
        this.function = Objects.requireNonNull(function, "function");
        this.validRange = Objects.requireNonNull(validRange, "validRange");
    }

    @Override
    @Nullable
    public Item getNullable(int key) {
        if (!hasMappingFor(key)) {
            return null;
        }
        return function.apply(key);
    }

    @Override
    public boolean hasMappingFor(int key) {
        return this.validRange.contains(key);
    }

    @Nonnull
    @Override
    public SchemeMapping copy() {
        return this; // no need to make a copy, the backing data is immutable
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionalSchemeMapping that = (FunctionalSchemeMapping) o;
        return function.equals(that.function) &&
                validRange.equals(that.validRange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, validRange);
    }
}
