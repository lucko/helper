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

import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.utils.Delegates;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An object that acts as a proxy for a result that is initially unknown,
 * usually because the computation of its value is yet incomplete.
 *
 * <p>This interface carries similar method signatures to those of
 * {@link java.util.concurrent.CompletionStage} and {@link CompletableFuture}.</p>
 *
 * <p>However, a distinction is made between actions which are executed on
 * the main server thread vs asynchronously.</p>
 *
 * @param <V> the result type
 */
public interface Promise<V> extends Future<V>, Terminable {

    /**
     * Returns a new empty Promise
     *
     * <p>An empty promise can be 'completed' via the supply methods.</p>
     *
     * @param <U> the result type
     * @return a new empty promise
     */
    @Nonnull
    static <U> Promise<U> empty() {
        return HelperPromise.empty();
    }

    /**
     * Returns a new base promise to be built on top of.
     *
     * @return a new promise
     */
    @Nonnull
    static Promise<Void> start() {
        return HelperPromise.completed(null);
    }

    /**
     * Returns a Promise which is already completed with the given value.
     *
     * @param value the value
     * @param <U> the result type
     * @return a new completed promise
     */
    @Nonnull
    static <U> Promise<U> completed(@Nullable U value) {
        return HelperPromise.completed(value);
    }

    /**
     * Returns a Promise which is already completed with the given exception.
     *
     * @param exception the exception
     * @param <U> the result type
     * @return the new completed promise
     */
    @Nonnull
    static <U> Promise<U> exceptionally(@Nonnull Throwable exception) {
        return HelperPromise.exceptionally(exception);
    }

    /**
     * Returns a Promise which represents the given future.
     *
     * <p>The implementation will make an attempt to wrap the future without creating a new process
     * to await the result (by casting to {@link java.util.concurrent.CompletionStage} or
     * {@link com.google.common.util.concurrent.ListenableFuture}).</p>
     *
     * <p>Calls to {@link #cancel() cancel} the returned promise will not affected the wrapped
     * future.</p>
     *
     * @param future the future to wrap
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    static <U> Promise<U> wrapFuture(@Nonnull Future<U> future) {
        return HelperPromise.wrapFuture(future);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier.
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the value supplier
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplying(@Nonnull ThreadContext context, @Nonnull Supplier<U> supplier) {
        Promise<U> p = empty();
        return p.supply(context, supplier);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier.
     *
     * @param supplier the value supplier
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingSync(@Nonnull Supplier<U> supplier) {
        Promise<U> p = empty();
        return p.supplySync(supplier);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier.
     *
     * @param supplier the value supplier
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingAsync(@Nonnull Supplier<U> supplier) {
        Promise<U> p = empty();
        return p.supplyAsync(supplier);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the value supplier
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayed(@Nonnull ThreadContext context, @Nonnull Supplier<U> supplier, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyDelayed(context, supplier, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the value supplier
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayed(@Nonnull ThreadContext context, @Nonnull Supplier<U> supplier, long delay, @Nonnull TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyDelayed(context, supplier, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayedSync(@Nonnull Supplier<U> supplier, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyDelayedSync(supplier, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayedSync(@Nonnull Supplier<U> supplier, long delay, @Nonnull TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyDelayedSync(supplier, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayedAsync(@Nonnull Supplier<U> supplier, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyDelayedAsync(supplier, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayedAsync(@Nonnull Supplier<U> supplier, long delay, @Nonnull TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyDelayedAsync(supplier, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable.
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the value callable
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionally(@Nonnull ThreadContext context, @Nonnull Callable<U> callable) {
        Promise<U> p = empty();
        return p.supplyExceptionally(context, callable);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable.
     *
     * @param callable the value callable
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallySync(@Nonnull Callable<U> callable) {
        Promise<U> p = empty();
        return p.supplyExceptionallySync(callable);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable.
     *
     * @param callable the value callable
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallyAsync(@Nonnull Callable<U> callable) {
        Promise<U> p = empty();
        return p.supplyExceptionallyAsync(callable);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the value callable
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallyDelayed(@Nonnull ThreadContext context, @Nonnull Callable<U> callable, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayed(context, callable, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the value callable
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallyDelayed(@Nonnull ThreadContext context, @Nonnull Callable<U> callable, long delay, @Nonnull TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayed(context, callable, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the value callable
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallyDelayedSync(@Nonnull Callable<U> callable, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedSync(callable, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the value callable
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallyDelayedSync(@Nonnull Callable<U> callable, long delay, @Nonnull TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedSync(callable, delay, unit);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the value callable
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallyDelayedAsync(@Nonnull Callable<U> callable, long delayTicks) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedAsync(callable, delayTicks);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the value callable
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingExceptionallyDelayedAsync(@Nonnull Callable<U> callable, long delay, @Nonnull TimeUnit unit) {
        Promise<U> p = empty();
        return p.supplyExceptionallyDelayedAsync(callable, delay, unit);
    }
    
    /**
     * Attempts to cancel execution of this task.
     *
     * @return {@code false} if the task could not be cancelled, typically
     * because it has already completed normally;
     * {@code true} otherwise
     */
    default boolean cancel() {
        return cancel(true);
    }

