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

package me.lucko.helper.terminable.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

public class SimpleTerminableRegistry implements TerminableRegistry {
    private final List<Terminable> terminables = Collections.synchronizedList(new ArrayList<>());
    private boolean terminated = false;

    @Nonnull
    @Override
    public final <T extends Terminable> T bind(@Nonnull T terminable) {
        Preconditions.checkNotNull(terminable, "terminable");
        terminables.add(terminable);
        return terminable;
    }

    @Nonnull
    @Override
    public <T extends CompositeTerminable> T bindComposite(@Nonnull T terminable) {
        Preconditions.checkNotNull(terminable, "terminable");
        terminable.setup(this);
        return terminable;
    }

    @Override
    public final boolean terminate() {
        Lists.reverse(terminables).forEach((terminable) -> {
            try {
                terminable.terminate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        terminables.clear();
        terminated = true;
        return true;
    }

    @Override
    public final boolean hasTerminated() {
        return terminated;
    }

    @Override
    public final void cleanup() {
        terminables.removeIf(Terminable::hasTerminated);
    }
}
