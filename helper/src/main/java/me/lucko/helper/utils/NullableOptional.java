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

package me.lucko.helper.utils;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A container object which may or may not contain a value.
 *
 * <p>An extension of Java's {@link java.util.Optional}, which can hold
 * nullable values.</p>
 */
public final class NullableOptional<T> {

    /**
     * Common instance for {@code empty()}.
     */
    private static final NullableOptional<?> EMPTY = new NullableOptional<>();

    /**
     * Returns an empty {@code NullableOptional} instance. No value is present for this
     * NullableOptional.
     *
     * @param <T> Type of the non-existent value
     * @return an empty {@code NullableOptional}
     */
    @Nonnull
    public static<T> NullableOptional<T> empty() {
        @SuppressWarnings("unchecked")
        NullableOptional<T> t = (NullableOptional<T>) EMPTY;
        return t;
    }

    /**
     * Returns a {@code NullableOptional} with the specified present value.
     *
     * @param <T> the class of the value
     * @param value the value to be present
     * @return a {@code NullableOptional} with the value present
     */
    @Nonnull
    public static <T> NullableOptional<T> of(@Nullable T value) {
        return new NullableOptional<>(value);
    }

    /**
     * Returns a {@code NullableOptional} describing the specified optional.
     *
     * @param <T> the class of the value
     * @param value the possibly-empty value to describe
     * @return a {@code NullableOptional} with a present value if the specified value
     * is present, otherwise an empty {@code NullableOptional}
     */
    @Nonnull
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> NullableOptional<T> fromOptional(@Nonnull Optional<T> value) {
        //noinspection OptionalIsPresent
        return value.isPresent() ? of(value.get()) : empty();
    }

    private final T value;
    private final boolean isPresent;

    /**
     * Constructs an empty instance.
     */
    private NullableOptional() {
        this.value = null;
        this.isPresent = false;
    }

    /**
     * Constructs an instance with the value present.
     *
     * @param value the value
     */
    private NullableOptional(@Nullable T value) {
        this.value = value;
        this.isPresent = true;
    }

    /**
     * If a value is present in this {@code NullableOptional}, returns the value,
     * otherwise throws {@code NoSuchElementException}.
     *
     * @return the value held by this {@code NullableOptional}
     * @throws NoSuchElementException if there is no value present
     *
     * @see NullableOptional#isPresent()
     */
    @Nullable
    public T get() {
        if (!isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        return this.value;
    }

    /**
     * If a value is present in this {@link NullableOptional}, returns the result
     * of passing the value to {@link Optional#ofNullable(Object)}. Otherwise returns
     * an empty optional.
     *
     * @return an {@link Optional} representing the value held by this {@link NullableOptional}
     *
     * @see Optional#ofNullable(Object)
     */
    @Nonnull
    public Optional<T> asOptional() {
        return isPresent() ? Optional.ofNullable(this.value) : Optional.empty();
    }

    /**
     * Return {@code true} if there is a value present, otherwise {@code false}.
     *
     * @return {@code true} if there is a value present, otherwise {@code false}
     */
    public boolean isPresent() {
        return this.isPresent;
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param consumer block to be executed if a value is present
     * @throws NullPointerException if value is present and {@code consumer} is
     * null
     */
    public void ifPresent(@Nonnull Consumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(this.value);
        }
    }

    /**
     * If a value is present, and the value matches the given predicate,
     * return an {@code NullableOptional} describing the value, otherwise return an
     * empty {@code NullableOptional}.
     *
     * @param predicate a predicate to apply to the value, if present
     * @return an {@code NullableOptional} describing the value of this {@code NullableOptional}
     * if a value is present and the value matches the given predicate,
     * otherwise an empty {@code NullableOptional}
     * @throws NullPointerException if the predicate is null
     */
    @Nonnull
    public NullableOptional<T> filter(@Nonnull Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (!isPresent()) {
            return this;
        } else {
            return predicate.test(this.value) ? this : empty();
        }
    }

    /**
     * If a value is present, apply the provided mapping function to it,
     * and return an {@code NullableOptional} describing the
     * result. Otherwise return an empty {@code NullableOptional}.
     *
     * @param <U> The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return an {@code NullableOptional} describing the result of applying a mapping
     * function to the value of this {@code NullableOptional}, if a value is present,
     * otherwise an empty {@code NullableOptional}
     * @throws NullPointerException if the mapping function is null
     */
    @Nonnull
    public<U> NullableOptional<U> map(@Nonnull Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return NullableOptional.of(mapper.apply(this.value));
        }
    }

