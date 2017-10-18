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

package me.lucko.helper.js.bindings;

import me.lucko.helper.js.exports.ScriptExportRegistry;
import me.lucko.helper.js.plugin.ScriptPlugin;

import javax.annotation.Nonnull;
import javax.script.Bindings;

/**
 * Represents the environment for a scripts execution.
 */
public interface SystemScriptBindings {

    /**
     * Appends this environment's variables to the given bindings.
     *
     * @param bindings the bindings to append to
     */
    void appendTo(@Nonnull Bindings bindings);

    /**
     * Gets the plugin which owns these bindings
     *
     * @return the plugin
     */
    @Nonnull
    ScriptPlugin getPlugin();

    /**
     * Gets the exports used by this binding
     *
     * @return the exports
     */
    @Nonnull
    ScriptExportRegistry getExports();

}
