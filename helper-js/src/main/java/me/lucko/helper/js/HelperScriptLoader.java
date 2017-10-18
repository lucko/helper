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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

public class HelperScriptLoader implements SystemScriptLoader {

    private final HelperJsPlugin plugin;
    private final SystemScriptBindings bindings;
    private final File directory;

    // the files being handled by this instance
    private List<String> files = new ArrayList<>();

    // files which have recently been watched
    private Set<String> recentlyWatched = new HashSet<>();
    // files which have recently been unwatched
    private Set<String> recentlyUnwatched = new HashSet<>();

    private final ScriptRegistry registry = ScriptRegistry.create();
    private final ReentrantLock lock = new ReentrantLock();

    public HelperScriptLoader(HelperJsPlugin plugin, SystemScriptBindings bindings, File directory) {
        this.plugin = plugin;
        this.bindings = bindings;
        this.directory = directory;
    }

    @Override
    public void watchAll(@Nonnull Collection<String> c) {
        lock.lock();
        try {
            files.addAll(c);
            recentlyWatched.addAll(c);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unwatchAll(@Nonnull Collection<String> c) {
        lock.lock();
        try {
            files.removeAll(c);
            recentlyUnwatched.addAll(c);
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
        if (!lock.tryLock()) {
            return;
        }

        List<Terminable> toTerminate = new ArrayList<>();
        List<Script> toRun = new ArrayList<>();
        Set<String> addedFiles = new HashSet<>();

        try {
            // ensure that we only add/remove files which are still being handled by this loader instance
            // effectively ensures that the contents of addedFiles and removedFiles are also in files
            recentlyWatched.retainAll(files);
            recentlyUnwatched.removeAll(files);

            // try to handle new files
            for (String fileName : recentlyWatched) {
                File file = new File(directory, fileName);
                if (!file.exists()) {
                    continue;
                }

                // ensure it's not already loaded
                if (registry.getScript(file) != null) {
                    continue;
                }

                // init a new script instance & register it
                Script script = new HelperScript(fileName, file, file.lastModified(), new DelegateScriptLoader(this), bindings);
                registry.register(script);

                // record that we've loaded this script
                toRun.add(script);
                addedFiles.add(fileName);
                plugin.getLogger().info("Loaded script: " + formatFileName(file));
            }

            // try to handle removed files
            for (String fileName : recentlyUnwatched) {
                File file = new File(directory, fileName);

                Script script = registry.getScript(file);
                if (script == null) {
                    continue;
                }

                // unregister the script
                registry.unregister(script);

                // record that we've unloaded this script
                toTerminate.add(script);
                plugin.getLogger().info("Unloaded script: " + formatFileName(file));
            }

            // handle reloads - first make a copy of the registry
            Map<File, Script> registryCopy = new HashMap<>();
            registryCopy.putAll(registry.getAll());

            // iterate each known script
            for (Map.Entry<File, Script> entry : registryCopy.entrySet()) {
                File file = entry.getKey();
                Script script = entry.getValue();

                if (file.exists()) {

                    // if the file exists, try to reload it
                    long lastModified = script.getLastModified();
                    if (script.getLatestDependencyLoad() >= lastModified) {
                        continue;
                    }

                    // since we're creating a new script instance, we need to schedule an unload for the old one.
                    toTerminate.add(script);

                    // init a new script instance
                    Script newScript = new HelperScript(script.getName(), file, lastModified, new DelegateScriptLoader(this), bindings);
                    registry.register(newScript);
                    toRun.add(newScript);

                    plugin.getLogger().info("Reloaded script: " + formatFileName(file));
                } else {

                    // file has disappeared - unregister it
                    registry.unregister(script);

                    // add it to addedFiles, and try to load it again next cycle
                    recentlyWatched.add(file.getAbsolutePath().substring(directory.getAbsolutePath().length() + 1));

                    toTerminate.add(script);

                    plugin.getLogger().info("Unloading script: " + formatFileName(file));
                }
            }

            recentlyWatched.removeAll(addedFiles);
            recentlyUnwatched.clear();
        } finally {
            lock.unlock();
        }

        // handle init of new scripts & cleanup of old ones
        Scheduler.runSync(() -> {
            for (Script script : toRun) {
                script.run();
            }

            Terminables.silentlyTerminate(toTerminate);
        });
    }

    @Override
    public File getDirectory() {
        return directory;
    }

    @Override
    public boolean terminate() {
        // wait for & then permanently lock & completely terminate this instance
        lock.lock();

        // tidy up
        registry.terminate();
        files.clear();
        return true;
    }

    private String formatFileName(File file) {
        return file.getAbsolutePath().substring(directory.getAbsolutePath().length()).replace("\\", "/");
    }

}
