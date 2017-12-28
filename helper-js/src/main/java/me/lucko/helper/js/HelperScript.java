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
import me.lucko.helper.js.loader.DelegateScriptLoader;
import me.lucko.helper.js.loader.ScriptLoader;
import me.lucko.helper.js.loader.SystemScriptLoader;
import me.lucko.helper.js.script.Script;
import me.lucko.helper.js.script.ScriptLogger;
import me.lucko.helper.js.script.SimpleScriptLogger;
import me.lucko.helper.terminable.registry.TerminableRegistry;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;

public class HelperScript implements Script {

    // the name of this script
    private final String name;
    // the associated script file
    private final Path path;

    // the loader instance handling this script
    private final ScriptLoader loader;
    // the bindings used by this script
    private final SystemScriptBindings systemBindings;
    // the scripts logger
    private final ScriptLogger logger;
    // the terminable registry used by this script
    private final TerminableRegistry terminableRegistry = TerminableRegistry.create();
    // the scripts dependencies
    private final Set<Path> depends = new HashSet<>();

    public HelperScript(@Nonnull Path path, @Nonnull SystemScriptLoader loader, @Nonnull SystemScriptBindings systemBindings) {
        String name = path.getFileName().toString();
        if (name.endsWith(".js")) {
            this.name = name.substring(0, name.length() - 3);
        } else {
            this.name = name;
        }
        this.path = path;
        this.loader = new DelegateScriptLoader(loader);
        this.systemBindings = systemBindings;
        this.logger = new SimpleScriptLogger(this);
        this.depends.add(this.path);
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Path getPath() {
        return path;
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
        ScriptEngine engine = loader.getScriptEngine();

        try {
            // create bindings
            Bindings bindings = engine.createBindings();

            // provide an export for this scripts attributes
            bindings.put("loader", loader);
            bindings.put("registry", terminableRegistry);
            bindings.put("logger", logger);

            // the path of the script file (current working directory)
            bindings.put("cwd", path.normalize().toString().replace("\\", "/"));

            // the root scripts directory
            bindings.put("rsd", loader.getDirectory().normalize().toString().replace("\\", "/") + "/");

            // function to depend on another script
            bindings.put("depend", (Consumer<String>) this::depend);

            // append the global helper bindings to this instance
            systemBindings.appendTo(bindings);

            // create a new script context, and attach our bindings
            ScriptContext context = new SimpleScriptContext();
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            // evaluate the header
            engine.eval(systemBindings.getPlugin().getScriptHeader(), context);

            // resolve the load path, relative to the loader directory.
            Path loadPath = loader.getDirectory().normalize().resolve(path);
            engine.eval("__load(\"" + loadPath.toString().replace("\\", "/") + "\");", context);

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public Set<Path> getDependencies() {
        return Collections.unmodifiableSet(depends);
    }

    @Override
    public void depend(@Nonnull String path) {
        depend(Paths.get(path));
    }

    @Override
    public void depend(@Nonnull Path path) {
        if (this.path.equals(path)) {
            return;
        }

        depends.add(path);
    }

    @Override
    public boolean terminate() {
        loader.terminate();
        terminableRegistry.terminate();
        return true;
    }

}
