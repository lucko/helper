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

import me.lucko.helper.internal.exception.HelperExceptions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

final class HelperAsyncExecutor extends AbstractExecutorService implements ScheduledExecutorService {
    private final ExecutorService taskService;
    private final ScheduledExecutorService timerExecutionService;

    private final Set<ScheduledFuture<?>> tasks = Collections.newSetFromMap(new WeakHashMap<>());

    HelperAsyncExecutor() {
        this.taskService = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("helper-scheduler-%d")
                .build()
        );
        this.timerExecutionService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("helper-scheduler-timer")
                .build()
        );
    }

    private ScheduledFuture<?> consumeTask(ScheduledFuture<?> future) {
        synchronized (this.tasks) {
            this.tasks.add(future);
        }
        return future;
    }

    public void cancelRepeatingTasks() {
        synchronized (this.tasks) {
            for (ScheduledFuture<?> task : this.tasks) {
                task.cancel(false);
            }
        }
    }

    @Override
    public void execute(Runnable runnable) {
        this.taskService.execute(HelperExceptions.wrapSchedulerTask(runnable));
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Runnable delegate = HelperExceptions.wrapSchedulerTask(command);
        return consumeTask(this.timerExecutionService.schedule(() -> this.taskService.execute(delegate), delay, unit));
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return consumeTask(this.timerExecutionService.scheduleAtFixedRate(new FixedRateWorker(HelperExceptions.wrapSchedulerTask(command)), initialDelay, period, unit));
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduleAtFixedRate(command, initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        // noop
    }

    @Override
    public List<Runnable> shutdownNow() {
        // noop
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        throw new IllegalStateException("Not shutdown");
    }

    private final class FixedRateWorker implements Runnable {
        private final Runnable delegate;
        private final ReentrantLock lock = new ReentrantLock();
        private final AtomicInteger running = new AtomicInteger(0);

        private FixedRateWorker(Runnable delegate) {
            this.delegate = delegate;
        }

        // the purpose of 'lock' and 'running' is to prevent concurrent
        // execution on the underlying delegate runnable.
        // only one instance of the worker will "wait" for the previous task to finish

        @Override
        public void run() {
            // assuming a task that takes a really long time:
            // first call: running=1 - we want to run
            // second call: running=2 - we want to wait
            // third call: running=3 - assuming second is still waiting, we want to cancel
            if (this.running.incrementAndGet() > 2) {
                this.running.decrementAndGet();
                return;
            }

            HelperAsyncExecutor.this.taskService.execute(() -> {
                this.lock.lock();
                try {
                    this.delegate.run();
                } finally {
                    this.lock.unlock();
                    this.running.decrementAndGet();
                }
            });
        }
    }
}
