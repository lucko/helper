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

import me.lucko.helper.Commands;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.utils.Players;

import org.bukkit.plugin.Plugin;

import java.util.Comparator;

/**
 * Standalone plugin which provides the helper library at runtime for other plugins
 * on the server to use.
 */
@HelperImplementationPlugin
public final class StandalonePlugin extends ExtendedJavaPlugin {
    public StandalonePlugin() {
        getLogger().info("Initialized helper v" + getDescription().getVersion());
    }

    @Override
    protected void enable() {
        // provide an info command
        if (getConfig().getBoolean("info-command", true)) {
            Commands.create()
                    .handler(c -> LoaderUtils.getHelperImplementationPlugins().stream()
                            .sorted(Comparator.comparing(Plugin::getName))
                            .forEach(pl -> Players.msg(c.sender(), "&7[&6helper&7] &7Running &6" + pl.getName() + " v" + pl.getDescription().getVersion() + "&7.")))
                    .register("helper");
        }
    }
}
