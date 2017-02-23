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

package me.lucko.helper.plugin;

import com.google.common.collect.Lists;

import me.lucko.helper.utils.CompositeTerminable;
import me.lucko.helper.utils.Terminable;

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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private final List<Terminable> terminables = new ArrayList<>();

    @Override
    public final void onDisable() {
        Lists.reverse(terminables).forEach((terminable) -> {
            try {
                terminable.terminate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        terminables.clear();
    }

    @Override
    public void accept(Terminable terminable) {
        registerTerminable(terminable);
    }

    public <T extends Listener> T registerListener(T listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }

    public <T extends Terminable> T registerTerminable(T terminable) {
        terminables.add(terminable);
        return terminable;
    }

    public <T extends CompositeTerminable> T bindTerminable(T terminable) {
        terminable.bind(this);
        return terminable;
    }

    public <T extends CommandExecutor> T registerCommand(T command, String... aliases) {
        if (aliases.length == 0) {
            throw new IllegalArgumentException("no aliases");
        }
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

    public <T> T getService(Class<T> service) {
        return getServer().getServicesManager().load(service);
    }

    public <T> T provideService(Class<T> clazz, T instance, ServicePriority priority) {
        getServer().getServicesManager().register(clazz, instance, this, priority);
        return instance;
    }

    public <T> T provideService(Class<T> clazz, T instance) {
        return provideService(clazz, instance, ServicePriority.Normal);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPlugin(String name, Class<T> pluginClass) {
        return (T) getServer().getPluginManager().getPlugin(name);
    }

    public File getBundledFile(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
        return file;
    }

    public YamlConfiguration loadConfig(String file) {
        return YamlConfiguration.loadConfiguration(getBundledFile(file));
    }

}
