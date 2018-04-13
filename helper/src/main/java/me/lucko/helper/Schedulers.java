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
import me.lucko.helper.promise.Promise;
import me.lucko.helper.promise.ThreadContext;
import me.lucko.helper.scheduler.HelperExecutors;
import me.lucko.helper.scheduler.Scheduler;
import me.lucko.helper.scheduler.Task;
import me.lucko.helper.scheduler.builder.TaskBuilder;
import me.lucko.helper.timings.Timings;
import me.lucko.helper.utils.Delegates;
import me.lucko.helper.utils.Log;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import co.aikar.timings.lib.MCTiming;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        public <T> Promise<T> supply(@Nonnull Supplier<T> supplier) {
            Objects.requireNonNull(supplier, "supplier");
            return Promise.supplyingSync(supplier);
        }

        @Nonnull
        @Override
        public <T> Promise<T> call(@Nonnull Callable<T> callable) {
            Objects.requireNonNull(callable, "callable");
            return Promise.supplyingSync(Delegates.callableToSupplier(callable));
        }

        @Nonnull
        @Override
        public Promise<Void> run(@Nonnull Runnable runnable) {
            Objects.requireNonNull(runnable, "runnable");
            return Promise.supplyingSync(Delegates.runnableToSupplier(runnable));
        }

        @Nonnull
        @Override
        public <T> Promise<T> supplyLater(@Nonnull Supplier<T> supplier, long delay) {
            Objects.requireNonNull(supplier, "supplier");
            return Promise.supplyingDelayedSync(supplier, delay);
        }

        @Nonnull
        @Override
        public <T> Promise<T> callLater(@Nonnull Callable<T> callable, long delay) {
            Objects.requireNonNull(callable, "callable");
            return Promise.supplyingDelayedSync(Delegates.callableToSupplier(callable), delay);
        }

        @Nonnull
        @Override
        public Promise<Void> runLater(@Nonnull Runnable runnable, long delay) {
            Objects.requireNonNull(runnable, "runnable");
            return Promise.supplyingDelayedSync(Delegates.runnableToSupplier(runnable), delay);
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Consumer<Task> consumer, long delay, long interval) {
            Objects.requireNonNull(consumer, "consumer");
            HelperTask task = new HelperTask(consumer);
            task.runTaskTimer(LoaderUtils.getPlugin(), delay, interval);
            return task;
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Runnable runnable, long delay, long interval) {
            Objects.requireNonNull(runnable, "runnable");
            return runRepeating(Delegates.runnableToConsumer(runnable), delay, interval);
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
        public <T> Promise<T> supply(@Nonnull Supplier<T> supplier) {
            Objects.requireNonNull(supplier, "supplier");
            return Promise.supplyingAsync(supplier);
        }

        @Nonnull
        @Override
        public <T> Promise<T> call(@Nonnull Callable<T> callable) {
            Objects.requireNonNull(callable, "callable");
            return Promise.supplyingAsync(Delegates.callableToSupplier(callable));
        }

        @Nonnull
        @Override
        public Promise<Void> run(@Nonnull Runnable runnable) {
            Objects.requireNonNull(runnable, "runnable");
            return Promise.supplyingAsync(Delegates.runnableToSupplier(runnable));
        }

        @Nonnull
        @Override
        public <T> Promise<T> supplyLater(@Nonnull Supplier<T> supplier, long delay) {
            Objects.requireNonNull(supplier, "supplier");
            return Promise.supplyingDelayedAsync(supplier, delay);
        }

        @Nonnull
        @Override
        public <T> Promise<T> callLater(@Nonnull Callable<T> callable, long delay) {
            Objects.requireNonNull(callable, "callable");
            return Promise.supplyingDelayedAsync(Delegates.callableToSupplier(callable), delay);
        }

        @Nonnull
        @Override
        public Promise<Void> runLater(@Nonnull Runnable runnable, long delay) {
            Objects.requireNonNull(runnable, "runnable");
            return Promise.supplyingDelayedAsync(Delegates.runnableToSupplier(runnable), delay);
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Consumer<Task> consumer, long delay, long interval) {
            Objects.requireNonNull(consumer, "consumer");
            Objects.requireNonNull(consumer, "consumer");
            HelperTask task = new HelperTask(consumer);
            task.runTaskTimerAsynchronously(LoaderUtils.getPlugin(), delay, interval);
            return task;
        }

        @Nonnull
        @Override
        public Task runRepeating(@Nonnull Runnable runnable, long delay, long interval) {
            Objects.requireNonNull(runnable, "runnable");
            return runRepeating(Delegates.runnableToConsumer(runnable), delay, interval);
        }
    }

    private static class HelperTask extends BukkitRunnable implements Task {
        private final Consumer<Task> backingTask;
        private final MCTiming timing;

        private final AtomicInteger counter = new AtomicInteger(0);
        private final AtomicBoolean shouldStop = new AtomicBoolean(false);

        private HelperTask(Consumer<Task> backingTask) {
            this.backingTask = backingTask;
            this.timing = Timings.of("helper-scheduler: " + Delegate.resolve(backingTask).getClass().getName());
        }

        @Override
        public void run() {
            if (this.shouldStop.get()) {
                cancel();
                return;
            }

            try (MCTiming t = this.timing.startTiming()) {
                this.backingTask.accept(this);
                this.counter.incrementAndGet();
            } catch (Throwable e) {
                Log.severe("[SCHEDULER] Exception thrown whilst executing task");
                e.printStackTrace();
            }

            if (this.shouldStop.get()) {
                cancel();
            }
        }

        @Override
        public int getTimesRan() {
            return this.counter.get();
        }

        @Override
        public boolean stop() {
            return !this.shouldStop.getAndSet(true);
        }

        @Override
        public int getBukkitId() {
            return getTaskId();
        }

        @Override
        public boolean isClosed() {
            return this.shouldStop.get();
        }
    }

    private Schedulers() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
