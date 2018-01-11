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
import me.lucko.helper.config.Configs;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.maven.LibraryLoader;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.terminable.registry.TerminableRegistry;
import me.lucko.helper.utils.CommandMapUtil;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An "extended" JavaPlugin class.
 */
public class ExtendedJavaPlugin extends JavaPlugin implements HelperPlugin {

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
        Scheduler.builder()
                .async()
                .after(10, TimeUnit.SECONDS)
                .every(30, TimeUnit.SECONDS)
                .run(terminableRegistry::cleanup)
                .bindWith(terminableRegistry);

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

    @Nonnull
    @Override
    public <T extends Listener> T registerListener(@Nonnull T listener) {
        Preconditions.checkNotNull(listener, "listener");
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }

    @Nonnull
    @Override
    public <T extends CommandExecutor> T registerCommand(@Nonnull T command, @Nonnull String... aliases) {
        return CommandMapUtil.registerCommand(this, command, aliases);
    }

    @Nullable
    @Override
    public <T> T getService(@Nonnull Class<T> service) {
        return getServer().getServicesManager().load(service);
    }

    @Nonnull
    @Override
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance, @Nonnull ServicePriority priority) {
        getServer().getServicesManager().register(clazz, instance, this, priority);
        return instance;
    }

    @Nonnull
    @Override
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance) {
        Preconditions.checkNotNull(clazz, "clazz");
        Preconditions.checkNotNull(instance, "instance");
        return provideService(clazz, instance, ServicePriority.Normal);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getPlugin(@Nonnull String name, @Nonnull Class<T> pluginClass) {
        Preconditions.checkNotNull(name, "name");
        Preconditions.checkNotNull(pluginClass, "pluginClass");
        return (T) getServer().getPluginManager().getPlugin(name);
    }

    @Nonnull
    @Override
    public File getBundledFile(@Nonnull String name) {
        Preconditions.checkNotNull(name, "name");
        getDataFolder().mkdirs();
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
        return file;
    }

    @Nonnull
    @Override
    public YamlConfiguration loadConfig(@Nonnull String file) {
        Preconditions.checkNotNull(file, "file");
        return YamlConfiguration.loadConfiguration(getBundledFile(file));
    }

    @Nonnull
    @Override
    public ConfigurationNode loadConfigNode(@Nonnull String file) {
        Preconditions.checkNotNull(file, "file");
        return Configs.yamlLoad(getBundledFile(file));
    }

    @Nonnull
    @Override
    public ClassLoader getClassloader() {
        return super.getClassLoader();
    }
}
