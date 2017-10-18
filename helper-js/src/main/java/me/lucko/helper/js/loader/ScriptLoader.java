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

import me.lucko.helper.terminable.Terminable;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * An object capable of loadings scripts and monitoring them for updates.
 */
public interface ScriptLoader extends Terminable {

    /**
     * Loads and watches a script file
     *
     * @param files the file to watch
     */
    default void watch(@Nonnull String... files) {
        this.watchAll(Arrays.asList(files));
    }

    /**
     * Loads and watches a collection of script files
     *
     * @param files the files to watch
     */
    void watchAll(@Nonnull Collection<String> files);

    /**
     * Unloads a script file
     *
     * @param files the file to unwatch
     */
    default void unwatch(@Nonnull String... files) {
        this.unwatchAll(Arrays.asList(files));
    }

    /**
     * Unloads a collection of script files
     *
     * @param files the files to unwatch
     */
    void unwatchAll(@Nonnull Collection<String> files);

    /**
     * Gets the root directory of this loader
     *
     * @return the root dir
     */
    File getDirectory();

}
