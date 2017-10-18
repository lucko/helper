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

package me.lucko.helper.js.plugin;

import me.lucko.helper.js.loader.SystemScriptLoader;

import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

import javax.annotation.Nonnull;

/**
 * Represents a plugin which runs scripts
 */
public interface ScriptPlugin extends Plugin {

    /**
     * Gets the plugins script loader
     *
     * @return the script loader
     */
    @Nonnull
    SystemScriptLoader getScriptLoader();

    /**
     * Get the plugin's classloader
     *
     * @return the plugins classloader
     */
    @Nonnull
    ClassLoader getPluginClassLoader();

    /**
     * Gets the plugin's logger
     *
     * @return the plugins logger
     */
    @Nonnull
    Logger getPluginLogger();

    /**
     * Gets the script header, to be applied to all scripts loaded via this plugin
     *
     * @return the script header
     */
    @Nonnull
    String getScriptHeader();

}
