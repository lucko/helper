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

package me.lucko.helper.js.loader;

import me.lucko.helper.js.script.Script;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class SimpleScriptRegistry implements ScriptRegistry {

    private Map<File, Script> scripts = new HashMap<>();

    @Override
    public void register(@Nonnull Script script) {
        scripts.put(script.getFile(), script);
    }

    @Override
    public void unregister(@Nonnull Script script) {
        scripts.remove(script.getFile());
    }

    @Nullable
    @Override
    public Script getScript(@Nonnull File file) {
        return scripts.get(file);
    }

    @Nonnull
    @Override
    public Map<File, Script> getAll() {
        return Collections.unmodifiableMap(scripts);
    }

    @Override
    public boolean terminate() {
        for (Script script : scripts.values()) {
            script.terminate();
        }
        return true;
    }

}
