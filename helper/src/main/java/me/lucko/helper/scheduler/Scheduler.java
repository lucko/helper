package me.lucko.helper.scheduler;

import me.lucko.helper.promise.Promise;
import me.lucko.helper.promise.ThreadContext;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Utility for scheduling tasks
 */
public interface Scheduler extends Executor {

    /**
     * Gets the context this scheduler operates in.
     * 
     * @return the context
     */
    @Nonnull
    ThreadContext getContext();

    /**
     * Compute the result of the passed supplier.
     *
     * @param supplier the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    @Nonnull
    <T> Promise<T> supply(@Nonnull Supplier<T> supplier);

    /**
     * Compute the result of the passed callable.
     *
     * @param callable the callable
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    @Nonnull
    <T> Promise<T> call(@Nonnull Callable<T> callable);

    /**
     * Execute the passed runnable
     *
     * @param runnable the runnable
     * @return a Promise which will return when the runnable is complete
     */
    @Nonnull
    Promise<Void> run(@Nonnull Runnable runnable);

    /**
     * Compute the result of the passed supplier at some point in the future
     *
     * @param supplier the supplier
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    @Nonnull
    <T> Promise<T> supplyLater(@Nonnull Supplier<T> supplier, long delay);

    /**
     * Compute the result of the passed callable at some point in the future
     *
     * @param callable the callable
     * @param delay the delay in ticks before calling the supplier
     * @param <T> the return type
     * @return a Promise which will return the result of the computation
     */
    @Nonnull
    <T> Promise<T> callLater(@Nonnull Callable<T> callable, long delay);

    /**
     * Execute the passed runnable at some point in the future
     *
     * @param runnable the runnable
     * @param delay the delay in ticks before calling the supplier
     * @return a Promise which will return when the runnable is complete
     */
    @Nonnull
    Promise<Void> runLater(@Nonnull Runnable runnable, long delay);

    /**
     * Schedule a repeating task to run
     *
     * @param consumer the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    @Nonnull
    Task runRepeating(@Nonnull Consumer<Task> consumer, long delay, long interval);

    /**
     * Schedule a repeating task to run
     *
     * @param runnable the task to run
     * @param delay the delay before the task begins
     * @param interval the interval at which the task will repeat
     * @return a task instance
     */
    @Nonnull
    Task runRepeating(@Nonnull Runnable runnable, long delay, long interval);

}
