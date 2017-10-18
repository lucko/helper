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

class SimpleScriptExport<T> implements ScriptExport<T> {
    private final String id;

    private T value;

    SimpleScriptExport(String id) {
        this.id = id;
    }

    @Nonnull
    @Override
    public String id() {
        return id;
    }

    @Nullable
    @Override
    public T get() {
        return value;
    }

    @Override
    public T get(T other) {
        if (value != null) {
            return value;
        } else {
            return value = other;
        }
    }

    @Override
    public T get(@Nonnull Supplier<T> other) {
        if (value != null) {
            return value;
        } else {
            return value = other.get();
        }
    }

    @Nonnull
    @Override
    public ScriptExport<T> set(T value) {
        this.value = value;
        return this;
    }

    @Nonnull
    @Override
    public ScriptExport<T> setIfAbsent(T value) {
        if (this.value == null) {
            this.value = value;
        }
        return this;
    }

    @Nonnull
    @Override
    public ScriptExport<T> setIfAbsent(@Nonnull Supplier<T> value) {
        if (this.value == null) {
            this.value = value.get();
        }
        return this;
    }

    @Override
    public boolean hasValue() {
        return value != null;
    }


}
