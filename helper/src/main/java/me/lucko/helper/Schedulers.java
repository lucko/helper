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

package me.lucko.helper;

import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.internal.exception.HelperExceptions;
import me.lucko.helper.promise.ThreadContext;
import me.lucko.helper.scheduler.HelperExecutors;
import me.lucko.helper.scheduler.Scheduler;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.scheduler.Ticks;
import me.lucko.helper.scheduler.builder.TaskBuilder;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * Provides common instances of {@link Scheduler}.
 */
@NonnullByDefault
public final class Schedulers {
    private static final Scheduler SYNC_SCHEDULER = new SyncScheduler();
    private static final Scheduler ASYNC_SCHEDULER = new AsyncScheduler();

    /**
     * Gets a scheduler for the given context.
     *
     * @param context the context
     * @return a scheduler
     */
    public static Scheduler get(ThreadContext context) {
        switch (context) {
            case SYNC:
                return sync();
            case ASYNC:
                return async();
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a "sync" scheduler, which executes tasks on the main server thread.
     *
     * @return a sync executor instance
     */
    public static Scheduler sync() {
        return SYNC_SCHEDULER;
    }

    /**
     * Returns an "async" scheduler, which executes tasks asynchronously.
     *
     * @return an async executor instance
     */
    public static Scheduler async() {
        return ASYNC_SCHEDULER;
    }

    /**
     * Gets Bukkit's scheduler.
     *
     * @return bukkit's scheduler
     */
    public static BukkitScheduler bukkit() {
        return Helper.bukkitScheduler();
    }

    /**
     * Gets a {@link TaskBuilder} instance
     *
     * @return a task builder
     */
    public static TaskBuilder builder() {
        return TaskBuilder.newBuilder();
    }

    private static final class SyncScheduler implements Scheduler {

        @Override
        public void execute(Runnable runnable) {
            HelperExecutors.sync().execute(runnable);
        }

        @Nonnull
        @Override
        public ThreadContext getContext() {
            return ThreadContext.SYNC;
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Consumer<Task> consumer, long delayTicks, long intervalTicks) {
            Objects.requireNonNull(consumer, "consumer");
            HelperTask task = new HelperTask(consumer);
            task.runTaskTimer(LoaderUtils.getPlugin(), delayTicks, intervalTicks);
            return task;
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Consumer<Task> consumer, long delay, @Nonnull TimeUnit delayUnit, long interval, @Nonnull TimeUnit intervalUnit) {
            return runRepeating(consumer, Ticks.from(delay, delayUnit), Ticks.from(interval, intervalUnit));
        }
    }

    private static final class AsyncScheduler implements Scheduler {

        @Override
        public void execute(Runnable runnable) {
            HelperExecutors.asyncHelper().execute(runnable);
        }

        @Nonnull
        @Override
        public ThreadContext getContext() {
            return ThreadContext.ASYNC;
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Consumer<Task> consumer, long delayTicks, long intervalTicks) {
            Objects.requireNonNull(consumer, "consumer");
            HelperTask task = new HelperTask(consumer);
            task.runTaskTimerAsynchronously(LoaderUtils.getPlugin(), delayTicks, intervalTicks);
            return task;
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Consumer<Task> consumer, long delay, @Nonnull TimeUnit delayUnit, long interval, @Nonnull TimeUnit intervalUnit) {
            Objects.requireNonNull(consumer, "consumer");
            return new HelperAsyncTask(consumer, delay, delayUnit, interval, intervalUnit);
        }
    }

    private static class HelperTask extends BukkitRunnable implements Task, Delegate<Consumer<Task>> {
        private final Consumer<Task> backingTask;

        private final AtomicInteger counter = new AtomicInteger(0);
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private HelperTask(Consumer<Task> backingTask) {
            this.backingTask = backingTask;
        }

        @Override
        public void run() {
            if (this.cancelled.get()) {
                cancel();
                return;
            }

            try {
                this.backingTask.accept(this);
                this.counter.incrementAndGet();
            } catch (Throwable e) {
                HelperExceptions.reportScheduler(e);
            }

            if (this.cancelled.get()) {
                cancel();
            }
        }

        @Override
        public int getTimesRan() {
            return this.counter.get();
        }

        @Override
        public boolean stop() {
            return !this.cancelled.getAndSet(true);
        }

        @Override
        public int getBukkitId() {
            return getTaskId();
        }

        @Override
        public boolean isClosed() {
            return this.cancelled.get();
        }

        @Override
        public Consumer<Task> getDelegate() {
            return this.backingTask;
        }
    }

    private static class HelperAsyncTask implements Runnable, Task, Delegate<Consumer<Task>> {
        private final Consumer<Task> backingTask;
        private final ScheduledFuture<?> future;

        private final AtomicInteger counter = new AtomicInteger(0);
        private final AtomicBoolean cancelled = new AtomicBoolean(false);

        private HelperAsyncTask(Consumer<Task> backingTask, long delay, TimeUnit delayUnit, long interval, TimeUnit intervalUnit) {
            this.backingTask = backingTask;
            this.future = HelperExecutors.asyncHelper().scheduleAtFixedRate(this, delayUnit.toNanos(delay), intervalUnit.toNanos(interval), TimeUnit.NANOSECONDS);
        }

        @Override
        public void run() {
            if (this.cancelled.get()) {
                return;
            }

            try {
                this.backingTask.accept(this);
                this.counter.incrementAndGet();
            } catch (Throwable e) {
                HelperExceptions.reportScheduler(e);
            }
        }

        @Override
        public int getTimesRan() {
            return this.counter.get();
        }

        @Override
        public boolean stop() {
            if (!this.cancelled.getAndSet(true)) {
                this.future.cancel(false);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getBukkitId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isClosed() {
            return this.cancelled.get();
        }

        @Override
        public Consumer<Task> getDelegate() {
            return this.backingTask;
        }
    }

    private Schedulers() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
