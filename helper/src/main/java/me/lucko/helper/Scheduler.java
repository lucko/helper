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

import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.timings.Timings;
import me.lucko.helper.utils.LoaderUtils;
import me.lucko.helper.utils.Log;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import co.aikar.timings.lib.MCTiming;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * A utility class to help with scheduling.
 */
@NonnullByDefault
public final class Scheduler {

    private static final Executor SYNC_EXECUTOR = new SyncExecutor();
    private static final Executor BUKKIT_ASYNC_EXECUTOR = new AsyncExecutor();
    private static final ExecutorService ASYNC_EXECUTOR = new FallbackAsyncExecutor();

    private static final Consumer<Throwable> EXCEPTION_CONSUMER = throwable -> {
        Log.severe("[SCHEDULER] Exception thrown whilst executing task");
        throwable.printStackTrace();
    };

    /**
     * Get an Executor instance which will execute all passed runnables on the main server thread.
     * @return a "sync" executor instance
     */
    public static synchronized Executor sync() {
        return SYNC_EXECUTOR;
    }

    /**
     * Get an Executor instance which will execute all passed runnables using the Bukkit scheduler thread pool
     *
     * Does not allow tasks to be posted if the backing plugin is not enabled.
     *
     * @return an "async" executor instance
     */
    public static synchronized Executor bukkitAsync() {
        return BUKKIT_ASYNC_EXECUTOR;
    }

    /**
     * Get an Executor instance which will execute all passed runnables using an internal thread pool
     *
     * @return an "async" executor instance
     */
    public static synchronized ExecutorService internalAsync() {
        return ASYNC_EXECUTOR;
    }

    /**
     * Get an Executor instance which will execute all passed runnables using a thread pool
     *
     * @return an "async" executor instance
     */
    public static synchronized Executor async() {
        return LoaderUtils.getPlugin().isEnabled() ? bukkitAsync() : internalAsync();
    }
    
    public static BukkitScheduler bukkit() {
        return Helper.bukkitScheduler();
    }

    /**
     * Compute the result of the passed supplier on the main thread
     * @param supplier the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> supplySync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return CompletableFuture.supplyAsync(wrapSupplier(supplier), sync());
    }

    /**
     * Compute the result of the passed supplier asynchronously
     * @param supplier the supplier
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        return CompletableFuture.supplyAsync(wrapSupplier(supplier), async());
    }

    /**
     * Call a callable on the main server thread
     * @param callable the callable
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> callSync(Callable<T> callable) {
        Preconditions.checkNotNull(callable, "callable");
        return CompletableFuture.supplyAsync(wrapCallable(callable), sync());
    }

    /**
     * Call a callable asynchronously
     * @param callable the callable
     * @param <T> the return type
     * @return a completable future which will return the result of the computation
     */
    public static <T> CompletableFuture<T> callAsync(Callable<T> callable) {
        Preconditions.checkNotNull(callable, "callable");
        return CompletableFuture.supplyAsync(wrapCallable(callable), async());
    }

