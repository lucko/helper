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

import me.lucko.helper.js.bindings.SystemScriptBindings;

import java.util.Collection;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A registry for script object state.
 *
 * <p>All scripts are stateless, so data which needs to persist between reloads must
 * be saved to the export registry.</p>
 *
 * <p>The same export registry is shared between scripts using the same {@link SystemScriptBindings}.</p>
 */
public interface ScriptExportRegistry {

    @Nonnull
    static ScriptExportRegistry create() {
        return new SimpleScriptExportRegistry();
    }

    /**
     * Produces and returns a script export handle
     *
     * @param key the key of the handle
     * @param <T> the export type
     * @return the handle
     */
    @Nonnull
    <T> ScriptExport<T> handle(@Nonnull String key);

    /**
     * Produces and returns a script export handle
     *
     * @param key the key of the handle
     * @param defaultValue the value to populate the handle with if empty
     * @param <T> the export type
     * @return the handle
     */
    @Nonnull
    default <T> ScriptExport<T> handle(@Nonnull String key, T defaultValue) {
        ScriptExport<T> handle = handle(key);
        return handle.setIfAbsent(defaultValue);
    }

    /**
     * Produces and returns a script export handle
     *
     * @param key the key of the handle
     * @param defaultValue the value to populate the handle with if empty
     * @param <T> the export type
     * @return the handle
     */
    @Nonnull
    default <T> ScriptExport<T> handle(@Nonnull String key, Supplier<T> defaultValue) {
        ScriptExport<T> handle = handle(key);
        return handle.setIfAbsent(defaultValue);
    }


    /**
     * Gets an export handle, if present
     *
     * @param key the key of the handle
     * @param <T> the export type
     * @return the export, or null
     */
    @Nullable
    default <T> T get(@Nonnull String key) {
        ScriptExport<T> handle = handle(key);
        return handle.get();
    }

    /**
     * Gets or initializes a new export, and sets it to the given value if empty
     *
     * @param key the handle key
     * @param other the value to populate the handle with if empty
     * @param <T> the export type
     * @return the export
     */
    default <T> T get(@Nonnull String key, T other) {
        ScriptExport<T> handle = handle(key);
        return handle.get(other);
    }

    /**
     * Gets or initializes a new export, and sets it to the given value if empty
     *
     * @param key the handle key
     * @param other the value to populate the handle with if empty
     * @param <T> the export type
     * @return the export
     */
    default <T> T get(@Nonnull String key, @Nonnull Supplier<T> other) {
        ScriptExport<T> handle = handle(key);
        return handle.get(other);
    }

    /**
     * Gets or initializes a new export, and sets it to the given value
     *
     * @param key the handle key
     * @param value the value to set
     * @param <T> the export type
     * @return the handle
     */
    @Nonnull
    default <T> ScriptExport<T> set(@Nonnull String key, T value) {
        ScriptExport<T> handle = handle(key);
        return handle.set(value);
    }

    /**
     * Gets if this export registry has a certain export
     *
     * @param key the get of the export
     * @return if the registry has an export
     */
    boolean has(@Nonnull String key);

    /**
     * Removes an export from this registry.
     *
     * @param key the export key
     */
    void remove(@Nonnull String key);

    /**
     * Gets all exports in this registry
     *
     * @return the exports in this registry
     */
    @Nonnull
    Collection<ScriptExport<?>> getExports();

}
