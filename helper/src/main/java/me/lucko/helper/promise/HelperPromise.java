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

import me.lucko.helper.Scheduler;
import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.utils.LoaderUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
class HelperPromise<V> implements Promise<V> {

    @Nonnull
    public static <U> HelperPromise<U> empty() {
        return new HelperPromise<>();
    }

    @Nonnull
    public static <U> HelperPromise<U> completed(@Nullable U value) {
        return new HelperPromise<>(value);
    }

    @Nonnull
    public static <U> HelperPromise<U> exceptionally(@Nonnull Throwable t) {
        return new HelperPromise<>(t);
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
    private final CompletableFuture<V> fut = new CompletableFuture<>();

    private HelperPromise() {}

    private HelperPromise(@Nullable V v) {
        supplied.set(true);
        fut.complete(v);
    }

    private HelperPromise(@Nonnull Throwable t) {
        supplied.set(true);
        fut.completeExceptionally(t);
    }

    /* utility methods */

    private void executeSync(@Nonnull Runnable runnable) {
        if (ThreadContext.forCurrentThread() == ThreadContext.SYNC) {
            runnable.run();
        } else {
            Scheduler.sync().execute(runnable);
        }
    }

    private void executeAsync(@Nonnull Runnable runnable) {
        Scheduler.async().execute(runnable);
    }

    private void executeDelayedSync(@Nonnull Runnable runnable, long delay) {
        if (delay <= 0) {
            executeSync(runnable);
        } else {
            Scheduler.bukkit().runTaskLater(LoaderUtils.getPlugin(), Scheduler.wrapRunnable(runnable), delay);
        }
    }

    private void executeDelayedAsync(@Nonnull Runnable runnable, long delay) {
        if (delay <= 0) {
            executeAsync(runnable);
        } else {
            Scheduler.bukkit().runTaskLaterAsynchronously(LoaderUtils.getPlugin(), Scheduler.wrapRunnable(runnable), delay);
        }
    }

    private boolean complete(V value) {
        return !cancelled.get() && fut.complete(value);
    }

    private boolean completeExceptionally(@Nonnull Throwable t) {
        return fut.completeExceptionally(t);
    }

    private void markAsSupplied() {
        if (!supplied.compareAndSet(false, true)) {
            throw new IllegalStateException("Promise is already being supplied.");
        }
    }

    /* future methods */

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancelled.set(true);
        return fut.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return fut.isCancelled();
    }

    @Override
    public boolean isDone() {
        return fut.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return fut.get();
    }

    @Override
    public V get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return fut.get(timeout, unit);
    }

    @Override
    public V join() {
        return fut.join();
    }

    @Override
    public V getNow(V valueIfAbsent) {
        return fut.getNow(valueIfAbsent);
    }

    @Override
    public CompletableFuture<V> toCompletableFuture() {
        return fut.thenApply(Function.identity());
    }

    /* implementation */

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
    public Promise<V> supplyDelayedSync(@Nonnull Supplier<V> supplier, long delay) {
        markAsSupplied();
        executeDelayedSync(new SupplyRunnable(supplier), delay);
        return this;
    }

    @Nonnull
    @Override
    public Promise<V> supplyDelayedAsync(@Nonnull Supplier<V> supplier, long delay) {
        markAsSupplied();
        executeDelayedAsync(new SupplyRunnable(supplier), delay);
        return this;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplySync(@Nonnull Function<? super V, ? extends U> fn) {
        HelperPromise<U> promise = empty();
        fut.whenComplete((value, t) -> {
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
        fut.whenComplete((value, t) -> {
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
    public <U> Promise<U> thenApplyDelayedSync(@Nonnull Function<? super V, ? extends U> fn, long delay) {
        HelperPromise<U> promise = empty();
        fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ApplyRunnable<>(promise, fn, value), delay);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenApplyDelayedAsync(@Nonnull Function<? super V, ? extends U> fn, long delay) {
        HelperPromise<U> promise = empty();
        fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ApplyRunnable<>(promise, fn, value), delay);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeSync(@Nonnull Function<? super V, ? extends Promise<U>> fn) {
        HelperPromise<U> promise = empty();
        fut.whenComplete((value, t) -> {
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
        fut.whenComplete((value, t) -> {
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
    public <U> Promise<U> thenComposeDelayedSync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay) {
        HelperPromise<U> promise = empty();
        fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedSync(new ComposeRunnable<>(promise, fn, value, true), delay);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public <U> Promise<U> thenComposeDelayedAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay) {
        HelperPromise<U> promise = empty();
        fut.whenComplete((value, t) -> {
            if (t != null) {
                promise.completeExceptionally(t);
            } else {
                executeDelayedAsync(new ComposeRunnable<>(promise, fn, value, false), delay);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallySync(@Nonnull Function<Throwable, ? extends V> fn) {
        HelperPromise<V> promise = empty();
        fut.whenComplete((value, t) -> {
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
        fut.whenComplete((value, t) -> {
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
    public Promise<V> exceptionallyDelayedSync(@Nonnull Function<Throwable, ? extends V> fn, long delay) {
        HelperPromise<V> promise = empty();
        fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedSync(new ExceptionallyRunnable<>(promise, fn, t), delay);
            }
        });
        return promise;
    }

    @Nonnull
    @Override
    public Promise<V> exceptionallyDelayedAsync(@Nonnull Function<Throwable, ? extends V> fn, long delay) {
        HelperPromise<V> promise = empty();
        fut.whenComplete((value, t) -> {
            if (t == null) {
                promise.complete(value);
            } else {
                executeDelayedAsync(new ExceptionallyRunnable<>(promise, fn, t), delay);
            }
        });
        return promise;
    }

    /* delegating behaviour runnables */

    private final class SupplyRunnable implements Runnable, Delegate<Supplier<V>> {
        private final Supplier<V> supplier;
        private SupplyRunnable(Supplier<V> supplier) {
            this.supplier = supplier;
        }
        @Override public Supplier<V> getDelegate() { return supplier; }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                fut.complete(supplier.get());
            } catch (Throwable t) {
                fut.completeExceptionally(t);
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
        @Override public Function getDelegate() { return function; }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                promise.complete(function.apply(value));
            } catch (Throwable e) {
                promise.completeExceptionally(e);
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
        @Override public Function getDelegate() { return function; }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                Promise<U> p = function.apply(value);
                if (p == null) {
                    promise.complete(null);
                } else {
                    if (sync) {
                        p.thenAcceptSync(promise::complete);
                    } else {
                        p.thenAcceptAsync(promise::complete);
                    }
                }
            } catch (Throwable e) {
                promise.completeExceptionally(e);
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
        @Override public Function getDelegate() { return function; }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            try {
                promise.complete(function.apply(t));
            } catch (Throwable e) {
                promise.completeExceptionally(e);
            }
        }
    }

}
