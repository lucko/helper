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

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.promise.ThreadContext;
import me.lucko.helper.scheduler.TaskBuilder;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.timings.Timings;
import me.lucko.helper.utils.Delegates;
import me.lucko.helper.utils.Log;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import co.aikar.timings.lib.MCTiming;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A utility class to help with scheduling.
 */
@NonnullByDefault
public final class Scheduler {

    private static final Executor SYNC_EXECUTOR = new SyncExecutor();
    private static final Executor ASYNC_EXECUTOR_BUKKIT = new BukkitAsyncExecutor();
    private static final ExecutorService ASYNC_EXECUTOR_HELPER = new HelperAsyncExecutor();

    public static final Consumer<Throwable> EXCEPTION_CONSUMER = throwable -> {
        Log.severe("[SCHEDULER] Exception thrown whilst executing task");
        throwable.printStackTrace();
    };

    /**
     * Returns a "sync" executor, which executes all passed runnables on the main server thread.
     *
     * @return a sync executor instance
     */
    public static synchronized Executor sync() {
        return SYNC_EXECUTOR;
    }

    /**
     * Returns an "async" executor, which executes all passed runnables asynchronously
     *
     * @return an async executor instance
     */
    public static synchronized Executor async() {
        return ASYNC_EXECUTOR_HELPER;
    }

    /**
     * Returns a variant of {@link #async()}, which always uses the BukkitScheduler's
     * thread pool to execute tasks.
     *
     * <p>Note: the BukkitScheduler does not allow tasks to be posted if the backing
     * plugin is not enabled. Execution of tasks does not commence until the server has fully started.</p>
     *
     * @return an "async" executor instance
     */
    public static synchronized Executor bukkitAsync() {
        return ASYNC_EXECUTOR_BUKKIT;
    }

    /**
     * Returns a variant of {@link #async()} which uses an internal thread pool
     * to execute tasks.
     *
     * <p>This executor instance is not affected by Bukkit rules, and will start working
     * immediately. (before the server has fully started)</p>
     *
     * @return an "async" executor instance
     */
    public static synchronized ExecutorService internalAsync() {
        return ASYNC_EXECUTOR_HELPER;
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

    /**
     * Compute the result of the passed supplier
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> supply(ThreadContext context, Supplier<T> supplier) {
        Preconditions.checkNotNull(context, "context");
        Preconditions.checkNotNull(supplier, "supplier");
        return Promise.supplying(context, supplier);
    }

    /**
     * Compute the result of the passed supplier on the main thread
     *
     * @param supplier the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> supplySync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return Promise.supplyingSync(supplier);
    }

    /**
     * Compute the result of the passed supplier asynchronously
     *
     * @param supplier the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> supplyAsync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return Promise.supplyingAsync(supplier);
    }

    /**
     * Call a callable
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the callable
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> call(ThreadContext context, Callable<T> callable) {
        Preconditions.checkNotNull(context, "context");
        Preconditions.checkNotNull(callable, "callable");
        return Promise.supplying(context, Delegates.callableToSupplier(callable));
    }

    /**
     * Call a callable on the main server thread
     *
     * @param callable the callable
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> callSync(Callable<T> callable) {
        Preconditions.checkNotNull(callable, "callable");
        return Promise.supplyingSync(Delegates.callableToSupplier(callable));
    }

    /**
     * Call a callable asynchronously
     *
     * @param callable the callable
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> callAsync(Callable<T> callable) {
        Preconditions.checkNotNull(callable, "callable");
        return Promise.supplyingAsync(Delegates.callableToSupplier(callable));
    }

    /**
     * Execute a runnable
     *
     * @param context the type of executor to use to supply the promise
     * @param runnable the runnable
     * @return a Promise which will return when the runnable is complete
     */
    public static Promise<Void> run(ThreadContext context, Runnable runnable) {
        Preconditions.checkNotNull(context, "context");
        Preconditions.checkNotNull(runnable, "runnable");
        return Promise.supplyingSync(Delegates.runnableToSupplier(runnable));
    }

    /**
     * Execute a runnable on the main server thread
     *
     * @param runnable the runnable
     * @return a Promise which will return when the runnable is complete
     */
    public static Promise<Void> runSync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return Promise.supplyingSync(Delegates.runnableToSupplier(runnable));
    }

