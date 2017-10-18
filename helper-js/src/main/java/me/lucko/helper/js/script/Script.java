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

import java.io.File;

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
     * @return the file
     */
    @Nonnull
    File getFile();

    /**
     * Gets the most recent time when this script or a dependency was load modified.
     *
     * @return the most recent modification time
     */
    long getLastModified();

    /**
     * Gets the most recent time when a dependency of this script was loaded.
     *
     * @return the latest dependency load time
     */
    long getLatestDependencyLoad();

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
     * Marks that this script depends on another script.
     *
     * @param file the other script
     */
    default void depend(@Nonnull String file) {
        depend(new File(file));
    }

    /**
     * Marks that this script depends on another script.
     *
     * @param file the other script
     */
    void depend(@Nonnull File file);

}
