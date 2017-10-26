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

import com.google.common.io.CharStreams;

import me.lucko.helper.Scheduler;
import me.lucko.helper.js.loader.SystemScriptLoader;
import me.lucko.helper.js.plugin.ScriptPlugin;
import me.lucko.helper.js.utils.EnsureLoad;
import me.lucko.helper.plugin.ExtendedJavaPlugin;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

public class HelperJsPlugin extends ExtendedJavaPlugin implements ScriptPlugin {

    private HelperScriptLoader loader;
    private String scriptHeader;

    @Override
    protected void load() {
        // ensure all helper classes are loaded in
        EnsureLoad.ensure();
    }

    @Override
    protected void enable() {

        // get the script header
        InputStream headerResource = getResource("header.js");
        if (headerResource == null) {
            throw new RuntimeException("Unable to get resource 'header.js' from jar");
        }

        try {
            scriptHeader = CharStreams.toString(new InputStreamReader(headerResource, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Exception occurred whilst reading header file", e);
        }

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

        // setup script loader
        loader = new HelperScriptLoader(this, new HelperScriptBindings(this), scriptDirectory);
        loader.watch(config.getString("init-script", "init.js"));
        loader.preload();

        // schedule script loader poll task
        Scheduler.runTaskRepeatingAsync(loader, 0L, config.getLong("poll-interval", 20L));
    }

    @Nonnull
    @Override
    public SystemScriptLoader getScriptLoader() {
        return loader;
    }

    @Nonnull
    public ClassLoader getPluginClassLoader() {
        return getClassLoader();
    }

    @Nonnull
    @Override
    public Logger getPluginLogger() {
        return getLogger();
    }

    @Nonnull
    public String getScriptHeader() {
        return scriptHeader;
    }

}
