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

package me.lucko.helper;

import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.plugin.HelperPlugin;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Base class for helper, which mainly just proxies calls to {@link Bukkit#getServer()} for convenience.
 */
@NonnullByDefault
public final class Helper {

    /**
     * Gets the plugin which is "hosting" helper.
     *
     * @return the host plugin
     */
    public static HelperPlugin hostPlugin() {
        return LoaderUtils.getPlugin();
    }

    public static Server server() {
        return Bukkit.getServer();
    }

    public static ConsoleCommandSender console() {
        return server().getConsoleSender();
    }

    public static PluginManager plugins() {
        return server().getPluginManager();
    }

    public static ServicesManager services() {
        return server().getServicesManager();
    }

    public static BukkitScheduler bukkitScheduler() {
        return server().getScheduler();
    }

    @Nullable
    public static <T> T serviceNullable(Class<T> clazz) {
        return Services.get(clazz).orElse(null);
    }

    public static <T> Optional<T> service(Class<T> clazz) {
        return Services.get(clazz);
    }

    public static void executeCommand(String command) {
        server().dispatchCommand(console(), command);
    }

    @Nullable
    public static World worldNullable(String name) {
        return server().getWorld(name);
    }

    public static Optional<World> world(String name) {
        return Optional.ofNullable(worldNullable(name));
    }

    private Helper() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
