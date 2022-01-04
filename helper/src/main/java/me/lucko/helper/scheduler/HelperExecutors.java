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

import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.internal.exception.HelperExceptions;

import org.bukkit.Bukkit;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Provides common {@link Executor} instances.
 */
public final class HelperExecutors {
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
            Bukkit.getScheduler().scheduleSyncDelayedTask(LoaderUtils.getPlugin(), HelperExceptions.wrapSchedulerTask(runnable));
        }
    }

    private static final class BukkitAsyncExecutor implements Executor {
        @Override
        public void execute(Runnable runnable) {
            Bukkit.getScheduler().runTaskAsynchronously(LoaderUtils.getPlugin(), HelperExceptions.wrapSchedulerTask(runnable));
        }
    }

    @Deprecated
    public static Runnable wrapRunnable(Runnable runnable) {
        return HelperExceptions.wrapSchedulerTask(runnable);
    }

    private HelperExecutors() {}

}