    /**
     * Execute a runnable asynchronously
     *
     * @param runnable the runnable
     * @return a Promise which will return when the runnable is complete
     */
    public static Promise<Void> runAsync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return Promise.supplyingAsync(Delegates.runnableToSupplier(runnable));
    }

    /**
     * Returns a consumer which when supplied, will delegate calls to the given
     * consumer (a)synchronously.
     *
     * @param context the type of executor to use to supply the delegate consumer
     * @param action the delegate consumer
     * @param <T> the type
     * @return a wrapped consumer
     */
    public static <T> Consumer<T> consuming(ThreadContext context, Consumer<? super T> action) {
        Preconditions.checkNotNull(context, "context");
        switch (context) {
            case SYNC:
                return consumingSync(action);
            case ASYNC:
                return consumingAsync(action);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a consumer which when supplied, will delegate calls to the given
     * consumer on the main server thread.
     *
     * @param action the delegate consumer
     * @param <T> the type
     * @return a wrapped consumer
     */
    public static <T> Consumer<T> consumingSync(Consumer<? super T> action) {
        Promise<T> promise = Promise.empty();
        promise.thenAcceptSync(action);
        return promise::supply;
    }

    /**
     * Returns a consumer which when supplied, will delegate calls to the given
     * consumer asynchronously.
     *
     * @param action the delegate consumer
     * @param <T> the type
     * @return a wrapped consumer
     */
    public static <T> Consumer<T> consumingAsync(Consumer<? super T> action) {
        Promise<T> promise = Promise.empty();
        promise.thenAcceptAsync(action);
        return promise::supply;
    }

    /**
     * Compute the result of the passed supplier at some point in the future
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the supplier
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> supplyLater(ThreadContext context, Supplier<T> supplier, long delay) {
        Preconditions.checkNotNull(context, "context");
        Preconditions.checkNotNull(supplier, "supplier");
        return Promise.supplyingDelayed(context, supplier, delay);
    }

    /**
     * Compute the result of the passed supplier on the main thread at some point in the future
     *
     * @param supplier the supplier
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> supplyLaterSync(Supplier<T> supplier, long delay) {
        Preconditions.checkNotNull(supplier, "supplier");
        return Promise.supplyingDelayedSync(supplier, delay);
    }

    /**
     * Compute the result of the passed supplier asynchronously at some point in the future
     *
     * @param supplier the supplier
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> supplyLaterAsync(Supplier<T> supplier, long delay) {
        Preconditions.checkNotNull(supplier, "supplier");
        return Promise.supplyingDelayedAsync(supplier, delay);
    }

    /**
     * Call a callable at some point in the future
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the callable
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> callLater(ThreadContext context, Callable<T> callable, long delay) {
        Preconditions.checkNotNull(context, "context");
        Preconditions.checkNotNull(callable, "callable");
        return Promise.supplyingDelayed(context, Delegates.callableToSupplier(callable), delay);
    }

    /**
     * Call a callable on the main thread at some point in the future
     *
     * @param callable the callable
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> callLaterSync(Callable<T> callable, long delay) {
        Preconditions.checkNotNull(callable, "callable");
        return Promise.supplyingDelayedSync(Delegates.callableToSupplier(callable), delay);
    }

    /**
     * Call a callable asynchronously at some point in the future
     *
     * @param callable the callable
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    public static <T> Promise<T> callLaterAsync(Callable<T> callable, long delay) {
        Preconditions.checkNotNull(callable, "callable");
        return Promise.supplyingDelayedAsync(Delegates.callableToSupplier(callable), delay);
    }

    /**
     * Execute a runnable at some point in the future
     *
     * @param context the type of executor to use to supply the promise
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a Promise which will return when the runnable is complete
     */
    public static Promise<Void> runLater(ThreadContext context, Runnable runnable, long delay) {
        Preconditions.checkNotNull(context, "context");
        Preconditions.checkNotNull(runnable, "runnable");
        return Promise.supplyingDelayed(context, Delegates.runnableToSupplier(runnable), delay);
    }

    /**
     * Execute a runnable on the main server thread at some point in the future
     *
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a Promise which will return when the runnable is complete
     */
    public static Promise<Void> runLaterSync(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        return Promise.supplyingDelayedSync(Delegates.runnableToSupplier(runnable), delay);
    }

    /**
     * Execute a runnable asynchronously at some point in the future
     *
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a Promise which will return when the runnable is complete
     */
    public static Promise<Void> runLaterAsync(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        return Promise.supplyingDelayedAsync(Delegates.runnableToSupplier(runnable), delay);
    }

    /**
     * Schedule a repeating task to run
     *
     * @param context the type of executor to use to supply the promise
     * @param consumer the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeating(ThreadContext context, Consumer<Task> consumer, long delay, long interval) {
        Preconditions.checkNotNull(context, "context");
        switch (context) {
            case SYNC:
                return runTaskRepeatingSync(consumer, delay, interval);
            case ASYNC:
                return runTaskRepeatingAsync(consumer, delay, interval);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedule a repeating task to run on the main server thread
     *
     * @param consumer the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingSync(Consumer<Task> consumer, long delay, long interval) {
        Preconditions.checkNotNull(consumer, "consumer");
        HelperTask task = new HelperTask(consumer);
        task.runTaskTimer(LoaderUtils.getPlugin(), delay, interval);
        return task;
    }

    /**
     * Schedule a repeating task to run asynchronously
     *
     * @param consumer the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingAsync(Consumer<Task> consumer, long delay, long interval) {
        Preconditions.checkNotNull(consumer, "consumer");
        HelperTask task = new HelperTask(consumer);
        task.runTaskTimerAsynchronously(LoaderUtils.getPlugin(), delay, interval);
        return task;
    }

    /**
     * Schedule a repeating task to run
     *
     * @param context the type of executor to use to supply the promise
     * @param runnable the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeating(ThreadContext context, Runnable runnable, long delay, long interval) {
        Preconditions.checkNotNull(context, "context");
        switch (context) {
            case SYNC:
                return runTaskRepeatingSync(runnable, delay, interval);
            case ASYNC:
                return runTaskRepeatingAsync(runnable, delay, interval);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedule a repeating task to run on the main server thread
     *
     * @param runnable the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingSync(Runnable runnable, long delay, long interval) {
        Preconditions.checkNotNull(runnable, "runnable");
        return runTaskRepeatingSync(Delegates.runnableToConsumer(runnable), delay, interval);
    }

    /**
     * Schedule a repeating task to run asynchronously
     *
     * @param runnable the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    public static Task runTaskRepeatingAsync(Runnable runnable, long delay, long interval) {
        Preconditions.checkNotNull(runnable, "runnable");
        return runTaskRepeatingAsync(Delegates.runnableToConsumer(runnable), delay, interval);
    }

    /**
     * Represents a scheduled repeating task
     */
    @NonnullByDefault
    public interface Task extends Terminable {

        /**
         * Gets the number of times this task has ran. The counter is only incremented at the end of execution.
         *
         * @return the number of times this task has ran
         */
        int getTimesRan();

        /**
         * Stops the task
         *
         * @return true if the task wasn't already cancelled
         */
        boolean stop();

        /**
         * Gets the Bukkit ID for this task
         *
         * @return the bukkit id for this task
         */
        int getBukkitId();

    }

    private static final class SyncExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            bukkit().scheduleSyncDelayedTask(LoaderUtils.getPlugin(), wrapRunnable(runnable));
        }
    }

    private static final class BukkitAsyncExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            bukkit().runTaskAsynchronously(LoaderUtils.getPlugin(), wrapRunnable(runnable));
        }
    }

    private static final class HelperAsyncExecutor extends ThreadPoolExecutor {
        private HelperAsyncExecutor() {
            super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadFactoryBuilder().setNameFormat("helper-scheduler-%d").build());
        }

        @Override
        public void execute(Runnable runnable) {
            super.execute(wrapRunnable(runnable));
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
            if (shouldStop.get()) {
                cancel();
                return;
            }

            try (MCTiming t = timing.startTiming()) {
                backingTask.accept(this);
                counter.incrementAndGet();
            } catch (Throwable t) {
                EXCEPTION_CONSUMER.accept(t);
            }

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

        @Override
        public boolean terminate() {
            return stop();
        }

        @Override
        public boolean hasTerminated() {
            return shouldStop.get();
        }
    }

    public static Runnable wrapRunnable(Runnable runnable) {
        return new SchedulerWrappedRunnable(runnable);
    }

    private static final class SchedulerWrappedRunnable implements Runnable, Delegate<Runnable> {
        private final Runnable delegate;

        private SchedulerWrappedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try (MCTiming t = Timings.ofStart("helper-scheduler: " + Delegate.resolve(delegate).getClass().getName())) {
                delegate.run();
            } catch (Throwable t) {
                EXCEPTION_CONSUMER.accept(t);
            }
        }

        @Override
        public Runnable getDelegate() {
            return delegate;
        }
    }

    private Scheduler() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
