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

package me.lucko.helper.plugin;

import me.lucko.helper.terminable.TerminableConsumer;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface HelperPlugin extends Plugin, TerminableConsumer {

    /**
     * Register a listener with the server.
     *
     * <p>{@link me.lucko.helper.Events} should be used instead of this method in most cases.</p>
     *
     * @param listener the listener to register
     * @param <T> the listener class type
     * @return the listener
     */
    @Nonnull
    <T extends Listener> T registerListener(@Nonnull T listener);

    /**
     * Registers a CommandExecutor with the server
     *
     * @param command the command instance
     * @param aliases the command aliases
     * @param <T> the command executor class type
     * @return the command executor
     */
    @Nonnull
    default <T extends CommandExecutor> T registerCommand(@Nonnull T command, @Nonnull String... aliases) {
        return registerCommand(command, null, null, null, aliases);
    }

    /**
     * Registers a CommandExecutor with the server
     *
     * @param command the command instance
     * @param permission the command permission
     * @param permissionMessage the message sent when the sender doesn't the required permission
     * @param description the command description
     * @param aliases the command aliases
     * @param <T> the command executor class type
     * @return the command executor
     */
    @Nonnull
    <T extends CommandExecutor> T registerCommand(@Nonnull T command, String permission, String permissionMessage, String description, @Nonnull String... aliases);

    /**
     * Gets a service provided by the ServiceManager
     *
     * @param service the service class
     * @param <T> the class type
     * @return the service
     */
    @Nonnull
    <T> T getService(@Nonnull Class<T> service);

    /**
     * Provides a service to the ServiceManager, bound to this plugin
     *
     * @param clazz the service class
     * @param instance the instance
     * @param priority the priority to register the service at
     * @param <T> the service class type
     * @return the instance
     */
    @Nonnull
    <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance, @Nonnull ServicePriority priority);

    /**
     * Provides a service to the ServiceManager, bound to this plugin at {@link ServicePriority#Normal}.
     *
     * @param clazz the service class
     * @param instance the instance
     * @param <T> the service class type
     * @return the instance
     */
    @Nonnull
    <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance);

    /**
     * Gets if a given plugin is enabled.
     *
     * @param name the name of the plugin
     * @return if the plugin is enabled
     */
    boolean isPluginPresent(@Nonnull String name);

    /**
     * Gets a plugin instance for the given plugin name
     *
     * @param name the name of the plugin
     * @param pluginClass the main plugin class
     * @param <T> the main class type
     * @return the plugin
     */
    @Nullable
    <T> T getPlugin(@Nonnull String name, @Nonnull Class<T> pluginClass);

    /**
     * Gets a bundled file from the plugins resource folder.
     *
     * <p>If the file is not present, a version of it it copied from the jar.</p>
     *
     * @param name the name of the file
     * @return the file
     */
    @Nonnull
    File getBundledFile(@Nonnull String name);

    /**
     * Loads a config file from a file name.
     *
     * <p>Behaves in the same was as {@link #getBundledFile(String)} when the file is not present.</p>
     *
     * @param file the name of the file
     * @return the config instance
     */
    @Nonnull
    YamlConfiguration loadConfig(@Nonnull String file);

    /**
     * Loads a config file from a file name.
     *
     * <p>Behaves in the same was as {@link #getBundledFile(String)} when the file is not present.</p>
     *
     * @param file the name of the file
     * @return the config instance
     */
    @Nonnull
    ConfigurationNode loadConfigNode(@Nonnull String file);

    /**
     * Populates a config object.
     *
     * @param file the name of the file
     * @param configObject the config object
     * @param <T> the config object type
     */
    @Nonnull
    <T> T setupConfig(@Nonnull String file, @Nonnull T configObject);

    /**
     * Gets the plugin's class loader
     *
     * @return the class loader
     */
    @Nonnull
    ClassLoader getClassloader();

}
