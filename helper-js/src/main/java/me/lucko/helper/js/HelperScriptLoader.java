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
import me.lucko.helper.js.bindings.SystemScriptBindings;
import me.lucko.helper.js.loader.DelegateScriptLoader;
import me.lucko.helper.js.loader.ScriptRegistry;
import me.lucko.helper.js.loader.SystemScriptLoader;
import me.lucko.helper.js.script.Script;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.Terminables;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

public class HelperScriptLoader implements SystemScriptLoader {

    // the plugin instance
    private final HelperJsPlugin plugin;

    // the system bindings
    private final SystemScriptBindings bindings;

    // the script directory
    private final Path scriptDirectory;

    // the watch service monitoring the directory
    private final WatchService watchService;

    // the watch key for the script directory
    private WatchKey watchKey;

    // the script files currently being monitored by this instance
    // these paths are relative to the script directory
    private List<Path> files = new ArrayList<>();

    // the scripts currently loaded in this instance
    private final ScriptRegistry registry = ScriptRegistry.create();

    private final ReentrantLock lock = new ReentrantLock();

    public HelperScriptLoader(HelperJsPlugin plugin, SystemScriptBindings bindings, File scriptDirectory) {
        this.plugin = plugin;
        this.bindings = bindings;
        this.scriptDirectory = scriptDirectory.toPath();

        try {
            this.watchService = scriptDirectory.toPath().getFileSystem().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            watchKey = this.scriptDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void watchAll(@Nonnull Collection<String> c) {
        lock.lock();
        try {
            for (String s : c) {
                files.add(Paths.get(s));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unwatchAll(@Nonnull Collection<String> c) {
        lock.lock();
        try {
            for (String s : c) {
                files.remove(Paths.get(s));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void preload() {
        // keep running until we stop loading files
        int filesLength;
        do {
            filesLength = files.size();
            this.run();
        } while (filesLength != files.size());
    }

    @Override
    public void run() {

        // gather work
        Set<Script> toReload = new LinkedHashSet<>();
        Set<Path> toLoad = new LinkedHashSet<>();
        Set<Script> toUnload = new LinkedHashSet<>();

        // recently watched scripts
        for (Path path : files) {

            // if the path exists, make sure we have something loaded for it
            if (scriptDirectory.resolve(path).toFile().exists()) {
                Script script = registry.getScript(path);
                if (script == null) {
                    toLoad.add(path);
                }
            } else {
                // make sure we don't have anything!
                Script script = registry.getScript(path);
                if (script != null) {
                    toUnload.add(script);
                }
            }
        }

        // check for scripts which are no longer being watched
        for (Map.Entry<Path, Script> script : registry.getAll().entrySet()) {
            if (!files.contains(script.getKey())) {
                toUnload.add(script.getValue());
            }
        }

        Set<Path> tryUnload = new HashSet<>();

        // listen for file changes
        List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
        for (WatchEvent<?> event : watchEvents) {
            Path context = (Path) event.context();
            if (context == null) {
                continue;
            }

            // already being loaded / unloaded
            if (toLoad.contains(context) || toUnload.stream().anyMatch(s -> s.getFile().endsWith(context))) {
                continue;
            }

            // try delete
            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                tryUnload.add(context);
                continue;
            }

            // otherwise, try (re)load
            Script script = registry.getScript(context);
            if (script == null) {
                toLoad.add(context);
            } else {
                toReload.add(script);
            }
        }

        boolean valid = watchKey.reset();
        if (!valid) {
            new RuntimeException("WatchKey no longer valid: " + watchEvents.toString()).printStackTrace();
            try {
                watchKey = scriptDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // process scripts to be unloaded
        for (Path p : tryUnload) {
            // only unload if the script exists
            Script script = registry.getScript(p);
            if (script == null) {
                continue;
            }

            // only unload if the script isn't otherwise being loaded
            if (toLoad.contains(p) || toReload.contains(p)) {
                continue;
            }

            toUnload.add(script);
        }

        // handle reloading first
        Set<Script> reloadQueue = new LinkedHashSet<>();
        for (Script s : toReload) {
            resolveDepends(reloadQueue, s);
        }

        Set<Terminable> toTerminate = new HashSet<>();
        Set<Script> toRun = new HashSet<>();

        for (Script script : reloadQueue) {
            // since we're creating a new script instance, we need to schedule an unload for the old one.
            toTerminate.add(script);

            // init a new script instance
            Script newScript = new HelperScript(script.getName(), script.getFile(), new DelegateScriptLoader(this), bindings);
            registry.register(newScript);
            toRun.add(newScript);

            plugin.getLogger().info("Reloaded script: " + script.getFile());
        }

        // then handle loads
        for (Path path : toLoad) {
            // double check the script isn't loaded already.
            if (registry.getScript(path) != null) {
                continue;
            }

            // init a new script instance & register it
            Script script = new HelperScript(path.getFileName().toString(), path, new DelegateScriptLoader(this), bindings);
            registry.register(script);

            // record that we've loaded this script
            toRun.add(script);
            plugin.getLogger().info("Loaded script: " + path.toString());
        }

        for (Script s : toUnload) {
            registry.unregister(s);
            toTerminate.add(s);
            plugin.getLogger().info("Unloaded script: " + s.getFile().toString());
        }

        // handle init of new scripts & cleanup of old ones
        Scheduler.runSync(() -> {
            for (Script script : toRun) {
                script.run();
            }

            Terminables.silentlyTerminate(toTerminate);
        });
    }

    private void resolveDepends(Set<Script> accumulator, Script script) {
        if (!accumulator.add(script)) {
            return;
        }

        for (Script other : registry.getAll().values()) {
            if (other.getDependencies().contains(script.getFile())) {
                resolveDepends(accumulator, other);
            }
        }
    }

    @Override
    public Path getDirectory() {
        return scriptDirectory;
    }

    @Override
    public boolean terminate() {
        // tidy up
        registry.terminate();
        files.clear();
        return true;
    }

}
