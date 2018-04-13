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

package me.lucko.helper.metadata;

import java.lang.ref.SoftReference;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * Represents a value wrapped in a {@link SoftReference}
 *
 * @param <T> the wrapped value type
 */
public final class SoftValue<T> implements TransientValue<T> {

    public static <T> SoftValue<T> of(T value) {
        Objects.requireNonNull(value, "value");
        return new SoftValue<>(value);
    }

    public static <T> Supplier<SoftValue<T>> supplied(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier");

        return () -> {
            T value = supplier.get();
            Objects.requireNonNull(value, "value");

            return new SoftValue<>(value);
        };
    }

    private final SoftReference<T> value;

    private SoftValue(T value) {
        this.value = new SoftReference<>(value);
    }

    @Nullable
    @Override
    public T getOrNull() {
        return this.value.get();
    }

    @Override
    public boolean shouldExpire() {
        return this.value.get() == null;
    }

}
