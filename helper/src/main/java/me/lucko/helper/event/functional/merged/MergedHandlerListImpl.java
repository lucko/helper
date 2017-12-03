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

package me.lucko.helper.event.functional.merged;

import com.google.common.base.Preconditions;

import me.lucko.helper.event.MergedSubscription;
import me.lucko.helper.internal.LoaderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

class MergedHandlerListImpl<T> implements MergedHandlerList<T> {
    private final MergedBuilder<T> builder;
    private final List<BiConsumer<MergedSubscription<T>, ? super T>> handlers = new ArrayList<>(1);

    MergedHandlerListImpl(@Nonnull MergedBuilder<T> builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MergedHandlerList<T> biConsumer(@Nonnull BiConsumer<MergedSubscription<T>, ? super T> handler) {
        Preconditions.checkNotNull(handler, "handler");
        handlers.add(handler);
        return this;
    }

    @Nonnull
    @Override
    public MergedSubscription<T> register() {
        if (handlers.isEmpty()) {
            throw new IllegalStateException("No handlers have been registered");
        }

        HelperMergedEventListener<T> listener = new HelperMergedEventListener<>(builder, handlers);
        listener.register(LoaderUtils.getPlugin());
        return listener;
    }
}
