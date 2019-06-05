package me.lucko.helper.sql.util;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * A {@link java.util.function.Consumer}-copy method which is expected
 * to throw an exception.
 *
 * @param <T> the type of the input to the operation
 * @param <E> the type of error expected to be thrown
 */
@FunctionalInterface
public interface ThrownConsumer<T, E extends Throwable> {

    /**
     * @see java.util.function.Consumer#accept(Object)
     *
     * @throws E when the exception is thrown
     */
    void accept(T t) throws E;

    /**
     * @see java.util.function.Consumer#andThen(Consumer)
     */
    default ThrownConsumer<T, E> andThen(ThrownConsumer<? super T, E> after) {
        Objects.requireNonNull(after);
        return t -> {
            this.accept(t);
            after.accept(t);
        };
    }

    /**
     * Gets a ThrownConsumer which does absolutely nothing.
     *
     * @param <T> the type of the input to the operation
     * @param <E> the type of error expected to be thrown
     * @return a ThrownConsumer which does absolutely nothing
     */
    static <T, E extends Throwable> ThrownConsumer<T, E> nothing() {
        return t -> { };
    }
}