    /**
     * Execute a runnable on the main server thread
     * @param runnable the runnable
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runSync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return CompletableFuture.runAsync(wrapRunnable(runnable), sync());
    }

    /**
     * Execute a runnable asynchronously
     * @param runnable the runnable
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        Preconditions.checkNotNull(runnable, "runnable");
        return CompletableFuture.runAsync(wrapRunnable(runnable), async());
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
        HelperFuture<T> fut = new HelperFuture<>(null);
        BukkitTask task = bukkit().runTaskLater(LoaderUtils.getPlugin(), () -> {
            fut.setExecuting();
            try (MCTiming t = Timings.ofStart("helper-scheduler: " + supplier.getClass().getName())) {
                T result = supplier.get();
                fut.complete(result);
            } catch (Throwable t) {
                // print debug info, then pass on to future
                EXCEPTION_CONSUMER.accept(t);
                fut.completeExceptionally(t);
            }
        }, delay);
        fut.setCancelCallback(task::cancel);
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
        HelperFuture<T> fut = new HelperFuture<>(null);
        BukkitTask task = bukkit().runTaskLaterAsynchronously(LoaderUtils.getPlugin(), () -> {
            fut.setExecuting();
            try {
                T result = supplier.get();
                fut.complete(result);
            } catch (Throwable t) {
                // print debug info, then pass on to future
                EXCEPTION_CONSUMER.accept(t);
                fut.completeExceptionally(t);
            }
        }, delay);
        fut.setCancelCallback(task::cancel);
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
        HelperFuture<T> fut = new HelperFuture<>(null);
        BukkitTask task = bukkit().runTaskLater(LoaderUtils.getPlugin(), () -> {
            fut.setExecuting();
            try (MCTiming t = Timings.ofStart("helper-scheduler: " + callable.getClass().getName())) {
                T result = callable.call();
                fut.complete(result);
            } catch (Throwable t) {
                // print debug info, then pass on to future
                EXCEPTION_CONSUMER.accept(t);
                fut.completeExceptionally(t);
            }
        }, delay);
        fut.setCancelCallback(task::cancel);
        return fut;
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
        HelperFuture<T> fut = new HelperFuture<>(null);
        BukkitTask task = bukkit().runTaskLaterAsynchronously(LoaderUtils.getPlugin(), () -> {
            fut.setExecuting();
            try {
                T result = callable.call();
                fut.complete(result);
            } catch (Throwable t) {
                // print debug info, then pass on to future
                EXCEPTION_CONSUMER.accept(t);
                fut.completeExceptionally(t);
            }
        }, delay);
        fut.setCancelCallback(task::cancel);
        return fut;
    }

    /**
     * Execute a runnable on the main server thread at some point in the future
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runLaterSync(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        HelperFuture<Void> fut = new HelperFuture<>(null);
        BukkitTask task = bukkit().runTaskLater(LoaderUtils.getPlugin(), () -> {
            fut.setExecuting();
            try (MCTiming t = Timings.ofStart("helper-scheduler: " + runnable.getClass().getName())) {
                runnable.run();
                fut.complete(null);
            } catch (Throwable t) {
                // print debug info, then pass on to future
                EXCEPTION_CONSUMER.accept(t);
                fut.completeExceptionally(t);
            }
        }, delay);
        fut.setCancelCallback(task::cancel);
        return fut;
    }

    /**
     * Execute a runnable asynchronously at some point in the future
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a completable future which will return when the runnable is complete
     */
    public static CompletableFuture<Void> runLaterAsync(Runnable runnable, long delay) {
        Preconditions.checkNotNull(runnable, "runnable");
        HelperFuture<Void> fut = new HelperFuture<>(null);
        BukkitTask task = bukkit().runTaskLaterAsynchronously(LoaderUtils.getPlugin(), () -> {
            fut.setExecuting();
            try {
                runnable.run();
                fut.complete(null);
            } catch (Throwable t) {
                // print debug info, then pass on to future
                EXCEPTION_CONSUMER.accept(t);
                fut.completeExceptionally(t);
            }
        }, delay);
        fut.setCancelCallback(task::cancel);
        return fut;
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
        return runTaskRepeatingSync(new DelegateConsumer<>(runnable), delay, interval);
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
        return runTaskRepeatingAsync(new DelegateConsumer<>(runnable), delay, interval);
    }

