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

package me.lucko.helper.utils;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import javax.annotation.Nonnull;

public final class CommandMapUtil {

    // Cached CommandMap instance
    private static CommandMap commandMap = null;

    private static Constructor<?> commandConstructor;
    private static Field owningPluginField;
    static {
        try {
            commandConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            commandConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            owningPluginField = PluginCommand.class.getDeclaredField("owningPlugin");
            owningPluginField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers a CommandExecutor with the server
     *
     * @param command the command instance
     * @param aliases the command aliases
     * @param <T> the command executor class type
     * @return the command executor
     */
    @Nonnull
    public static <T extends CommandExecutor> T registerCommand(@Nonnull Plugin plugin, @Nonnull T command, @Nonnull String... aliases) {
        Preconditions.checkArgument(aliases.length != 0, "No aliases");
        for (String alias : aliases) {
            PluginCommand cmd = Bukkit.getServer().getPluginCommand(alias);
            if (cmd == null) {
                try {
                    cmd = (PluginCommand) commandConstructor.newInstance(alias, plugin);
                } catch (Exception ex) {
                    throw new RuntimeException("Could not register command: " + alias);
                }

                // Get the command map to register the command to
                if (commandMap == null) {
                    try {
                        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
                        Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
                        commandMapField.setAccessible(true);
                        commandMap = (CommandMap) commandMapField.get(pluginManager);
                    } catch (Exception ex) {
                        throw new RuntimeException("Could not register command: " + alias);
                    }
                }

                commandMap.register(plugin.getDescription().getName(), cmd);
            } else {
                // we may need to change the owningPlugin, since this was already registered
                try {
                    owningPluginField.set(cmd, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            cmd.setExecutor(command);
            if (command instanceof TabCompleter) {
                cmd.setTabCompleter((TabCompleter) command);
            } else {
                cmd.setTabCompleter(null);
            }
        }
        return command;
    }

    private CommandMapUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
