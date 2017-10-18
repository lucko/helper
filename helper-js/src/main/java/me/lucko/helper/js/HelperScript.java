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

import me.lucko.helper.js.bindings.SystemScriptBindings;
import me.lucko.helper.js.loader.ScriptLoader;
import me.lucko.helper.js.plugin.ScriptPlugin;
import me.lucko.helper.js.script.Script;
import me.lucko.helper.js.script.ScriptLogger;
import me.lucko.helper.js.script.SimpleScriptLogger;
import me.lucko.helper.terminable.registry.TerminableRegistry;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

public class HelperScript implements Script {

    // the nashorn script engine
    private static ScriptEngine engine;
    private static synchronized ScriptEngine initEngine(ScriptPlugin plugin) {
        if (engine == null) {
            engine = new NashornScriptEngineFactory().getScriptEngine(plugin.getPluginClassLoader());
        }
        return engine;
    }

    // the name of this script
    private final String name;
    // the associated script file
    private final File file;
    // the time when a dependency was last loaded
    private long dependencyLastLoad;

    // the loader instance handling this script
    private final ScriptLoader loader;
    // the bindings used by this script
    private final SystemScriptBindings systemBindings;
    // the scripts logger
    private final ScriptLogger logger;
    // the terminable registry used by this script
    private final TerminableRegistry terminableRegistry = TerminableRegistry.create();
    // the scripts dependencies
    private final Set<File> depends = new HashSet<>();

    public HelperScript(@Nonnull String name, @Nonnull File file, long dependencyLastLoad, @Nonnull ScriptLoader loader, @Nonnull SystemScriptBindings systemBindings) {
        if (name.endsWith(".js")) {
            this.name = name.substring(0, name.lastIndexOf('.'));
        } else {
            this.name = name;
        }
        this.file = file;
        this.dependencyLastLoad = dependencyLastLoad;
        this.loader = loader;
        this.systemBindings = systemBindings;
        this.logger = new SimpleScriptLogger(this);
        this.depends.add(this.file);
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public File getFile() {
        return file;
    }

    @Override
    public long getLastModified() {
        long lastModified = 0;

        for (File depend : depends) {
            try {
                // avoid a caching issue
                new FileReader(depend).close();
            } catch (Exception ignored) { }

            long dependLastModified = depend.lastModified();
            if (dependLastModified > lastModified) {
                lastModified = dependLastModified;
            }
        }

        return lastModified;
    }

    @Override
    public long getLatestDependencyLoad() {
        return dependencyLastLoad;
    }

    @Nonnull
    @Override
    public SystemScriptBindings getBindings() {
        return systemBindings;
    }

    @Nonnull
    @Override
    public ScriptLogger getLogger() {
        return logger;
    }

    @Override
    public void run() {
        ScriptEngine engine = initEngine(systemBindings.getPlugin());

        try {
            // create bindings
            Bindings bindings = engine.createBindings();

            // provide an export for this scripts attributes
            bindings.put("loader", loader);
            bindings.put("registry", terminableRegistry);
            bindings.put("logger", logger);

            // the path of the script file (current working directory)
            bindings.put("cwd", stripDotSlash(file.getAbsolutePath()));

            // the root scripts directory
            bindings.put("rsd", stripDotSlash(loader.getDirectory().getAbsolutePath()) + "/");

            // function to depend on another script
            bindings.put("depend", (Consumer<String>) this::depend);

            // append the global helper bindings to this instance
            systemBindings.appendTo(bindings);

            // create a new script context, and attach our bindings
            ScriptContext context = new SimpleScriptContext();
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            // evaluate the header
            engine.eval(systemBindings.getPlugin().getScriptHeader(), context);

            // load the script
            String path = stripDotSlash(file.getPath());
            engine.eval("__load(\"" + path + "\");", context);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void depend(@Nonnull File file) {
        if (this.file.getAbsolutePath().equals(file.getAbsolutePath())) {
            return;
        }

        long lastModified = file.lastModified();
        if (lastModified > this.dependencyLastLoad) {
            this.dependencyLastLoad = lastModified;
        }

        depends.add(file);
    }

    @Override
    public boolean terminate() {
        loader.terminate();
        terminableRegistry.terminate();
        return true;
    }

    private static String stripDotSlash(String string) {
        if (string.startsWith("./")) {
            string = string.substring(2);
        }
        return string.replace("\\", "/");
    }

}
