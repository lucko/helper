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

package me.lucko.helper.function;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * A collection of utilities for working with {@link Predicate}s.
 */
@NonnullByDefault
public final class Predicates {

    /**
     * Returns a predicate which takes the input, passes it through the given
     * function, and then obtains a result from the delegate predicate.
     *
     * @param function the function to map the input
     * @param predicate the predicate to test the result of the mapping
     * @param <A> the input type
     * @param <B> the tested type
     * @return a composed predicate
     */
    public static <A, B> Predicate<A> compose(Function<A, ? extends B> function, Predicate<B> predicate) {
        Objects.requireNonNull(function, "function");
        Objects.requireNonNull(predicate, "predicate");
        return input -> predicate.test(function.apply(input));
    }

    /**
     * Returns a predicate which always returns true
     *
     * @param <T> the type
     * @return a predicate
     */
    public static <T> Predicate<T> alwaysTrue() {
        return ObjectPredicate.ALWAYS_TRUE.cast();
    }

    /**
     * Returns a predicate which always returns false
     *
     * @param <T> the type
     * @return a predicate
     */
    public static <T> Predicate<T> alwaysFalse() {
        return ObjectPredicate.ALWAYS_FALSE.cast();
    }

    /**
     * Returns a predicate which returns true if the input is null,
     * false otherwise.
     *
     * @param <T> the type
     * @return a predicate
     */
    public static <T> Predicate<T> isNull() {
        return ObjectPredicate.IS_NULL.cast();
    }

    /**
     * Returns a predicate which takes the input, passes it through the given
     * function, and then evaluates the result against {@link #isNull()}.
     *
     * @param composeFunction the function to map the input
     * @param <A> the input type
     * @param <B> the tested type
     * @return a predicate
     */
    public static <A, B> Predicate<A> isNull(Function<A, ? extends B> composeFunction) {
        return compose(composeFunction, isNull());
    }

    /**
     * Returns a predicate which returns false if the input is null,
     * true otherwise.
     *
     * @param <T> the type
     * @return a predicate
     */
    public static <T> Predicate<T> notNull() {
        return ObjectPredicate.NOT_NULL.cast();
    }

    /**
     * Returns a predicate which takes the input, passes it through the given
     * function, and then evaluates the result against {@link #notNull()}.
     *
     * @param composeFunction the function to map the input
     * @param <A> the input type
     * @param <B> the tested type
     * @return a predicate
     */
    public static <A, B> Predicate<A> notNull(Function<A, ? extends B> composeFunction) {
        return compose(composeFunction, notNull());
    }

    /**
     * Returns a predicate which returns true if the target is
     * {@link Object#equals(Object)} to the input. If the target is null,
     * the predicate returns true if the input is also null, false otherwise.
     *
     * @param target the target object to compare against
     * @param <T> the type
     * @return a predicate
     */
    public static <T> Predicate<T> equalTo(@Nullable Object target) {
        return target == null ? Objects::isNull : target::equals;
    }

    /**
     * Returns a predicate which takes the input, passes it through the given
     * function, and then evaluates the result against {@link #equalTo(Object)}.
     *
     * @param composeFunction the function to map the input
     * @param target the target object to compare against
     * @param <A> the input type
     * @param <B> the tested type
     * @return a predicate
     */
    public static <A, B> Predicate<A> equalTo(Function<A, ? extends B> composeFunction, @Nullable Object target) {
        return compose(composeFunction, equalTo(target));
    }

    /**
     * Returns a predicate which returns true if the input is contained within
     * the given collection.
     *
     * @param collection the collection to query
     * @param <T> the type
     * @return a predicate
     */
    public static <T> Predicate<T> in(Collection<? extends T> collection) {
        return collection::contains;
    }

    /**
     * Returns a predicate which takes the input, passes it through the given
     * function, and then evaluates the result against {@link #in(Collection)}.
     *
     * @param composeFunction the function to map the input
     * @param collection the collection to query
     * @param <A> the input type
     * @param <B> the tested type
     * @return a predicate
     */
    public static <A, B> Predicate<A> in(Function<A, ? extends B> composeFunction, Collection<? extends B> collection) {
        return compose(composeFunction, in(collection));
    }

    /**
     * Returns a predicate which returns true if the input is contained within
     * the given var args.
     *
     * @param args the array to query
     * @param <T> the type
     * @return a predicate
     */
    @SafeVarargs
    public static <T> Predicate<T> in(T... args) {
        return input -> {
            if (input == null) {
                for (T t : args) {
                    if (t == null) {
                        return true;
                    }
                }
            } else {
                for (T t : args) {
                    if (input.equals(t)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    /**
     * Returns a predicate which takes the input, passes it through the given
     * function, and then evaluates the result against {@link #in(Object[])}.
     *
     * @param composeFunction the function to map the input
     * @param args the array to query
     * @param <A> the input type
     * @param <B> the tested type
     * @return a predicate
     */
    @SafeVarargs
    public static <A, B> Predicate<A> in(Function<A, ? extends B> composeFunction, B... args) {
        return compose(composeFunction, in(args));
    }

    /**
     * Returns a predicate which returns true if the input is an instance of the
     * given class.
     *
     * @param clazz the class
     * @param <T> the type
     * @return a predicate
     */
    public static <T> Predicate<T> instanceOf(Class<?> clazz) {
        return clazz::isInstance;
    }

    /**
     * Returns a predicate which takes the input, passes it through the given
     * function, and then evaluates the result against {@link #instanceOf(Class)}.
     *
     * @param composeFunction the function to map the input
     * @param clazz the class
     * @param <A> the input type
     * @param <B> the tested type
     * @return a predicate
     */
    public static <A, B> Predicate<A> instanceOf(Function<A, ? extends B> composeFunction, Class<?> clazz) {
        return compose(composeFunction, instanceOf(clazz));
    }

    private enum ObjectPredicate implements Predicate<Object> {
        ALWAYS_TRUE {
            @Override public boolean test(Object o) { return true; }
            @Override public Predicate<? super Object> and(Predicate<? super Object> other) { return other; }
            @Override public Predicate<? super Object> or(Predicate<? super Object> other) { return this; }
            @Override public Predicate<? super Object> negate() { return ALWAYS_FALSE; }
        },
        ALWAYS_FALSE {
            @Override public boolean test(Object o) { return false; }
            @Override public Predicate<? super Object> and(Predicate<? super Object> other) { return this; }
            @Override public Predicate<? super Object> or(Predicate<? super Object> other) { return other; }
            @Override public Predicate<? super Object> negate() { return ALWAYS_TRUE; }
        },

        IS_NULL {
            @Override public boolean test(Object o) { return o == null; }
            @Override public Predicate<? super Object> negate() { return NOT_NULL; }
        },
        NOT_NULL {
            @Override public boolean test(Object o) { return o != null; }
            @Override public Predicate<? super Object> negate() { return IS_NULL; }
        };

        <T> Predicate<T> cast() {
            //noinspection unchecked
            return (Predicate<T>) this;
        }
    }

    private Predicates() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