    /**
     * If a value is present, apply the provided {@code NullableOptional}-bearing
     * mapping function to it, return that result, otherwise return an empty
     * {@code NullableOptional}.  This method is similar to {@link #map(Function)},
     * but the provided mapper is one whose result is already an {@code NullableOptional},
     * and if invoked, {@code flatMap} does not wrap it with an additional
     * {@code NullableOptional}.
     *
     * @param <U> The type parameter to the {@code NullableOptional} returned by
     * @param mapper a mapping function to apply to the value, if present
     *           the mapping function
     * @return the result of applying an {@code NullableOptional}-bearing mapping
     * function to the value of this {@code NullableOptional}, if a value is present,
     * otherwise an empty {@code NullableOptional}
     * @throws NullPointerException if the mapping function is null or returns
     * a null result
     */
    @Nonnull
    public<U> NullableOptional<U> flatMap(@Nonnull Function<? super T, NullableOptional<U>> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent())
            return empty();
        else {
            return Objects.requireNonNull(mapper.apply(this.value));
        }
    }

    /**
     * Return the value if present, otherwise return {@code other}.
     *
     * @param other the value to be returned if there is no value present, may
     * be null
     * @return the value, if present, otherwise {@code other}
     */
    @Nullable
    public T orElse(@Nullable T other) {
        return isPresent() ? this.value : other;
    }

    /**
     * Return the value if present, otherwise invoke {@code other} and return
     * the result of that invocation.
     *
     * @param other a {@code Supplier} whose result is returned if no value
     * is present
     * @return the value if present otherwise the result of {@code other.get()}
     * @throws NullPointerException if value is not present and {@code other} is
     * null
     */
    @Nullable
    public T orElseGet(@Nonnull Supplier<? extends T> other) {
        return isPresent() ? this.value : other.get();
    }

    /**
     * Return the contained value, if present, otherwise throw an exception
     * to be created by the provided supplier.
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier The supplier which will return the exception to
     * be thrown
     * @return the present value
     * @throws X if there is no value present
     * @throws NullPointerException if no value is present and
     * {@code exceptionSupplier} is null
     */
    @Nullable
    public <X extends Throwable> T orElseThrow(@Nonnull Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return this.value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Indicates whether some other object is "equal to" this NullableOptional. The
     * other object is considered equal if:
     * <ul>
     * <li>it is also an {@code NullableOptional} and;
     * <li>both instances have no value present or;
     * <li>the present values are "equal to" each other via {@code equals()}.
     * </ul>
     *
     * @param obj an object to be tested for equality
     * @return {code true} if the other object is "equal to" this object
     * otherwise {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof NullableOptional)) {
            return false;
        }

        NullableOptional<?> other = (NullableOptional<?>) obj;
        return this.isPresent == other.isPresent && Objects.equals(this.value, other.value);
    }

    /**
     * Returns the hash code value of the present value, if any, or -1 if
     * no value is present.
     *
     * @return hash code value of the present value or 0 if no value is present
     * @see Objects#hashCode(Object)
     */
    @Override
    public int hashCode() {
        if (!this.isPresent) {
            return -1;
        }
        return Objects.hashCode(this.value);
    }

    /**
     * Returns a non-empty string representation of this NullableOptional suitable for
     * debugging.
     *
     * @return the string representation of this instance
     */
    @Override
    public String toString() {
        return isPresent()
                ? String.format("NullableOptional[%s]", this.value)
                : "NullableOptional.empty";
    }
}
