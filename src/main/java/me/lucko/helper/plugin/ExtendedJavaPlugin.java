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

import com.google.common.base.Preconditions;

import me.lucko.helper.Scheduler;
import me.lucko.helper.terminable.CompositeTerminable;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableRegistry;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * An "extended" JavaPlugin instance, providing a built in {@link TerminableRegistry}, and methods to easily register
 * commands at runtime, and provide/retrieve services from the Bukkit ServiceManager.
 */
public abstract class ExtendedJavaPlugin extends JavaPlugin implements Consumer<Terminable> {
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

    // Cached CommandMap instance
    private CommandMap commandMap = null;

    private final TerminableRegistry terminableRegistry = TerminableRegistry.create();
    private boolean hasTask = false;

    private void setupTerminableCleanupTask() {
        synchronized (terminableRegistry) {
            if (!hasTask) {
                hasTask = true;

                Scheduler.runTaskRepeatingAsync(terminableRegistry::cleanup, 600L, 600L).register(terminableRegistry);
            }
        }
    }

    @Override
    public final void onDisable() {
        terminableRegistry.terminate();
    }

    /**
     * Register a listener with the server.
     *
     * <p>{@link me.lucko.helper.Events} should be used instead of this method in most cases.</p>
     *
     * @param listener the listener to register
     * @param <T> the listener class type
     * @return the listener
     */
    public <T extends Listener> T registerListener(T listener) {
        Preconditions.checkNotNull(listener, "listener");
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }

    /**
     * Registers a terminable with this plugins {@link TerminableRegistry}
     *
     * @param terminable the terminable to register
     */
    @Override
    public void accept(Terminable terminable) {
        registerTerminable(terminable);
    }

    /**
     * Registers a terminable with this plugins {@link TerminableRegistry}
     *
     * @param terminable the terminable to register
     * @return the terminable
     */
    public <T extends Terminable> T registerTerminable(T terminable) {
        setupTerminableCleanupTask();
        terminableRegistry.accept(terminable);
        return terminable;
    }

    /**
     * Binds a {@link CompositeTerminable} to this plugins {@link TerminableRegistry}
     *
     * @param terminable the composite terminable to bind
     * @param <T> the terminable class type
     * @return the composite terminable
     */
    public <T extends CompositeTerminable> T bindTerminable(T terminable) {
        setupTerminableCleanupTask();
        terminableRegistry.bindTerminable(terminable);
        return terminable;
    }

    /**
     * Registers a CommandExecutor with the server
     *
     * @param command the command instance
     * @param aliases the command aliases
     * @param <T> the command executor class type
     * @return the command executor
     */
    public <T extends CommandExecutor> T registerCommand(T command, String... aliases) {
        Preconditions.checkArgument(aliases.length != 0, "No aliases");
        for (String alias : aliases) {
            PluginCommand cmd = getServer().getPluginCommand(alias);
            if (cmd == null) {
                try {
                    cmd = (PluginCommand) commandConstructor.newInstance(alias, this);
                } catch (Exception ex) {
                    throw new RuntimeException("Could not register command: " + alias);
                }

                // Get the command map to register the command to
                if (commandMap == null) {
                    try {
                        PluginManager pluginManager = getServer().getPluginManager();
                        Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
                        commandMapField.setAccessible(true);
                        commandMap = (CommandMap) commandMapField.get(pluginManager);
                    } catch (Exception ex) {
                        throw new RuntimeException("Could not register command: " + alias);
                    }
                }

                commandMap.register(this.getDescription().getName(), cmd);
            } else {
                // we may need to change the owningPlugin, since this was already registered
                try {
                    owningPluginField.set(cmd, this);
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

    /**
     * Gets a service provided by the ServiceManager
     *
     * @param service the service class
     * @param <T> the class type
     * @return the service
     */
    public <T> T getService(Class<T> service) {
        return getServer().getServicesManager().load(service);
    }

    /**
     * Provides a service to the ServiceManager, bound to this plugin
     *
     * @param clazz the service class
     * @param instance the instance
     * @param priority the priority to register the service at
     * @param <T> the service class type
     * @return the instance
     */
    public <T> T provideService(Class<T> clazz, T instance, ServicePriority priority) {
        getServer().getServicesManager().register(clazz, instance, this, priority);
        return instance;
    }

    /**
     * Provides a service to the ServiceManager, bound to this plugin at {@link ServicePriority#Normal}.
     *
     * @param clazz the service class
     * @param instance the instance
     * @param <T> the service class type
     * @return the instance
     */
    public <T> T provideService(Class<T> clazz, T instance) {
        Preconditions.checkNotNull(clazz, "clazz");
        Preconditions.checkNotNull(instance, "instance");
        return provideService(clazz, instance, ServicePriority.Normal);
    }

    /**
     * Gets a plugin instance for the given plugin name
     *
     * @param name the name of the plugin
     * @param pluginClass the main plugin class
     * @param <T> the main class type
     * @return the plugin
     */
    @SuppressWarnings("unchecked")
    public <T> T getPlugin(String name, Class<T> pluginClass) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(pluginClass, "pluginClass");
        return (T) getServer().getPluginManager().getPlugin(name);
    }

    /**
     * Gets a bundled file from the plugins resource folder.
     *
     * <p>If the file is not present, a version of it it copied from the jar.</p>
     *
     * @param name the name of the file
     * @return the file
     */
    public File getBundledFile(String name) {
        Preconditions.checkNotNull(name, "name");
        getDataFolder().mkdirs();
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
        return file;
    }

    /**
     * Loads a config file from a file name.
     *
     * <p>Behaves in the same was as {@link #getBundledFile(String)} when the file is not present.</p>
     *
     * @param file the name of the file
     * @return the config instance
     */
    public YamlConfiguration loadConfig(String file) {
        Preconditions.checkNotNull(file, "file");
        return YamlConfiguration.loadConfiguration(getBundledFile(file));
    }

}
