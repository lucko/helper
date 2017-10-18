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

package me.lucko.helper.js.exports;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wraps a specific export in a {@link ScriptExportRegistry}.
 *
 * @param <T> the export type
 */
public interface ScriptExport<T> {

    /**
     * Gets the ID of the export
     *
     * @return the id
     */
    @Nonnull
    String id();

    /**
     * Gets the value currently in the export
     *
     * @return the current value
     */
    @Nullable
    T get();

    /**
     * Either gets the value or sets it to another, and returns the same value
     *
     * @param other the other value
     * @return the value
     */
    T get(T other);

    /**
     * Either gets the value or sets it to another, and returns the same value
     *
     * @param other the other value
     * @return the value
     */
    T get(@Nonnull Supplier<T> other);

    /**
     * Sets the value of the export
     *
     * @param value the value to set
     */
    @Nonnull
    ScriptExport<T> set(T value);

    /**
     * Sets the value of the export if a value isn't already present, then return the handle
     *
     * @param value the value to set if absent
     * @return this handle
     */
    @Nonnull
    ScriptExport<T> setIfAbsent(T value);

    /**
     * Sets the value of the export if a value isn't already present, then return the handle
     *
     * @param value the value to set if absent
     * @return this handle
     */
    @Nonnull
    ScriptExport<T> setIfAbsent(@Nonnull Supplier<T> value);

    /**
     * Gets if this handle has a value
     *
     * @return true if this handle has a value
     */
    boolean hasValue();

}
