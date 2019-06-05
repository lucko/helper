package me.lucko.helper.sql.util;

import java.util.Objects;
import java.util.function.Function;

/**
 *  * A {@link java.util.function.Function}-copy method which is expected
 *  * to throw an exception.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 * @param <E> the type of error expected to be thrown
 */
@FunctionalInterface
public interface ThrownFunction<T, R, E extends Throwable> {

    /**
     * @see java.util.function.Function#apply(Object)
     *
     * @throws E when the exception is thrown
     */
    R apply(T t) throws E;

    /**
     * @see java.util.function.Function#compose(Function)
     */
    default <V> ThrownFunction<V, R, E> compose(ThrownFunction<? super V, ? extends T, E> before) {
        Objects.requireNonNull(before);
        return v -> this.apply(before.apply(v));
    }

    /**
     * @see java.util.function.Function#andThen(Function)
     */
    default <V> ThrownFunction<T, V, E> andThen(ThrownFunction<? super R, ? extends V, E> after) {
        Objects.requireNonNull(after);
        return t -> after.apply(this.apply(t));
    }

    /**
     * Gets a ThrownFunction which returns the input argument.
     * 
     * @param <R> the type of both the input argument and the result of the function
     * @param <E> the type of error expected to be thrown
     * @return a ThrownFunction which returns the input argument
     */
    static <R, E extends Throwable> ThrownFunction<R, R, E> nothing() {
        return r -> r;
    }
}
