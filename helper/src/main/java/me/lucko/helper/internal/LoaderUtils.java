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

package me.lucko.helper.internal;

import me.lucko.helper.Helper;
import me.lucko.helper.plugin.HelperPlugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Provides the instance which loaded the helper classes into the server
 */
public final class LoaderUtils {
    private static HelperPlugin plugin = null;
    private static Thread mainThread = null;

    @Nonnull
    public static synchronized HelperPlugin getPlugin() {
        if (plugin == null) {
            JavaPlugin pl = JavaPlugin.getProvidingPlugin(LoaderUtils.class);
            if (!(pl instanceof HelperPlugin)) {
                throw new IllegalStateException("helper providing plugin does not implement HelperPlugin: " + pl.getClass().getName());
            }
            plugin = (HelperPlugin) pl;

            String pkg = LoaderUtils.class.getPackage().getName();
            pkg = pkg.substring(0, pkg.length() - ".internal".length());

            Bukkit.getLogger().info("[helper] helper (" + pkg + ") bound to plugin " + plugin.getName() + " - " + plugin.getClass().getName());

            setup();
        }

        return plugin;
    }

    /**
     * To be used for testing only
     */
    public static synchronized void forceSetPlugin(HelperPlugin plugin) {
        LoaderUtils.plugin = plugin;
    }

    public static Set<Plugin> getHelperImplementationPlugins() {
        return Stream.concat(
                Stream.<Plugin>of(getPlugin()),
                Arrays.stream(Helper.plugins().getPlugins())
                        .filter(pl -> pl.getClass().isAnnotationPresent(HelperImplementationPlugin.class))
        ).collect(Collectors.toSet());
    }

    public static Set<HelperPlugin> getHelperPlugins() {
        return Stream.concat(
                Stream.of(getPlugin()),
                Arrays.stream(Helper.plugins().getPlugins())
                        .filter(pl -> pl instanceof HelperPlugin)
                        .map(pl -> (HelperPlugin) pl)
        ).collect(Collectors.toSet());
    }

    @Nonnull
    public static synchronized Thread getMainThread() {
        if (mainThread == null) {
            if (Bukkit.getServer().isPrimaryThread()) {
                mainThread = Thread.currentThread();
            }
        }
        return mainThread;
    }

    // performs an intial setup for global handlers
    private static void setup() {

        // cache main thread in this class
        getMainThread();
    }

    private LoaderUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
