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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.utils.Log;

import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Provides common {@link Executor} instances.
 */
public final class HelperExecutors {
    private static final Consumer<Throwable> EXCEPTION_CONSUMER = throwable -> {
        Log.severe("[SCHEDULER] Exception thrown whilst executing task");
        throwable.printStackTrace();
    };

    private static final Executor SYNC_BUKKIT = new BukkitSyncExecutor();
    private static final Executor ASYNC_BUKKIT = new BukkitAsyncExecutor();
    private static final HelperAsyncExecutor ASYNC_HELPER = new HelperAsyncExecutor();

    public static Executor sync() {
        return SYNC_BUKKIT;
    }

    public static ScheduledExecutorService asyncHelper() {
        return ASYNC_HELPER;
    }

    public static Executor asyncBukkit() {
        return ASYNC_BUKKIT;
    }

    public static void shutdown() {
        ASYNC_HELPER.cancelRepeatingTasks();
    }

    private static final class BukkitSyncExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(LoaderUtils.getPlugin(), wrapRunnable(runnable));
        }
    }

    private static final class BukkitAsyncExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            Bukkit.getScheduler().runTaskAsynchronously(LoaderUtils.getPlugin(), wrapRunnable(runnable));
        }
    }

    private static final class HelperAsyncExecutor extends ScheduledThreadPoolExecutor {
        private final Set<ScheduledFuture<?>> tasks = Collections.newSetFromMap(new WeakHashMap<>());

        private HelperAsyncExecutor() {
            super(16, new ThreadFactoryBuilder().setNameFormat("helper-scheduler-%d").build());
        }

        private ScheduledFuture<?> consumeTask(ScheduledFuture<?> future) {
            this.tasks.add(future);
            return future;
        }

        public void cancelRepeatingTasks() {
            for (ScheduledFuture<?> task : this.tasks) {
                task.cancel(false);
            }
        }

        @Override
        public void execute(Runnable runnable) {
            super.execute(wrapRunnable(runnable));
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
            return consumeTask(super.scheduleAtFixedRate(command, initialDelay, period, unit));
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
            return consumeTask(super.scheduleWithFixedDelay(command, initialDelay, delay, unit));
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
            try {
                this.delegate.run();
            } catch (Throwable t) {
                EXCEPTION_CONSUMER.accept(t);
            }
        }

        @Override
        public Runnable getDelegate() {
            return this.delegate;
        }
    }


    private HelperExecutors() {}

}
