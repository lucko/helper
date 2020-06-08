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

import me.lucko.helper.Schedulers;
import me.lucko.helper.Services;
import me.lucko.helper.config.ConfigFactory;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.maven.LibraryLoader;
import me.lucko.helper.scheduler.HelperExecutors;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.CommandMapUtil;

import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import ninja.leaping.configurate.ConfigurationNode;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An "extended" JavaPlugin class.
 */
public class ExtendedJavaPlugin extends JavaPlugin implements HelperPlugin {

    // the backing terminable registry
    private CompositeTerminable terminableRegistry;

    // are we the plugin that's providing helper?
    private boolean isLoaderPlugin;

    // Used by subclasses to perform logic for plugin load/enable/disable.
    protected void load() {}
    protected void enable() {}
    protected void disable() {}

    @Override
    public final void onLoad() {
        // LoaderUtils.getPlugin() has the side effect of caching the loader ref
        // do that nice and early. also store whether 'this' plugin is the loader.
        final HelperPlugin loaderPlugin = LoaderUtils.getPlugin();
        this.isLoaderPlugin = this == loaderPlugin;

        this.terminableRegistry = CompositeTerminable.create();

        LibraryLoader.loadAll(getClass());

        // call subclass
        load();
    }

    @Override
    public final void onEnable() {
        // schedule cleanup of the registry
        Schedulers.builder()
                .async()
                .after(10, TimeUnit.SECONDS)
                .every(30, TimeUnit.SECONDS)
                .run(this.terminableRegistry::cleanup)
                .bindWith(this.terminableRegistry);

        // setup services
        if (this.isLoaderPlugin) {
            HelperServices.setup(this);
        }

        // call subclass
        enable();
    }

    @Override
    public final void onDisable() {

        // call subclass
        disable();

        // terminate the registry
        this.terminableRegistry.closeAndReportException();

        if (this.isLoaderPlugin) {
            // shutdown the scheduler
            HelperExecutors.shutdown();
        }
    }

    @Nonnull
    @Override
    public <T extends AutoCloseable> T bind(@Nonnull T terminable) {
        return this.terminableRegistry.bind(terminable);
    }

    @Nonnull
    @Override
    public <T extends TerminableModule> T bindModule(@Nonnull T module) {
        return this.terminableRegistry.bindModule(module);
    }

    @Nonnull
    @Override
    public <T extends Listener> T registerListener(@Nonnull T listener) {
        Objects.requireNonNull(listener, "listener");
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }

    @Nonnull
    @Override
    public <T extends CommandExecutor> T registerCommand(@Nonnull T command, String permission, String permissionMessage, String description, @Nonnull String... aliases) {
        return CommandMapUtil.registerCommand(this, command, permission, permissionMessage, description, aliases);
    }

    @Nonnull
    @Override
    public <T> T getService(@Nonnull Class<T> service) {
        return Services.load(service);
    }

    @Nonnull
    @Override
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance, @Nonnull ServicePriority priority) {
        return Services.provide(clazz, instance, this, priority);
    }

    @Nonnull
    @Override
    public <T> T provideService(@Nonnull Class<T> clazz, @Nonnull T instance) {
        return provideService(clazz, instance, ServicePriority.Normal);
    }

    @Override
    public boolean isPluginPresent(@Nonnull String name) {
        return getServer().getPluginManager().getPlugin(name) != null;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getPlugin(@Nonnull String name, @Nonnull Class<T> pluginClass) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(pluginClass, "pluginClass");
        return (T) getServer().getPluginManager().getPlugin(name);
    }

    private File getRelativeFile(@Nonnull String name) {
        getDataFolder().mkdirs();
        return new File(getDataFolder(), name);
    }

    @Nonnull
    @Override
    public File getBundledFile(@Nonnull String name) {
        Objects.requireNonNull(name, "name");
        File file = getRelativeFile(name);
        if (!file.exists()) {
            saveResource(name, false);
        }
        return file;
    }

    @Nonnull
    @Override
    public YamlConfiguration loadConfig(@Nonnull String file) {
        Objects.requireNonNull(file, "file");
        return YamlConfiguration.loadConfiguration(getBundledFile(file));
    }

    @Nonnull
    @Override
    public ConfigurationNode loadConfigNode(@Nonnull String file) {
        Objects.requireNonNull(file, "file");
        return ConfigFactory.yaml().load(getBundledFile(file));
    }

    @Nonnull
    @Override
    public <T> T setupConfig(@Nonnull String file, @Nonnull T configObject) {
        Objects.requireNonNull(file, "file");
        Objects.requireNonNull(configObject, "configObject");
        File f = getRelativeFile(file);
        ConfigFactory.yaml().load(f, configObject);
        return configObject;
    }

    @Nonnull
    @Override
    public ClassLoader getClassloader() {
        return super.getClassLoader();
    }
}
