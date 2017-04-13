/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me>
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

package me.lucko.helper.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class LoaderUtils {
    private static JavaPlugin plugin = null;

    public static synchronized JavaPlugin getPlugin() {
        if (plugin == null) {
            plugin = JavaPlugin.getProvidingPlugin(LoaderUtils.class);

            String thisClass = LoaderUtils.class.getName();
            thisClass = thisClass.substring(0, thisClass.length() - ".utils.LoaderUtils".length());
            Bukkit.getLogger().info("[helper] helper (" + thisClass + ") bound to plugin " + plugin.getName() + " - " + plugin.getClass().getName());
        }
        return plugin;
    }

    private LoaderUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
