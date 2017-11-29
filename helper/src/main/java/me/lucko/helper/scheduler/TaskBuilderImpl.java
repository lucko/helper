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

package me.lucko.helper.scheduler;

import me.lucko.helper.Scheduler;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.promise.ThreadContext;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

class TaskBuilderImpl implements TaskBuilder {
    static final TaskBuilder INSTANCE = new TaskBuilderImpl();

    private final TaskBuilder.ThreadContextual sync;
    private final ThreadContextual async;

    private TaskBuilderImpl() {
        sync = new ThreadContextualBuilder(ThreadContext.SYNC);
        async = new ThreadContextualBuilder(ThreadContext.ASYNC);
    }

    @Nonnull
    @Override
    public TaskBuilder.ThreadContextual sync() {
        return sync;
    }

    @Nonnull
    @Override
    public TaskBuilder.ThreadContextual async() {
        return async;
    }

    private static final class ThreadContextualBuilder implements TaskBuilder.ThreadContextual {
        private final ThreadContext context;
        private final ContextualPromiseBuilder instant;

        private ThreadContextualBuilder(ThreadContext context) {
            this.context = context;
            this.instant = new ContextualPromiseBuilderImpl(context);
        }

        @Nonnull
        @Override
        public ContextualPromiseBuilder now() {
            return instant;
        }

        @Nonnull
        @Override
        public TaskBuilder.Delayed after(long ticks) {
            return new DelayedBuilder(context, ticks);
        }

        @Nonnull
        @Override
        public TaskBuilder.Delayed after(long duration, @Nonnull TimeUnit unit) {
            return new DelayedBuilder(context, Ticks.from(duration, unit));
        }
    }

    private static final class DelayedBuilder implements TaskBuilder.Delayed {
        private final ThreadContext context;
        private final long delay;

        private DelayedBuilder(ThreadContext context, long delay) {
            this.context = context;
            this.delay = delay;
        }

        @Nonnull
        @Override
        public <T> Promise<T> supply(@Nonnull Supplier<T> supplier) {
            return Scheduler.supplyLater(context, supplier, delay);
        }

        @Nonnull
        @Override
        public <T> Promise<T> call(@Nonnull Callable<T> callable) {
            return Scheduler.callLater(context, callable, delay);
        }

        @Nonnull
        @Override
        public Promise<Void> run(@Nonnull Runnable runnable) {
            return Scheduler.runLater(context, runnable, delay);
        }

        @Nonnull
        @Override
        public ContextualTaskBuilder every(long ticks) {
            return new ContextualTaskBuilderImpl(context, delay, ticks);
        }

        @Nonnull
        @Override
        public ContextualTaskBuilder every(long duration, TimeUnit unit) {
            return new ContextualTaskBuilderImpl(context, delay, Ticks.from(duration, unit));
        }
    }
}