    /**
     * Returns the result value when complete, or throws an
     * (unchecked) exception if completed exceptionally.
     *
     * <p>To better conform with the use of common functional forms, if a
     * computation involved in the completion of this
     * Promise threw an exception, this method throws an
     * (unchecked) {@link CompletionException} with the underlying
     * exception as its cause.</p>
     *
     * @return the result value
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this future completed
     * exceptionally or a completion computation threw an exception
     */
    V join();

    /**
     * Returns the result value (or throws any encountered exception)
     * if completed, else returns the given valueIfAbsent.
     *
     * @param valueIfAbsent the value to return if not completed
     * @return the result value, if completed, else the given valueIfAbsent
     * @throws CancellationException if the computation was cancelled
     * @throws CompletionException if this future completed
     * exceptionally or a completion computation threw an exception
     */
    V getNow(V valueIfAbsent);

    /**
     * Supplies the Promise's result.
     *
     * @param value the object to pass to the promise
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supply(@Nullable V value);

    /**
     * Supplies an exceptional result to the Promise.
     *
     * @param exception the exception to supply
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyException(@Nonnull Throwable exception);

    /**
     * Schedules the supply of the Promise's result, via the given supplier.
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the supplier
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    default Promise<V> supply(@Nonnull ThreadContext context, @Nonnull Supplier<V> supplier) {
        switch (context) {
            case SYNC:
                return supplySync(supplier);
            case ASYNC:
                return supplyAsync(supplier);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given supplier.
     *
     * @param supplier the supplier
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplySync(@Nonnull Supplier<V> supplier);

    /**
     * Schedules the supply of the Promise's result, via the given supplier.
     *
     * @param supplier the supplier
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyAsync(@Nonnull Supplier<V> supplier);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the supplier
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    default Promise<V> supplyDelayed(@Nonnull ThreadContext context, @Nonnull Supplier<V> supplier, long delayTicks) {
        switch (context) {
            case SYNC:
                return supplyDelayedSync(supplier, delayTicks);
            case ASYNC:
                return supplyDelayedAsync(supplier, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param supplier the supplier
     * @param delay the delay
     * @param unit the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    default Promise<V> supplyDelayed(@Nonnull ThreadContext context, @Nonnull Supplier<V> supplier, long delay, @Nonnull TimeUnit unit) {
        switch (context) {
            case SYNC:
                return supplyDelayedSync(supplier, delay, unit);
            case ASYNC:
                return supplyDelayedAsync(supplier, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyDelayedSync(@Nonnull Supplier<V> supplier, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delay the delay
     * @param unit the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyDelayedSync(@Nonnull Supplier<V> supplier, long delay, @Nonnull TimeUnit unit);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyDelayedAsync(@Nonnull Supplier<V> supplier, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delay the delay
     * @param unit the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyDelayedAsync(@Nonnull Supplier<V> supplier, long delay, @Nonnull TimeUnit unit);

    /**
     * Schedules the supply of the Promise's result, via the given callable.
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the callable
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    default Promise<V> supplyExceptionally(@Nonnull ThreadContext context, @Nonnull Callable<V> callable) {
        switch (context) {
            case SYNC:
                return supplyExceptionallySync(callable);
            case ASYNC:
                return supplyExceptionallyAsync(callable);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given callable.
     *
     * @param callable the callable
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyExceptionallySync(@Nonnull Callable<V> callable);

    /**
     * Schedules the supply of the Promise's result, via the given callable.
     *
     * @param callable the callable
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyExceptionallyAsync(@Nonnull Callable<V> callable);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the callable
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    default Promise<V> supplyExceptionallyDelayed(@Nonnull ThreadContext context, @Nonnull Callable<V> callable, long delayTicks) {
        switch (context) {
            case SYNC:
                return supplyExceptionallyDelayedSync(callable, delayTicks);
            case ASYNC:
                return supplyExceptionallyDelayedAsync(callable, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param callable the callable
     * @param delay the delay
     * @param unit the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    default Promise<V> supplyExceptionallyDelayed(@Nonnull ThreadContext context, @Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        switch (context) {
            case SYNC:
                return supplyExceptionallyDelayedSync(callable, delay, unit);
            case ASYNC:
                return supplyExceptionallyDelayedAsync(callable, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the callable
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyExceptionallyDelayedSync(@Nonnull Callable<V> callable, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the callable
     * @param delay the delay
     * @param unit the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyExceptionallyDelayedSync(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the callable
     * @param delayTicks the delay in ticks
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyExceptionallyDelayedAsync(@Nonnull Callable<V> callable, long delayTicks);

    /**
     * Schedules the supply of the Promise's result, via the given callable,
     * after the delay has elapsed.
     *
     * @param callable the callable
     * @param delay the delay
     * @param unit the unit of delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyExceptionallyDelayedAsync(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenApply(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends U> fn) {
        switch (context) {
            case SYNC:
                return thenApplySync(fn);
            case ASYNC:
                return thenApplyAsync(fn);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplySync(@Nonnull Function<? super V, ? extends U> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplyAsync(@Nonnull Function<? super V, ? extends U> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenApplyDelayed(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends U> fn, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenApplyDelayedSync(fn, delayTicks);
            case ASYNC:
                return thenApplyDelayedAsync(fn, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenApplyDelayed(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends U> fn, long delay, @Nonnull TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenApplyDelayedSync(fn, delay, unit);
            case ASYNC:
                return thenApplyDelayedAsync(fn, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplyDelayedSync(@Nonnull Function<? super V, ? extends U> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplyDelayedSync(@Nonnull Function<? super V, ? extends U> fn, long delay, @Nonnull TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplyDelayedAsync(@Nonnull Function<? super V, ? extends U> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplyDelayedAsync(@Nonnull Function<? super V, ? extends U> fn, long delay, @Nonnull TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action.
     *
     * @param context the type of executor to use to supply the promise
     * @param action the action to perform before completing the returned future
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAccept(@Nonnull ThreadContext context, @Nonnull Consumer<? super V> action) {
        switch (context) {
            case SYNC:
                return thenAcceptSync(action);
            case ASYNC:
                return thenAcceptAsync(action);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action.
     *
     * @param action the action to perform before completing the returned future
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptSync(@Nonnull Consumer<? super V> action) {
        return thenApplySync(Delegates.consumerToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action.
     *
     * @param action the action to perform before completing the returned future
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptAsync(@Nonnull Consumer<? super V> action) {
        return thenApplyAsync(Delegates.consumerToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param action the action to perform before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayed(@Nonnull ThreadContext context, @Nonnull Consumer<? super V> action, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenAcceptDelayedSync(action, delayTicks);
            case ASYNC:
                return thenAcceptDelayedAsync(action, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param action the action to perform before completing the returned future
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayed(@Nonnull ThreadContext context, @Nonnull Consumer<? super V> action, long delay, @Nonnull TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenAcceptDelayedSync(action, delay, unit);
            case ASYNC:
                return thenAcceptDelayedAsync(action, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action the action to perform before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayedSync(@Nonnull Consumer<? super V> action, long delayTicks) {
        return thenApplyDelayedSync(Delegates.consumerToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action the action to perform before completing the returned future
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayedSync(@Nonnull Consumer<? super V> action, long delay, @Nonnull TimeUnit unit) {
        return thenApplyDelayedSync(Delegates.consumerToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action the action to perform before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayedAsync(@Nonnull Consumer<? super V> action, long delayTicks) {
        return thenApplyDelayedAsync(Delegates.consumerToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action the action to perform before completing the returned future
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayedAsync(@Nonnull Consumer<? super V> action, long delay, @Nonnull TimeUnit unit) {
        return thenApplyDelayedAsync(Delegates.consumerToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task.
     *
     * @param context the type of executor to use to supply the promise
     * @param action the action to run before completing the returned future
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRun(@Nonnull ThreadContext context, @Nonnull Runnable action) {
        switch (context) {
            case SYNC:
                return thenRunSync(action);
            case ASYNC:
                return thenRunAsync(action);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task.
     *
     * @param action the action to run before completing the returned future
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunSync(@Nonnull Runnable action) {
        return thenApplySync(Delegates.runnableToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task.
     *
     * @param action the action to run before completing the returned future
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunAsync(@Nonnull Runnable action) {
        return thenApplyAsync(Delegates.runnableToFunction(action));
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param action the action to run before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayed(@Nonnull ThreadContext context, @Nonnull Runnable action, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenRunDelayedSync(action, delayTicks);
            case ASYNC:
                return thenRunDelayedAsync(action, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param action the action to run before completing the returned future
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayed(@Nonnull ThreadContext context, @Nonnull Runnable action, long delay, @Nonnull TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenRunDelayedSync(action, delay, unit);
            case ASYNC:
                return thenRunDelayedAsync(action, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayedSync(@Nonnull Runnable action, long delayTicks) {
        return thenApplyDelayedSync(Delegates.runnableToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayedSync(@Nonnull Runnable action, long delay, @Nonnull TimeUnit unit) {
        return thenApplyDelayedSync(Delegates.runnableToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayedAsync(@Nonnull Runnable action, long delayTicks) {
        return thenApplyDelayedAsync(Delegates.runnableToFunction(action), delayTicks);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayedAsync(@Nonnull Runnable action, long delay, @Nonnull TimeUnit unit) {
        return thenApplyDelayedAsync(Delegates.runnableToFunction(action), delay, unit);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenCompose(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends Promise<U>> fn) {
        switch (context) {
            case SYNC:
                return thenComposeSync(fn);
            case ASYNC:
                return thenComposeAsync(fn);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeSync(@Nonnull Function<? super V, ? extends Promise<U>> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function.
     *
     * @param fn the function to use to compute the value
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenComposeDelayedSync(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends Promise<U>> fn, long delayTicks) {
        switch (context) {
            case SYNC:
                return thenComposeDelayedSync(fn, delayTicks);
            case ASYNC:
                return thenComposeDelayedAsync(fn, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenComposeDelayedSync(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends Promise<U>> fn, long delay, @Nonnull TimeUnit unit) {
        switch (context) {
            case SYNC:
                return thenComposeDelayedSync(fn, delay, unit);
            case ASYNC:
                return thenComposeDelayedAsync(fn, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeDelayedSync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeDelayedSync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay, @Nonnull TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delayTicks the delay in ticks
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeDelayedAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param unit the unit of delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeDelayedAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay, @Nonnull TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function. Otherwise, if this promise completes normally, then the
     * returned promise also completes normally with the same value.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @return the new promise
     */
    @Nonnull
    default Promise<V> exceptionally(@Nonnull ThreadContext context, @Nonnull Function<Throwable, ? extends V> fn) {
        switch (context) {
            case SYNC:
                return exceptionallySync(fn);
            case ASYNC:
                return exceptionallyAsync(fn);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function. Otherwise, if this promise completes normally, then the
     * returned promise also completes normally with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallySync(@Nonnull Function<Throwable, ? extends V> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function. Otherwise, if this promise completes normally, then the
     * returned promise also completes normally with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallyAsync(@Nonnull Function<Throwable, ? extends V> fn);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    default Promise<V> exceptionallyDelayed(@Nonnull ThreadContext context, @Nonnull Function<Throwable, ? extends V> fn, long delayTicks) {
        switch (context) {
            case SYNC:
                return exceptionallyDelayedSync(fn, delayTicks);
            case ASYNC:
                return exceptionallyDelayedAsync(fn, delayTicks);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param context the type of executor to use to supply the promise
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    default Promise<V> exceptionallyDelayed(@Nonnull ThreadContext context, @Nonnull Function<Throwable, ? extends V> fn, long delay, @Nonnull TimeUnit unit) {
        switch (context) {
            case SYNC:
                return exceptionallyDelayedSync(fn, delay, unit);
            case ASYNC:
                return exceptionallyDelayedAsync(fn, delay, unit);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallyDelayedSync(@Nonnull Function<Throwable, ? extends V> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallyDelayedSync(@Nonnull Function<Throwable, ? extends V> fn, long delay, @Nonnull TimeUnit unit);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @param delayTicks the delay in ticks
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallyDelayedAsync(@Nonnull Function<Throwable, ? extends V> fn, long delayTicks);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's exception as the argument to the given
     * function, after the delay has elapsed. Otherwise, if this promise
     * completes normally, then the returned promise also completes normally
     * with the same value.
     *
     * @param fn the function to use to compute the value of the returned
     *           Promise, if this promise completed exceptionally
     * @param delay the delay
     * @param unit the unit of delay
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallyDelayedAsync(@Nonnull Function<Throwable, ? extends V> fn, long delay, @Nonnull TimeUnit unit);


    /**
     * Returns a {@link CompletableFuture} maintaining the same
     * completion properties as this Promise.
     *
     * A Promise implementation that does not choose to interoperate
     * with CompletableFutures may throw {@code UnsupportedOperationException}.
     *
     * @return the CompletableFuture
     * @throws UnsupportedOperationException if this implementation
     * does not interoperate with CompletableFuture
     */
    CompletableFuture<V> toCompletableFuture();

}
