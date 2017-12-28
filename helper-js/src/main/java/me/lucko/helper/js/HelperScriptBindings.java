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

package me.lucko.helper.js;

import com.google.common.reflect.TypeToken;

import me.lucko.helper.js.bindings.GeneralScriptBindings;
import me.lucko.helper.js.bindings.SystemScriptBindings;
import me.lucko.helper.js.exports.ScriptExportRegistry;
import me.lucko.helper.js.plugin.ScriptPlugin;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.SchemeMapping;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.utils.Color;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.script.Bindings;

public class HelperScriptBindings implements SystemScriptBindings {
    private static final Method GET_CLASSLOADER_METHOD;
    private static final Method GET_PACKAGES_METHOD;

    static {
        Method getClassLoader;
        Method getPackages;

        try {
            getClassLoader = JavaPlugin.class.getDeclaredMethod("getClassLoader");
            getClassLoader.setAccessible(true);

            getPackages = ClassLoader.class.getDeclaredMethod("getPackages");
            getPackages.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        GET_CLASSLOADER_METHOD = getClassLoader;
        GET_PACKAGES_METHOD = getPackages;
    }

    private final ScriptPlugin plugin;
    private final ScriptExportRegistry exports;

    public HelperScriptBindings(HelperJsPlugin plugin) {
        this.plugin = plugin;
        this.exports = ScriptExportRegistry.create();
    }

    @Nonnull
    @Override
    public ScriptPlugin getPlugin() {
        return plugin;
    }

    @Nonnull
    @Override
    public ScriptExportRegistry getExports() {
        return exports;
    }

    @Override
    public void appendTo(@Nonnull Bindings bindings) {

        // provide exports to access the exports registry & core server classes
        bindings.put("exports", exports);
        bindings.put("server", Bukkit.getServer());
        bindings.put("plugin", plugin);
        bindings.put("services", Bukkit.getServicesManager());

        // some util functions
        bindings.put("colorize", (Function<Object, String>) HelperScriptBindings::colorize);
        bindings.put("newMetadataKey", (Function<Object, MetadataKey>) HelperScriptBindings::newMetadataKey);
        bindings.put("newEmptyScheme", (Supplier<MenuScheme>) HelperScriptBindings::newScheme);
        bindings.put("newScheme", (Function<SchemeMapping, MenuScheme>) HelperScriptBindings::newScheme);

        // some general functions for working with java collections in js
        GeneralScriptBindings.appendTo(bindings);

        // provide hook into the resolvePackageWildcard method below, used by the importWildcardPackage function
        bindings.put("resolvePackageWildcard", (Function<String, List<String>>) HelperScriptBindings::resolvePackageWildcard);
    }

    private static String colorize(Object object) {
        return Color.colorize(object.toString());
    }

    private static <T> MetadataKey<T> newMetadataKey(Object id) {
        return MetadataKey.create(id.toString(), new TypeToken<T>(){});
    }

    private static MenuScheme newScheme() {
        return new MenuScheme();
    }

    private static MenuScheme newScheme(SchemeMapping mapping) {
        return new MenuScheme(mapping);
    }

    private static List<String> resolvePackageWildcard(String name) {
        if (name.endsWith(".*")) {
            name = name.substring(0, name.length() - 2);
        }

        Set<String> allPackages;

        try {
            allPackages = getAllPackages();
        } catch (Exception e) {
            return new ArrayList<>();
        }

        Set<String> matches = new HashSet<>();
        matches.add(name);
        for (String p : allPackages) {
            if (p.startsWith(name)) {
                matches.add(p);
            }
        }

        return new ArrayList<>(matches);
    }

    private static Set<String> getAllPackages() throws Exception {
        Plugin[] plugins = Bukkit.getServer().getPluginManager().getPlugins();

        Set<String> names = new HashSet<>();
        Set<ClassLoader> classLoaders = Collections.newSetFromMap(new IdentityHashMap<>(plugins.length + 3));

        // inspect all plugin classloaders
        for (Plugin plugin : plugins) {
            ClassLoader classLoader = (ClassLoader) GET_CLASSLOADER_METHOD.invoke(plugin);
            classLoaders.add(classLoader);
        }

        // catch all other classloaders
        classLoaders.add(ClassLoader.getSystemClassLoader());
        classLoaders.add(Thread.currentThread().getContextClassLoader());
        classLoaders.add(Bukkit.getServer().getClass().getClassLoader());

        for (ClassLoader classLoader : classLoaders) {
            Package[] packages = (Package[]) GET_PACKAGES_METHOD.invoke(classLoader);
            for (Package p : packages) {
                names.add(p.getName());
            }
        }

        return names;
    }

}
