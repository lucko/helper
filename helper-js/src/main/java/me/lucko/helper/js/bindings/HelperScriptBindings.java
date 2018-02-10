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

import com.google.common.reflect.TypeToken;

import me.lucko.helper.js.HelperJsPlugin;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.SchemeMapping;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.utils.Color;
import me.lucko.scriptcontroller.bindings.BindingsBuilder;
import me.lucko.scriptcontroller.bindings.BindingsSupplier;

import org.bukkit.Bukkit;

import java.util.function.Function;
import java.util.function.Supplier;

public class HelperScriptBindings implements BindingsSupplier {
    private final HelperJsPlugin plugin;

    public HelperScriptBindings(HelperJsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void accumulateTo(BindingsBuilder bindings) {

        // provide exports to access the exports registry & core server classes
        bindings.put("server", Bukkit.getServer());
        bindings.put("plugin", plugin);
        bindings.put("services", Bukkit.getServicesManager());

        // some util functions
        bindings.put("colorize", (Function<Object, String>) HelperScriptBindings::colorize);
        bindings.put("newMetadataKey", (Function<Object, MetadataKey>) HelperScriptBindings::newMetadataKey);
        bindings.put("newEmptyScheme", (Supplier<MenuScheme>) HelperScriptBindings::newScheme);
        bindings.put("newScheme", (Function<SchemeMapping, MenuScheme>) HelperScriptBindings::newScheme);
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

}
