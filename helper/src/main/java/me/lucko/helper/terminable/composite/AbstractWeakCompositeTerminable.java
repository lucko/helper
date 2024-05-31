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

package me.lucko.helper.terminable.composite;

import me.lucko.helper.terminable.Terminable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AbstractWeakCompositeTerminable implements CompositeTerminable {
    private final Deque<WeakReference<AutoCloseable>> closeables = new ConcurrentLinkedDeque<>();
    private boolean closed = false;

    protected AbstractWeakCompositeTerminable() {

    }

    @Override
    public CompositeTerminable with(AutoCloseable autoCloseable) {
        Objects.requireNonNull(autoCloseable, "autoCloseable");
        this.closeables.push(new WeakReference<>(autoCloseable));
        return this;
    }

    @Override
    public void close() throws CompositeClosingException {
        List<Exception> caught = new ArrayList<>();
        for (WeakReference<AutoCloseable> ref; (ref = this.closeables.poll()) != null; ) {
            AutoCloseable ac = ref.get();
            if (ac == null) {
                continue;
            }

            try {
                ac.close();
            } catch (Exception e) {
                caught.add(e);
            }
        }
        this.closed = true;

        if (!caught.isEmpty()) {
            throw new CompositeClosingException(caught);
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void cleanup() {
        this.closeables.removeIf(ref -> {
            AutoCloseable ac = ref.get();
            return ac == null || (ac instanceof Terminable && ((Terminable) ac).isClosed());
        });
    }
}
