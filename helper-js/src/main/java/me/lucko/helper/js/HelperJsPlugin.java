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

package me.lucko.helper.js;

import me.lucko.helper.Scheduler;
import me.lucko.helper.js.bindings.GeneralScriptBindings;
import me.lucko.helper.js.bindings.HelperScriptBindings;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.scheduler.Ticks;
import me.lucko.scriptcontroller.ScriptController;
import me.lucko.scriptcontroller.environment.loader.ScriptLoadingExecutor;
import me.lucko.scriptcontroller.logging.SystemLogger;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class HelperJsPlugin extends ExtendedJavaPlugin {
    private ScriptController controller;

    @Override
    protected void enable() {
        // load config
        YamlConfiguration config = loadConfig("config.yml");

        // get script directory
        Path scriptDirectory = Paths.get(config.getString("script-directory"));
        if (!Files.isDirectory(scriptDirectory)) {
            try {
                Files.createDirectories(scriptDirectory);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getLogger().info("Using script directory: " + scriptDirectory.toString() + "(" + scriptDirectory.toAbsolutePath().toString() + ")");

        this.controller = ScriptController.builder()
                .withDirectory(scriptDirectory)
                .loadExecutor(new ScriptLoadingExecutor() {
                    @Override
                    public AutoCloseable scheduleAtFixedRate(Runnable runnable, long l, TimeUnit timeUnit) {
                        return Scheduler.builder()
                                .async()
                                .after(0L)
                                .every(l, timeUnit)
                                .run(runnable);
                    }

                    @Override
                    public void execute(@Nonnull Runnable command) {
                        Scheduler.runAsync(command);
                    }
                })
                .runExecutor(Scheduler.sync())
                .pollRate(Ticks.to(config.getLong("poll-interval", 20L), TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS)
                .logger(new SystemLogger() {
                    @Override public void info(String s) { getLogger().info(s); }
                    @Override public void warning(String s) { getLogger().warning(s); }
                    @Override public void severe(String s) { getLogger().severe(s); }
                })
                .withBindings(new GeneralScriptBindings())
                .withBindings(new HelperScriptBindings(this))
                .build();
    }

    @Override
    protected void disable() {
        this.controller.shutdown();
    }
}
