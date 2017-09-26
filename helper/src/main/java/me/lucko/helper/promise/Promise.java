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

import me.lucko.helper.utils.Delegates;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
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
 * {@link java.util.concurrent.CompletionStage} and {@link java.util.concurrent.CompletableFuture}.</p>
 *
 * <p>However, a distinction is made between actions which are executed on
 * the main server thread vs asynchronously.</p>
 *
 * @param <V> the result type
 */
public interface Promise<V> extends Future<V> {

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
     * @param t the exception
     * @param <U> the result type
     * @return the new completed promise
     */
    static <U> Promise<U> exceptionally(@Nonnull Throwable t) {
        return HelperPromise.exceptionally(t);
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
     * @param delay the delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayed(@Nonnull ThreadContext context, @Nonnull Supplier<U> supplier, long delay) {
        Promise<U> p = empty();
        return p.supplyDelayed(context, supplier, delay);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delay the delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayedSync(@Nonnull Supplier<U> supplier, long delay) {
        Promise<U> p = empty();
        return p.supplyDelayedSync(supplier, delay);
    }

    /**
     * Returns a new Promise, and schedules it's population via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the value supplier
     * @param delay the delay
     * @param <U> the result type
     * @return the promise
     */
    @Nonnull
    static <U> Promise<U> supplyingDelayedAsync(@Nonnull Supplier<U> supplier, long delay) {
        Promise<U> p = empty();
        return p.supplyDelayedAsync(supplier, delay);
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
     * @param delay the delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    default Promise<V> supplyDelayed(@Nonnull ThreadContext context, @Nonnull Supplier<V> supplier, long delay) {
        switch (context) {
            case SYNC:
                return supplyDelayedSync(supplier, delay);
            case ASYNC:
                return supplyDelayedAsync(supplier, delay);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delay the delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyDelayedSync(@Nonnull Supplier<V> supplier, long delay);

    /**
     * Schedules the supply of the Promise's result, via the given supplier,
     * after the delay has elapsed.
     *
     * @param supplier the supplier
     * @param delay the delay
     * @return the same promise
     * @throws IllegalStateException if the promise is already being supplied, or has already been completed.
     */
    @Nonnull
    Promise<V> supplyDelayedAsync(@Nonnull Supplier<V> supplier, long delay);

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
     * @param delay the delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenApplyDelayed(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends U> fn, long delay) {
        switch (context) {
            case SYNC:
                return thenApplyDelayedSync(fn, delay);
            case ASYNC:
                return thenApplyDelayedAsync(fn, delay);
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
     * @param delay the delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplyDelayedSync(@Nonnull Function<? super V, ? extends U> fn, long delay);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenApplyDelayedAsync(@Nonnull Function<? super V, ? extends U> fn, long delay);

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
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayed(@Nonnull ThreadContext context, @Nonnull Consumer<? super V> action, long delay) {
        switch (context) {
            case SYNC:
                return thenAcceptDelayedSync(action, delay);
            case ASYNC:
                return thenAcceptDelayedAsync(action, delay);
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
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayedSync(@Nonnull Consumer<? super V> action, long delay) {
        return thenApplyDelayedSync(Delegates.consumerToFunction(action), delay);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * action, after the delay has elapsed.
     *
     * @param action the action to perform before completing the returned future
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenAcceptDelayedAsync(@Nonnull Consumer<? super V> action, long delay) {
        return thenApplyDelayedAsync(Delegates.consumerToFunction(action), delay);
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
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayed(@Nonnull ThreadContext context, @Nonnull Runnable action, long delay) {
        switch (context) {
            case SYNC:
                return thenRunDelayedSync(action, delay);
            case ASYNC:
                return thenRunDelayedAsync(action, delay);
            default:
                throw new AssertionError();
        }
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayedSync(@Nonnull Runnable action, long delay) {
        return thenApplyDelayedSync(Delegates.runnableToFunction(action), delay);
    }

    /**
     * Returns a new Promise that, when this promise completes normally, executes
     * the given task, after the delay has elapsed.
     *
     * @param action the action to run before completing the returned future
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    default Promise<Void> thenRunDelayedAsync(@Nonnull Runnable action, long delay) {
        return thenApplyDelayedAsync(Delegates.runnableToFunction(action), delay);
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
     * @param delay the delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    default <U> Promise<U> thenComposeDelayedSync(@Nonnull ThreadContext context, @Nonnull Function<? super V, ? extends Promise<U>> fn, long delay) {
        switch (context) {
            case SYNC:
                return thenComposeDelayedSync(fn, delay);
            case ASYNC:
                return thenComposeDelayedAsync(fn, delay);
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
     * @param delay the delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeDelayedSync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay);

    /**
     * Returns a new Promise that, when this promise completes normally, is
     * executed with this promise's result as the argument to the given
     * function, after the delay has elapsed.
     *
     * @param fn the function to use to compute the value
     * @param delay the delay
     * @param <U> the result type
     * @return the new promise
     */
    @Nonnull
    <U> Promise<U> thenComposeDelayedAsync(@Nonnull Function<? super V, ? extends Promise<U>> fn, long delay);

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
                return exceptionallySync(fn);
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
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    default Promise<V> exceptionallyDelayed(@Nonnull ThreadContext context, @Nonnull Function<Throwable, ? extends V> fn, long delay) {
        switch (context) {
            case SYNC:
                return exceptionallyDelayedSync(fn, delay);
            case ASYNC:
                return exceptionallyDelayedAsync(fn, delay);
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
     * @param delay the delay
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallyDelayedSync(@Nonnull Function<Throwable, ? extends V> fn, long delay);

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
     * @return the new promise
     */
    @Nonnull
    Promise<V> exceptionallyDelayedAsync(@Nonnull Function<Throwable, ? extends V> fn, long delay);

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
