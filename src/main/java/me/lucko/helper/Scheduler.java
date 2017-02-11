/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me>
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

import com.google.common.base.Preconditions;

import me.lucko.helper.utils.LoaderUtils;
import me.lucko.helper.utils.Terminable;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A utility class to help with scheduling.
 */
public final class Scheduler {
    private static Executor syncExecutor = null;
    private static Executor bukkitAsyncExecutor = null;
    private static ExecutorService asyncExecutor = null;

    /**
     * Get an Executor instance which will execute all passed runnables on the main server thread.
     * @return a "sync" executor instance
     */
    public static synchronized Executor getSyncExecutor() {
        if (syncExecutor == null) {
            syncExecutor = runnable -> LoaderUtils.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(LoaderUtils.getPlugin(), runnable);
        }
        return syncExecutor;
    }

    /**
     * Get an Executor instance which will execute all passed runnables using the Bukkit Scheduler thread pool
     * @return an "async" executor instance
     */
    public static synchronized Executor getAsyncExecutor() {
        if (bukkitAsyncExecutor != null && LoaderUtils.getPlugin().isEnabled()) {
            return bukkitAsyncExecutor;
        }

        if (asyncExecutor == null) {
            asyncExecutor = Executors.newCachedThreadPool();
            getSyncExecutor().execute(() -> {
                bukkitAsyncExecutor = runnable -> LoaderUtils.getPlugin().getServer().getScheduler().runTaskAsynchronously(LoaderUtils.getPlugin(), runnable);
            });
        }

        return asyncExecutor;
    }

    /**
     * Compute the result of the passed supplier on the main thread
     * @param supplier the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> supplySync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return CompletableFuture.supplyAsync(supplier, getSyncExecutor());
    }

    /**
     * Compute the result of the passed supplier asynchronously
     * @param supplier the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return CompletableFuture.supplyAsync(supplier, getAsyncExecutor());
    }

    /**
     * Call a callable on the main server thread
     * @param callable the callable
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> callSync(Callable<T> callable) {
        Preconditions.checkNotNull(callable, "callable");
        return supplySync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Call a callable asynchronously
     * @param callable the callable
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> callAsync(Callable<T> callable) {
        Preconditions.checkNotNull(callable, "callable");
        return supplyAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Execute a runnable on the main server thread
     * @param runnable the runnable
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runSync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return CompletableFuture.runAsync(runnable, getSyncExecutor());
    }

    /**
     * Execute a runnable asynchronously
     * @param runnable the runnable
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return CompletableFuture.runAsync(runnable, getAsyncExecutor());
    }

    /**
     * Compute the result of the passed supplier on the main thread at some point in the future
     * @param supplier the supplier
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> supplyLaterSync(Supplier<T> supplier, long delay) {
        Preconditions.checkNotNull(supplier, "supplier");
        CompletableFuture<T> fut = new CompletableFuture<>();
        LoaderUtils.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(LoaderUtils.getPlugin(), () -> {
            T result = supplier.get();
            fut.complete(result);
        }, delay);

        return fut;
    }

    /**
     * Compute the result of the passed supplier asynchronously at some point in the future
     * @param supplier the supplier
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> supplyLaterAsync(Supplier<T> supplier, long delay) {
        Preconditions.checkNotNull(supplier, "supplier");
        CompletableFuture<T> fut = new CompletableFuture<>();
        LoaderUtils.getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(LoaderUtils.getPlugin(), () -> {
            T result = supplier.get();
            fut.complete(result);
        }, delay);

        return fut;
    }

    /**
     * Call a callable on the main thread at some point in the future
     * @param callable the callable
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> callLaterSync(Callable<T> callable, long delay) {
        Preconditions.checkNotNull(callable, "callable");
        return supplyLaterSync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delay);
    }

    /**
     * Call a callable asynchronously at some point in the future
     * @param callable the callable
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> callLaterAsync(Callable<T> callable, long delay) {
        Preconditions.checkNotNull(callable, "callable");
        return supplyLaterAsync(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delay);
    }

    /**
     * Execute a runnable on the main server thread at some point in the future
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runLaterSync(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        return supplyLaterSync(() -> {
            runnable.run();
            return null;
        }, delay);
    }

    /**
     * Execute a runnable asynchronously at some point in the future
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runLaterAsync(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        return supplyLaterAsync(() -> {
            runnable.run();
            return null;
        }, delay);
    }

    /**
     * Schedule a repeating task to run on the main server thread
     * @param consumer the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingSync(Consumer<Task> consumer, long delay, long interval) {
        Preconditions.checkNotNull(consumer, "consumer");
        TaskImpl task = new TaskImpl(consumer);
        task.runTaskTimer(LoaderUtils.getPlugin(), delay, interval);
        return task;
    }

    /**
     * Schedule a repeating task to run asynchronously
     * @param consumer the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingAsync(Consumer<Task> consumer, long delay, long interval) {
        Preconditions.checkNotNull(consumer, "consumer");
        TaskImpl task = new TaskImpl(consumer);
        task.runTaskTimerAsynchronously(LoaderUtils.getPlugin(), delay, interval);
        return task;
    }

    /**
     * Schedule a repeating task to run on the main server thread
     * @param runnable the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingSync(Runnable runnable, long delay, long interval) {
        Preconditions.checkNotNull(runnable, "runnable");
        return runTaskRepeatingSync(task -> runnable.run(), delay, interval);
    }

    /**
     * Schedule a repeating task to run asynchronously
     * @param runnable the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingAsync(Runnable runnable, long delay, long interval) {
        Preconditions.checkNotNull(runnable, "runnable");
        return runTaskRepeatingAsync(task -> runnable.run(), delay, interval);
    }

    /**
     * Represents a scheduled repeating task
     */
    public interface Task extends Terminable {

        /**
         * Gets the number of times this task has ran. The counter is only incremented at the end of execution.
         * @return the number of times this task has ran
         */
        int getTimesRan();

        /**
         * Stops the task
         * @return true if the task wasn't already cancelled
         */
        boolean stop();

        /**
         * Gets the Bukkit ID for this task
         * @return the bukkit id for this task
         */
        int getBukkitId();

        @Override
        default boolean terminate() {
            return stop();
        }
    }

    private static class TaskImpl extends BukkitRunnable implements Task {
        private final Consumer<Task> backingTask;

        private final AtomicInteger counter = new AtomicInteger(0);
        private final AtomicBoolean shouldStop = new AtomicBoolean(false);

        private TaskImpl(Consumer<Task> backingTask) {
            this.backingTask = backingTask;
        }

        @Override
        public void run() {
            if (shouldStop.get()) {
                cancel();
                return;
            }

            backingTask.accept(this);
            counter.incrementAndGet();

            if (shouldStop.get()) {
                cancel();
            }
        }

        @Override
        public int getTimesRan() {
            return counter.get();
        }

        @Override
        public boolean stop() {
            return !shouldStop.getAndSet(true);
        }

        @Override
        public int getBukkitId() {
            return getTaskId();
        }
    }

    private Scheduler() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
