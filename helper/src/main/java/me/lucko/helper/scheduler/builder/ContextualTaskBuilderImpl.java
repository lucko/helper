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

package me.lucko.helper.scheduler.builder;

import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.ThreadContext;
import me.lucko.helper.scheduler.Task;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

class ContextualTaskBuilderImpl implements ContextualTaskBuilder {
    private final ThreadContext context;
    private final long delay;
    private final long interval;

    ContextualTaskBuilderImpl(ThreadContext context, long delay, long interval) {
        this.context = context;
        this.delay = delay;
        this.interval = interval;
    }

    @Nonnull
    @Override
    public Task consume(@Nonnull Consumer<Task> consumer) {
        return Schedulers.get(this.context).runRepeating(consumer, this.delay, this.interval);
    }

    @Nonnull
    @Override
    public Task run(@Nonnull Runnable runnable) {
        return Schedulers.get(this.context).runRepeating(runnable, this.delay, this.interval);
    }
}
