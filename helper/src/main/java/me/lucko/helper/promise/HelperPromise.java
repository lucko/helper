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

package me.lucko.helper.promise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.internal.exception.HelperExceptions;
import me.lucko.helper.scheduler.HelperExecutors;
import me.lucko.helper.scheduler.Ticks;

import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of {@link Promise} using the server scheduler.
 *
 * @param <V> the result type
 */
final class HelperPromise<V> implements Promise<V> {

    @Nonnull
    static <U> HelperPromise<U> empty() {
        return new HelperPromise<>();
    }

    @Nonnull
    static <U> HelperPromise<U> completed(@Nullable U value) {
        return new HelperPromise<>(value);
    }

    @Nonnull
    static <U> HelperPromise<U> exceptionally(@Nonnull Throwable t) {
        return new HelperPromise<>(t);
    }

    @Nonnull
    static <U> Promise<U> wrapFuture(@Nonnull Future<U> future) {
        if (future instanceof CompletableFuture<?>) {
            return new HelperPromise<>(((CompletableFuture<U>) future).thenApply(Function.identity()));

        } else if (future instanceof CompletionStage<?>) {
            //noinspection unchecked
            CompletionStage<U> fut = (CompletionStage<U>) future;
            return new HelperPromise<>(fut.toCompletableFuture().thenApply(Function.identity()));

        } else if (future instanceof ListenableFuture<?>) {
            ListenableFuture<U> fut = (ListenableFuture<U>) future;
            HelperPromise<U> promise = empty();
            promise.supplied.set(true);

            Futures.addCallback(fut, new FutureCallback<U>() {
                @Override
                public void onSuccess(@Nullable U result) {
                    promise.complete(result);
                }

                @Override
                public void onFailure(@Nonnull Throwable t) {
                    promise.completeExceptionally(t);
                }
            });

            return promise;

        } else {
            if (future.isDone()) {
                try {
                    return completed(future.get());
                } catch (ExecutionException e) {
                    return exceptionally(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return Promise.supplyingExceptionallyAsync(future::get);
            }
        }
    }

    /**
     * If the promise is currently being supplied
     */
    private final AtomicBoolean supplied = new AtomicBoolean(false);

    /**
     * If the execution of the promise is cancelled
     */
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    /**
     * The completable future backing this promise
     */
    @Nonnull
    private final CompletableFuture<V> fut;

    private HelperPromise() {
        this.fut = new CompletableFuture<>();
    }

    private HelperPromise(@Nullable V v) {
        this.fut = CompletableFuture.completedFuture(v);
        this.supplied.set(true);
    }

    private HelperPromise(@Nonnull Throwable t) {
        (this.fut = new CompletableFuture<>()).completeExceptionally(t);
        this.supplied.set(true);
    }

    private HelperPromise(@Nonnull CompletableFuture<V> fut) {
        this.fut = Objects.requireNonNull(fut, "future");
        this.supplied.set(true);
        this.cancelled.set(fut.isCancelled());
    }

    /* utility methods */

    private void executeSync(@Nonnull Runnable runnable) {
        if (ThreadContext.forCurrentThread() == ThreadContext.SYNC) {
            HelperExceptions.wrapSchedulerTask(runnable).run();
        } else {
            HelperExecutors.sync().execute(runnable);
        }
    }

    private void executeAsync(@Nonnull Runnable runnable) {
        HelperExecutors.asyncHelper().execute(runnable);
    }

    private void executeDelayedSync(@Nonnull Runnable runnable, long delayTicks) {
        if (delayTicks <= 0) {
            executeSync(runnable);
        } else {
            Bukkit.getScheduler().runTaskLater(LoaderUtils.getPlugin(), HelperExceptions.wrapSchedulerTask(runnable), delayTicks);
        }
    }

    private void executeDelayedAsync(@Nonnull Runnable runnable, long delayTicks) {
        if (delayTicks <= 0) {
            executeAsync(runnable);
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(LoaderUtils.getPlugin(), HelperExceptions.wrapSchedulerTask(runnable), delayTicks);
        }
    }

    private void executeDelayedSync(@Nonnull Runnable runnable, long delay, TimeUnit unit) {
        if (delay <= 0) {
            executeSync(runnable);
        } else {
            Bukkit.getScheduler().runTaskLater(LoaderUtils.getPlugin(), HelperExceptions.wrapSchedulerTask(runnable), Ticks.from(delay, unit));
        }
    }

    private void executeDelayedAsync(@Nonnull Runnable runnable, long delay, TimeUnit unit) {
        if (delay <= 0) {
            executeAsync(runnable);
        } else {
            HelperExecutors.asyncHelper().schedule(HelperExceptions.wrapSchedulerTask(runnable), delay, unit);
        }
    }

    private boolean complete(V value) {
        return !this.cancelled.get() && this.fut.complete(value);
    }

    private boolean completeExceptionally(@Nonnull Throwable t) {
        return !this.cancelled.get() && this.fut.completeExceptionally(t);
    }

    private void markAsSupplied() {
        if (!this.supplied.compareAndSet(false, true)) {
            throw new IllegalStateException("Promise is already being supplied.");
        }
    }

    /* future methods */

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.cancelled.set(true);
        return this.fut.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.fut.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.fut.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return this.fut.get();
    }

    @Override
    public V get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.fut.get(timeout, unit);
    }

