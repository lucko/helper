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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link ScriptLoader} which delegates calls to a parent, but keeps track of the files
 * watched via its instance.
 */
public class DelegateScriptLoader implements ScriptLoader {

    private ScriptLoader parent;
    private List<String> files = new ArrayList<>();

    public DelegateScriptLoader(@Nonnull ScriptLoader parent) {
        this.parent = parent;
    }

    @Override
    public void watchAll(@Nonnull Collection<String> c) {
        c = new ArrayList<>(c);
        c.removeAll(files);

        files.addAll(c);
        parent.watchAll(c);
    }

    @Override
    public void unwatchAll(@Nonnull Collection<String> c) {
        c = new ArrayList<>(c);
        c.retainAll(files);

        files.removeAll(c);
        parent.unwatchAll(c);
    }

    @Override
    public File getDirectory() {
        return parent.getDirectory();
    }

    @Override
    public boolean terminate() {
        unwatchAll(files);
        return true;
    }
}
