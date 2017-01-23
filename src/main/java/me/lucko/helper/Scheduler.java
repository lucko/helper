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

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Scheduler {
    private static Plugin plugin = null;

    private static Executor syncExecutor = null;
    private static Executor asyncExecutor = null;

    private static synchronized Plugin getPlugin() {
        if (plugin == null) {
            plugin = JavaPlugin.getProvidingPlugin(Scheduler.class);
        }
        return plugin;
    }

    public static synchronized Executor getSyncExecutor() {
        if (syncExecutor == null) {
            syncExecutor = runnable -> getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), runnable);
        }
        return syncExecutor;
    }

    public static synchronized Executor getAsyncExecutor() {
        if (asyncExecutor == null) {
            asyncExecutor = runnable -> getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), runnable);
        }
        return asyncExecutor;
    }

    public static <T> CompletableFuture<T> supplySync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return CompletableFuture.supplyAsync(supplier, getSyncExecutor());
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return CompletableFuture.supplyAsync(supplier, getAsyncExecutor());
    }

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

    public static CompletableFuture<Void> runSync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return CompletableFuture.runAsync(runnable, getSyncExecutor());
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return CompletableFuture.runAsync(runnable, getAsyncExecutor());
    }

    public static <T> CompletableFuture<T> supplySyncLater(Supplier<T> supplier, long delay) {
        Preconditions.checkNotNull(supplier, "supplier");
        CompletableFuture<T> fut = new CompletableFuture<>();
        getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), () -> {
            T result = supplier.get();
            fut.complete(result);
        }, delay);

        return fut;
    }

    public static <T> CompletableFuture<T> supplyAsyncLater(Supplier<T> supplier, long delay) {
        Preconditions.checkNotNull(supplier, "supplier");
        CompletableFuture<T> fut = new CompletableFuture<>();
        getPlugin().getServer().getScheduler().runTaskLaterAsynchronously(getPlugin(), () -> {
            T result = supplier.get();
            fut.complete(result);
        }, delay);

        return fut;
    }

    public static <T> CompletableFuture<T> callSyncLater(Callable<T> callable, long delay) {
        Preconditions.checkNotNull(callable, "callable");
        return supplySyncLater(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delay);
    }

    public static <T> CompletableFuture<T> callAsyncLater(Callable<T> callable, long delay) {
        Preconditions.checkNotNull(callable, "callable");
        return supplyAsyncLater(() -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delay);
    }

    public static CompletableFuture<Void> runSyncLater(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        return supplySyncLater(() -> {
            runnable.run();
            return null;
        }, delay);
    }

    public static CompletableFuture<Void> runAsyncLater(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        return supplyAsyncLater(() -> {
            runnable.run();
            return null;
        }, delay);
    }

    public static Task runTaskSyncRepeating(Consumer<Task> consumer, long delay, long interval) {
        Preconditions.checkNotNull(consumer, "consumer");
        TaskImpl task = new TaskImpl(consumer);
        task.runTaskTimer(getPlugin(), delay, interval);
        return task;
    }

    public static Task runTaskAsyncRepeating(Consumer<Task> consumer, long delay, long interval) {
        Preconditions.checkNotNull(consumer, "consumer");
        TaskImpl task = new TaskImpl(consumer);
        task.runTaskTimerAsynchronously(getPlugin(), delay, interval);
        return task;
    }

    public static Task runTaskSyncRepeating(Runnable runnable, long delay, long interval) {
        Preconditions.checkNotNull(runnable, "runnable");
        return runTaskSyncRepeating(task -> runnable.run(), delay, interval);
    }

    public static Task runTaskAsyncRepeating(Runnable runnable, long delay, long interval) {
        Preconditions.checkNotNull(runnable, "runnable");
        return runTaskAsyncRepeating(task -> runnable.run(), delay, interval);
    }

    public interface Task {

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

    }

    public static class TaskImpl extends BukkitRunnable implements Task {
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
