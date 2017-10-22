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

package me.lucko.helper.js.loader;

import me.lucko.helper.js.script.Script;
import me.lucko.helper.terminable.Terminable;

import java.nio.file.Path;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A registry of {@link Script}s
 */
public interface ScriptRegistry extends Terminable {

    @Nonnull
    static ScriptRegistry create() {
        return new SimpleScriptRegistry();
    }

    /**
     * Registers a script
     *
     * @param script the script to register
     */
    void register(@Nonnull Script script);

    /**
     * Unregisters a script
     *
     * @param script the script to unregister
     */
    void unregister(@Nonnull Script script);

    /**
     * Gets a script by path
     *
     * @param path the path
     * @return a script for the file, or null
     */
    @Nullable
    Script getScript(@Nonnull Path path);

    /**
     * Gets all scripts known to this registry
     *
     * @return the scripts
     */
    @Nonnull
    Map<Path, Script> getAll();

}
