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

package me.lucko.helper.event.functional.single;

import com.google.common.base.Preconditions;

import me.lucko.helper.event.SingleSubscription;
import me.lucko.helper.internal.LoaderUtils;

import org.bukkit.event.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

class SingleHandlerListImpl<T extends Event> implements SingleHandlerList<T> {
    private final SingleBuilder<T> builder;
    private final List<BiConsumer<SingleSubscription<T>, ? super T>> handlers = new ArrayList<>(1);

    SingleHandlerListImpl(@Nonnull SingleBuilder<T> builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public SingleHandlerList<T> biConsumer(@Nonnull BiConsumer<SingleSubscription<T>, ? super T> handler) {
        Preconditions.checkNotNull(handler, "handler");
        this.handlers.add(handler);
        return this;
    }

    @Nonnull
    @Override
    public SingleSubscription<T> register() {
        if (this.handlers.isEmpty()) {
            throw new IllegalStateException("No handlers have been registered");
        }

        HelperEventListener<T> listener = new HelperEventListener<>(this.builder, this.handlers);
        listener.register(LoaderUtils.getPlugin());
        return listener;
    }
}
