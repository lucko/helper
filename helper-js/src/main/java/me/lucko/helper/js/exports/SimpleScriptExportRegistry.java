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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.annotation.Nonnull;

class SimpleScriptExportRegistry implements ScriptExportRegistry, Function<String, ScriptExport<?>> {
    private final Map<String, ScriptExport<?>> handles = new ConcurrentHashMap<>();

    @Override
    public ScriptExport<?> apply(String s) {
        return new SimpleScriptExport<>(s);
    }

    @Nonnull
    @Override
    public <T> ScriptExport<T> handle(@Nonnull String key) {
        //noinspection unchecked
        return (ScriptExport<T>) handles.computeIfAbsent(key.toLowerCase(), this);
    }

    @Override
    public boolean has(@Nonnull String key) {
        ScriptExport<?> handle = handles.get(key.toLowerCase());
        return handle != null && handle.hasValue();
    }

    @Override
    public void remove(@Nonnull String key) {
        handles.remove(key.toLowerCase());
    }

    @Nonnull
    @Override
    public Collection<ScriptExport<?>> getExports() {
        return Collections.unmodifiableCollection(handles.values());
    }
}
