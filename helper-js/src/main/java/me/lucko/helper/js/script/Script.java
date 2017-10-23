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

package me.lucko.helper.js.script;

import me.lucko.helper.js.bindings.SystemScriptBindings;
import me.lucko.helper.terminable.Terminable;

import java.nio.file.Path;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Represents a Script
 */
public interface Script extends Terminable, Runnable {

    /**
     * Gets the name of the script
     *
     * @return the script name
     */
    @Nonnull
    String getName();

    /**
     * Gets the file this script instance was created from
     *
     * <p>The returned path is relative to the loader directory.</p>
     *
     * @return the file path
     */
    @Nonnull
    Path getPath();

    /**
     * Gets the initial bindings used by this script
     *
     * @return the scripts bindings
     */
    @Nonnull
    SystemScriptBindings getBindings();

    /**
     * Gets the scripts logger instance
     *
     * @return the script logger
     */
    @Nonnull
    ScriptLogger getLogger();

    /**
     * Gets the other scripts depended on by this script.
     *
     * @return this scripts dependencies
     */
    Set<Path> getDependencies();

    /**
     * Marks that this script depends on another script.
     *
     * @param path the other script
     */
    void depend(@Nonnull String path);

    /**
     * Marks that this script depends on another script.
     *
     * @param path the other script
     */
    void depend(@Nonnull Path path);

}