    /**
     * Represents a scheduled repeating task
     */
    @NonnullByDefault
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

    }

    private static <T> Supplier<T> wrapSupplier(Supplier<T> supplier) {
        return new TimingWrappedSupplier<>(supplier);
    }

    private static <T> Supplier<T> wrapCallable(Callable<T> callable) {
        return new TimingWrappedCallable<>(callable);
    }

    private static Runnable wrapRunnable(Runnable runnable) {
        // already wrapped
        if (runnable instanceof TimingWrappedRunnable) {
            return runnable;
        }
        if (runnable instanceof CompletableFuture.AsynchronousCompletionTask) {
            return new WrappedRunnable(runnable);
        }
        return new TimingWrappedRunnable(runnable);
    }

    private static final class SyncExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            bukkit().scheduleSyncDelayedTask(LoaderUtils.getPlugin(), wrapRunnable(runnable));
        }
    }

    private static final class AsyncExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            bukkit().runTaskAsynchronously(LoaderUtils.getPlugin(), wrapRunnable(runnable));
        }
    }

    private static final class FallbackAsyncExecutor extends ThreadPoolExecutor {
        private FallbackAsyncExecutor() {
            // equivalent to calling Executors.newCachedThreadPool()
            super(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<>());
        }

        @Override
        public void execute(Runnable runnable) {
            super.execute(wrapRunnable(runnable));
        }
    }

    private static String getHandlerName(Consumer consumer) {
        if (consumer instanceof DelegateConsumer) {
            return ((DelegateConsumer) consumer).getDelegate().getClass().getName();
        } else {
            return consumer.getClass().getName();
        }
    }

    private static class TaskImpl extends BukkitRunnable implements Task {
        private final Consumer<Task> backingTask;
        private final MCTiming timing;

        private final AtomicInteger counter = new AtomicInteger(0);
        private final AtomicBoolean shouldStop = new AtomicBoolean(false);

        private TaskImpl(Consumer<Task> backingTask) {
            this.backingTask = backingTask;
            this.timing = Timings.of("helper-scheduler: " + getHandlerName(backingTask));
        }

        @Override
        public void run() {
            if (shouldStop.get()) {
                cancel();
                return;
            }

            try {
                try (MCTiming t = timing.startTiming()) {
                    backingTask.accept(this);
                }

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

    private static class HelperFuture<T> extends CompletableFuture<T> implements Terminable {
        @Nullable
        private Runnable cancelCallback = null;

        private boolean cancellable = true;
        private boolean cancelled = false;

        HelperFuture(@Nullable Runnable cancelCallback) {
            super();
            this.cancelCallback = cancelCallback;
        }

        private void setCancelCallback(Runnable cancelCallback) {
            this.cancelCallback = cancelCallback;
        }

        private void setExecuting() {
            this.cancellable = false;
        }

        @Override
        public synchronized boolean cancel(boolean mayInterruptIfRunning) {
            if (cancelled) {
                return true;
            }
            if (!cancellable) {
                return false;
            }

            cancelled = true;
            if (cancelCallback != null) {
                cancelCallback.run();
            }
            return super.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean terminate() {
            return cancel(true);
        }

        @Override
        public boolean hasTerminated() {
            return cancelled;
        }
    }

    private static final class DelegateConsumer<T> implements Consumer<T>, Delegate<Runnable> {
        private final Runnable delegate;

        private DelegateConsumer(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void accept(T t) {
            delegate.run();
        }

        @Override
        public Runnable getDelegate() {
            return delegate;
        }
    }

    private static final class TimingWrappedSupplier<T> implements Supplier<T>, Delegate<Supplier<T>> {
        private final Supplier<T> delegate;

        private TimingWrappedSupplier(Supplier<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            try (MCTiming t = Timings.ofStart("helper-scheduler: " + delegate.getClass().getName())) {
                return delegate.get();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        }

        @Override
        public Supplier<T> getDelegate() {
            return delegate;
        }
    }

    private static final class TimingWrappedCallable<T> implements Supplier<T>, Delegate<Callable<T>> {
        private final Callable<T> delegate;

        private TimingWrappedCallable(Callable<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public T get() {
            try (MCTiming t = Timings.ofStart("helper-scheduler: " + delegate.getClass().getName())) {
                return delegate.call();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        }

        @Override
        public Callable<T> getDelegate() {
            return delegate;
        }
    }

    private static final class TimingWrappedRunnable implements Runnable, Delegate<Runnable> {
        private final Runnable delegate;

        private TimingWrappedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try (MCTiming t = Timings.ofStart("helper-scheduler: " + delegate.getClass().getName())) {
                delegate.run();
            } catch (Throwable t) {
                throw new CompletionException(t);
            }
        }

        @Override
        public Runnable getDelegate() {
            return delegate;
        }
    }

    private static final class WrappedRunnable implements Runnable, Delegate<Runnable> {
        private final Runnable delegate;

        private WrappedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                delegate.run();
            } catch (Throwable t) {
                // print debug info, then re-throw
                EXCEPTION_CONSUMER.accept(t);

                if (t instanceof CompletionException) {
                    throw t;
                } else {
                    throw new CompletionException(t);
                }
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