    @Override
    public V join() {
        return this.fut.join();
    }

    @Override
    public V getNow(V valueIfAbsent) {
        return this.fut.getNow(valueIfAbsent);
    }

    @Override
    public CompletableFuture<V> toCompletableFuture() {
        return this.fut.thenApply(Function.identity());
    }

    @Override
    public void close() {
        cancel();
    }

    @Override
    public boolean isClosed() {
        return isCancelled();
    }

    /* implementation */

    @Nonnull
    @Override
    public Promise<V> supply(@Nullable V value) {
        markAsSupplied();
        complete(value);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyException(@Nonnull Throwable exception) {
        markAsSupplied();
        completeExceptionally(exception);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplySync(@Nonnull Supplier<V> supplier) {
        markAsSupplied();
        executeSync(new SupplyRunnable(supplier));
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyAsync(@Nonnull Supplier<V> supplier) {
        markAsSupplied();
        executeAsync(new SupplyRunnable(supplier));
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyDelayedSync(@Nonnull Supplier<V> supplier, long delayTicks) {
        markAsSupplied();
        executeDelayedSync(new SupplyRunnable(supplier), delayTicks);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyDelayedSync(@Nonnull Supplier<V> supplier, long delay, @Nonnull TimeUnit unit) {
        markAsSupplied();
        executeDelayedSync(new SupplyRunnable(supplier), delay, unit);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyDelayedAsync(@Nonnull Supplier<V> supplier, long delayTicks) {
        markAsSupplied();
        executeDelayedAsync(new SupplyRunnable(supplier), delayTicks);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyDelayedAsync(@Nonnull Supplier<V> supplier, long delay, @Nonnull TimeUnit unit) {
        markAsSupplied();
        executeDelayedAsync(new SupplyRunnable(supplier), delay, unit);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyExceptionallySync(@Nonnull Callable<V> callable) {
        markAsSupplied();
        executeSync(new ThrowingSupplyRunnable(callable));
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyExceptionallyAsync(@Nonnull Callable<V> callable) {
        markAsSupplied();
        executeAsync(new ThrowingSupplyRunnable(callable));
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyExceptionallyDelayedSync(@Nonnull Callable<V> callable, long delayTicks) {
        markAsSupplied();
        executeDelayedSync(new ThrowingSupplyRunnable(callable), delayTicks);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyExceptionallyDelayedSync(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        markAsSupplied();
        executeDelayedSync(new ThrowingSupplyRunnable(callable), delay, unit);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyExceptionallyDelayedAsync(@Nonnull Callable<V> callable, long delayTicks) {
        markAsSupplied();
        executeDelayedAsync(new ThrowingSupplyRunnable(callable), delayTicks);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyExceptionallyDelayedAsync(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        markAsSupplied();
        executeDelayedAsync(new ThrowingSupplyRunnable(callable), delay, unit);
        return this;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplySync(@Nonnull Function<? super V, ? extends U> fn) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeSync(new ApplyRunnable<>(promise, fn, value));
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplyAsync(@Nonnull Function<? super V, ? extends U> fn) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeAsync(new ApplyRunnable<>(promise, fn, value));
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplyDelayedSync(@Nonnull Function<? super V, ? extends U> fn, long delayTicks) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ApplyRunnable<>(promise, fn, value), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplyDelayedSync(@Nonnull Function<? super V, ? extends U> fn, long delay, @Nonnull TimeUnit unit) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ApplyRunnable<>(promise, fn, value), delay, unit);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplyDelayedAsync(@Nonnull Function<? super V, ? extends U> fn, long delayTicks) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ApplyRunnable<>(promise, fn, value), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplyDelayedAsync(@Nonnull Function<? super V, ? extends U> fn, long delay, @Nonnull TimeUnit unit) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ApplyRunnable<>(promise, fn, value), delay, unit);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeSync(@Nonnull Function<? super V, ? extends Promise<U>> fn) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeSync(new ComposeRunnable<>(promise, fn, value, true));
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeAsync(new ComposeRunnable<>(promise, fn, value, false));
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeDelayedSync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delayTicks) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ComposeRunnable<>(promise, fn, value, true), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeDelayedSync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay, @Nonnull TimeUnit unit) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ComposeRunnable<>(promise, fn, value, true), delay, unit);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeDelayedAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delayTicks) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ComposeRunnable<>(promise, fn, value, false), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeDelayedAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay, @Nonnull TimeUnit unit) {
        HelperPromise<U> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ComposeRunnable<>(promise, fn, value, false), delay, unit);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallySync(@Nonnull Function<Throwable, ? extends V> fn) {
        HelperPromise<V> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeSync(new ExceptionallyRunnable<>(promise, fn, t));
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallyAsync(@Nonnull Function<Throwable, ? extends V> fn) {
        HelperPromise<V> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeAsync(new ExceptionallyRunnable<>(promise, fn, t));
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallyDelayedSync(@Nonnull Function<Throwable, ? extends V> fn, long delayTicks) {
        HelperPromise<V> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedSync(new ExceptionallyRunnable<>(promise, fn, t), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallyDelayedSync(@Nonnull Function<Throwable, ? extends V> fn, long delay, @Nonnull TimeUnit unit) {
        HelperPromise<V> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedSync(new ExceptionallyRunnable<>(promise, fn, t), delay, unit);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallyDelayedAsync(@Nonnull Function<Throwable, ? extends V> fn, long delayTicks) {
        HelperPromise<V> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedAsync(new ExceptionallyRunnable<>(promise, fn, t), delayTicks);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallyDelayedAsync(@Nonnull Function<Throwable, ? extends V> fn, long delay, @Nonnull TimeUnit unit) {
        HelperPromise<V> promise = empty();
        this.fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedAsync(new ExceptionallyRunnable<>(promise, fn, t), delay, unit);
            }
        });
        return promise;
    }

    /* delegating behaviour runnables */

    private final class ThrowingSupplyRunnable implements Runnable, Delegate<Callable<V>> {
        private final Callable<V> supplier;
        private ThrowingSupplyRunnable(Callable<V> supplier) {
            this.supplier = supplier;
        }
        @Override public Callable<V> getDelegate() { return this.supplier; }

        @Override
        public void run() {
            if (HelperPromise.this.cancelled.get()) {
                return;
            }
            try {
                HelperPromise.this.fut.complete(this.supplier.call());
            } catch (Throwable t) {
                HelperExceptions.reportPromise(t);
                HelperPromise.this.fut.completeExceptionally(t);
            }
        }
    }

    private final class SupplyRunnable implements Runnable, Delegate<Supplier<V>> {
        private final Supplier<V> supplier;
        private SupplyRunnable(Supplier<V> supplier) {
            this.supplier = supplier;
        }
        @Override public Supplier<V> getDelegate() { return this.supplier; }

        @Override
        public void run() {
            if (HelperPromise.this.cancelled.get()) {
                return;
            }
            try {
                HelperPromise.this.fut.complete(this.supplier.get());
            } catch (Throwable t) {
                HelperExceptions.reportPromise(t);
                HelperPromise.this.fut.completeExceptionally(t);
            }
        }
    }

    private final class ApplyRunnable<U> implements Runnable, Delegate<Function> {
        private final HelperPromise<U> promise;
        private final Function<? super V, ? extends U> function;
        private final V value;
        private ApplyRunnable(HelperPromise<U> promise, Function<? super V, ? extends U> function, V value) {
            this.promise = promise;
            this.function = function;
            this.value = value;
        }
        @Override public Function getDelegate() { return this.function; }

        @Override
        public void run() {
            if (HelperPromise.this.cancelled.get()) {
                return;
            }
            try {
                this.promise.complete(this.function.apply(this.value));
            } catch (Throwable t) {
                HelperExceptions.reportPromise(t);
                this.promise.completeExceptionally(t);
            }
        }
    }

    private final class ComposeRunnable<U> implements Runnable, Delegate<Function> {
        private final HelperPromise<U> promise;
        private final Function<? super V, ? extends Promise<U>> function;
        private final V value;
        private final boolean sync;
        private ComposeRunnable(HelperPromise<U> promise, Function<? super V, ? extends Promise<U>> function, V value, boolean sync) {
            this.promise = promise;
            this.function = function;
            this.value = value;
            this.sync = sync;
        }
        @Override public Function getDelegate() { return this.function; }

        @Override
        public void run() {
            if (HelperPromise.this.cancelled.get()) {
                return;
            }
            try {
                Promise<U> p = this.function.apply(this.value);
                if (p == null) {
                    this.promise.complete(null);
                } else {
                    if (this.sync) {
                        p.thenAcceptSync(this.promise::complete);
                    } else {
                        p.thenAcceptAsync(this.promise::complete);
                    }
                }
            } catch (Throwable t) {
                HelperExceptions.reportPromise(t);
                this.promise.completeExceptionally(t);
            }
        }
    }

    private final class ExceptionallyRunnable<U> implements Runnable, Delegate<Function> {
        private final HelperPromise<U> promise;
        private final Function<Throwable, ? extends U> function;
        private final Throwable t;
        private ExceptionallyRunnable(HelperPromise<U> promise, Function<Throwable, ? extends U> function, Throwable t) {
            this.promise = promise;
            this.function = function;
            this.t = t;
        }
        @Override public Function getDelegate() { return this.function; }

        @Override
        public void run() {
            if (HelperPromise.this.cancelled.get()) {
                return;
            }
            try {
                this.promise.complete(this.function.apply(this.t));
            } catch (Throwable t) {
                HelperExceptions.reportPromise(t);
                this.promise.completeExceptionally(t);
            }
        }
    }

}
