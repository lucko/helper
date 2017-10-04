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
import me.lucko.helper.maven.LibraryLoader;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.terminable.composite.CompositeTerminableConsumer;
import me.lucko.helper.terminable.registry.TerminableRegistry;
import me.lucko.helper.utils.CommandMapUtil;
import me.lucko.helper.utils.LoaderUtils;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An "extended" JavaPlugin class.
 */
public class ExtendedJavaPlugin extends JavaPlugin implements TerminableConsumer, CompositeTerminableConsumer {

    // the backing terminable registry
    private TerminableRegistry terminableRegistry;

    // Used by subclasses to perform logic for plugin load/enable/disable.
    protected void load() {}
    protected void enable() {}
    protected void disable() {}

    @Override
    public final void onLoad() {
        LoaderUtils.getPlugin(); // cache the loader plugin & run initial setup
        terminableRegistry = TerminableRegistry.create();

        LibraryLoader.loadAll(getClass());

        // call subclass
        load();
    }

    @Override
    public final void onEnable() {
        // schedule cleanup of the registry
        Scheduler.runTaskRepeatingAsync(terminableRegistry::cleanup, 600L, 600L).bindWith(terminableRegistry);

        // call subclass
        enable();
    }

    @Override
    public final void onDisable() {

        // call subclass
        disable();

        // terminate the registry
        terminableRegistry.terminate();
    }

    @Nonnull
    @Override
    public <T extends Terminable> T bind(@Nonnull T terminable) {
        return terminableRegistry.bind(terminable);
    }

    @Nonnull
    @Override
    public <T extends Runnable> T bindRunnable(@Nonnull T runnable) {
        return terminableRegistry.bindRunnable(runnable);
    }

    @Nonnull
    @Override
    public <T extends CompositeTerminable> T bindComposite(@Nonnull T terminable) {
        return terminableRegistry.bindComposite(terminable);
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
    @Nonnull
    public <T extends Listener> T registerListener(@Nonnull T listener) {
        Preconditions.checkNotNull(listener, "listener");
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
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
    public <T extends CommandExecutor> T registerCommand(@Nonnull T command, @Nonnull String... aliases) {
        return CommandMapUtil.registerCommand(this, command, aliases);
    }

    /**
     * Gets a service provided by the ServiceManager
     *
     * @param service the service class
     * @param <T> the class type
     * @return the service
     */
    @Nullable
    public <T> T getService(@Nonnull Class<T> service) {
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
    @Nonnull
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance, @Nonnull ServicePriority priority) {
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
    @Nonnull
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance) {
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
    @Nullable
    public <T> T getPlugin(@Nonnull String name, @Nonnull Class<T> pluginClass) {
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
    @Nonnull
    public File getBundledFile(@Nonnull String name) {
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
    @Nonnull
    public YamlConfiguration loadConfig(@Nonnull String file) {
        Preconditions.checkNotNull(file, "file");
        return YamlConfiguration.loadConfiguration(getBundledFile(file));
    }

}
