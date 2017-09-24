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

package me.lucko.helper.function.chain;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A simplified version of Java 8's {@link java.util.stream.Stream} API.
 *
 * <p>Chains are <b>not</b> threadsafe or immutable, and should not be reused.
 * They are intended only as a way to reduce boilerplate.</p>
 *
 * <p>Unlike Streams, Chains are only able to process one object.</p>
 *
 * @param <T> the object type
 */
@NonnullByDefault
public interface Chain<T> {

    /**
     * Creates a new chain
     *
     * @param object the initial object
     * @param <T> the object type
     * @return the new chain
     */
    static <T> Chain<T> start(@Nullable T object) {
        return new SimpleChain<>(object);
    }

    /**
     * Creates a new chain
     *
     * @param optional the initial object
     * @param <T> the object type
     * @return the new chain
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Chain<T> startOpt(@Nonnull Optional<T> optional) {
        return new SimpleChain<>(optional.orElse(null));
    }

    /**
     * Applies an action to the backing object
     *
     * @param action the action to apply
     * @return this chain
     */
    Chain<T> apply(Consumer<? super T> action);

    /**
     * Applies an action to the backing object if the test passes.
     *
     * @param test the test
     * @param action the action to apply
     * @return this chain
     */
    Chain<T> applyIf(Predicate<? super T> test, Consumer<? super T> action);

    /**
     * Applies an action to the backing object, if the object is not null.
     *
     * @param action the action to apply
     * @return this chain
     */
    Chain<T> applyIfNonNull(Consumer<? super T> action);

    /**
     * Updates the backing object if the test fails, otherwise returns an identical chain.
     *
     * @param failValue the fail value
     * @return this chain
     */
    Chain<T> orElse(Predicate<? super T> test, T failValue);

    /**
     * Updates the backing object if it is null, otherwise returns an identical chain.
     *
     * @param otherValue the other value
     * @return this chain
     */
    Chain<T> orElseIfNull(T otherValue);

    /**
     * Updates the backing object if the test fails, otherwise returns an identical chain.
     *
     * @param failSupplier the fail supplier
     * @return this chain
     */
    Chain<T> orElseGet(Predicate<? super T> test, Supplier<? extends T> failSupplier);

    /**
     * Updates the backing object if it is null, otherwise returns an identical chain.
     *
     * @param supplier the null supplier
     * @return this chain
     */
    Chain<T> orElseGetIfNull(Supplier<? extends T> supplier);

    /**
     * Returns a new chain instance containing a new value, depending on if the current
     * object passed the given test.
     *
     * @param test the test
     * @param passValue the value to use if the object passes
     * @param failValue the value to use if the object fails
     * @param <R> the resultant chain type
     * @return a new chaining containing one of the new values
     */
    <R> Chain<R> ifElse(Predicate<? super T> test, R passValue, R failValue);

    /**
     * Transforms the backing object, and returns a new chain instance
     * containing the result of the transformation.
     *
     * @param mapper the mapping function
     * @param <R> the resultant chain type
     * @return a new chain containing the result of the mapping
     */
    <R> Chain<R> map(Function<? super T, ? extends R> mapper);

    /**
     * Transforms the backing object, and returns a new chain instance
     * containing the result of the transformation function, if the test is passed.
     * Otherwise, returns a new chain containing the other replacement value.
     *
     * @param test the test
     * @param passedMapper the function to use if the object passes the test
     * @param otherValue the value to use if the object fails the test
     * @param <R> the resultant chain type
     * @return a new chain containing the result of the mapping
     */
    <R> Chain<R> mapOrElse(Predicate<? super T> test, Function<? super T, ? extends R> passedMapper, R otherValue);

    /**
     * Transforms the backing object, and returns a new chain instance
     * containing the result of the chosen transformation function.
     *
     * @param test the test
     * @param passedMapper the function to use if the object passes the test
     * @param failedMapper the function to use if the object fails the test
     * @param <R> the resultant chain type
     * @return a new chain containing the result of the mapping
     */
    <R> Chain<R> mapOrElse(Predicate<? super T> test, Function<? super T, ? extends R> passedMapper, Function<? super T, ? extends R> failedMapper);

    /**
     * Transforms the backing object and returns a new chain instance
     * containing the result of the transformation, if the backing object is
     * not null. Otherwise, returns a new chain containing the other value.
     *
     * @param nonNullMapper the function to use if the object is not null
     * @param otherValue the value to use if the object is null
     * @param <R> the resultant chain type
     * @return a new chain containing the result of the mapping
     */
    <R> Chain<R> mapNullSafe(Function<? super T, ? extends R> nonNullMapper, R otherValue);

    /**
     * Transforms the backing object and returns a new chain instance
     * containing the result of the transformation, if the backing object is
     * not null. Otherwise, returns a new chain containing the result of the supplier.
     *
     * @param nonNullMapper the function to use if the object is not null
     * @param nullSupplier the supplier to use if the object is null
     * @param <R> the resultant chain type
     * @return a new chain containing the result of the mapping
     */
    <R> Chain<R> mapNullSafeGet(Function<? super T, ? extends R> nonNullMapper, Supplier<? extends R> nullSupplier);

    /**
     * Transforms the backing object, and returns a new chain instance
     * containing the result of the transformation.
     *
     * @param mapper the mapping function
     * @param <R> the resultant chain type
     * @return the result of the mapping
     */
    <R> Chain<R> flatMap(Function<? super T, ? extends Chain<? extends R>> mapper);

    /**
     * Gets an optional containing the object backing this chain.
     *
     * @return the object
     */
    Optional<T> end();

    /**
     * Gets the object, or null.
     *
     * @return the object (nullable)
     */
    @Nullable
    T endOrNull();

}
