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

import me.lucko.helper.interfaces.Delegate;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A collection of utility methods for delegating Java 8 functions
 */
public final class Delegates {

    public static <T> Consumer<T> runnableToConsumer(Runnable runnable) {
        return new RunnableToConsumer<>(runnable);
    }

    public static Supplier<Void> runnableToSupplier(Runnable runnable) {
        return new RunnableToSupplier<>(runnable);
    }

    public static <T> Supplier<T> callableToSupplier(Callable<T> callable) {
        return new CallableToSupplier<>(callable);
    }

    public static <T, U> BiConsumer<T, U> consumerToBiConsumerFirst(Consumer<T> consumer) {
        return new ConsumerToBiConsumerFirst<>(consumer);
    }

    public static <T, U> BiConsumer<T, U> consumerToBiConsumerSecond(Consumer<U> consumer) {
        return new ConsumerToBiConsumerSecond<>(consumer);
    }

    public static <T, U> BiPredicate<T, U> predicateToBiPredicateFirst(Predicate<T> predicate) {
        return new PredicateToBiPredicateFirst<>(predicate);
    }

    public static <T, U> BiPredicate<T, U> predicateToBiPredicateSecond(Predicate<U> predicate) {
        return new PredicateToBiPredicateSecond<>(predicate);
    }

    public static <T, U> Function<T, U> consumerToFunction(Consumer<T> consumer) {
        return new ConsumerToFunction<>(consumer);
    }

    public static <T, U> Function<T, U> runnableToFunction(Runnable runnable) {
        return new RunnableToFunction<>(runnable);
    }

    private static abstract class AbstractDelegate<T> implements Delegate<T> {
        final T delegate;

        AbstractDelegate(T delegate) {
            this.delegate = delegate;
        }

        @Override
        public T getDelegate() {
            return delegate;
        }
    }

    private static final class RunnableToConsumer<T> extends AbstractDelegate<Runnable> implements Consumer<T> {
        RunnableToConsumer(Runnable delegate) {
            super(delegate);
        }

        @Override
        public void accept(T t) {
            this.delegate.run();
        }
    }

    private static final class CallableToSupplier<T> extends AbstractDelegate<Callable<T>> implements Supplier<T> {
        CallableToSupplier(Callable<T> delegate) {
            super(delegate);
        }

        @Override
        public T get() {
            try {
                return this.delegate.call();
            } catch (RuntimeException | Error e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class RunnableToSupplier<T> extends AbstractDelegate<Runnable> implements Supplier<T> {
        RunnableToSupplier(Runnable delegate) {
            super(delegate);
        }

        @Override
        public T get() {
            this.delegate.run();
            return null;
        }
    }

    private static final class ConsumerToBiConsumerFirst<T, U> extends AbstractDelegate<Consumer<T>> implements BiConsumer<T, U> {
        ConsumerToBiConsumerFirst(Consumer<T> delegate) {
            super(delegate);
        }

        @Override
        public void accept(T t, U u) {
            this.delegate.accept(t);
        }
    }

    private static final class ConsumerToBiConsumerSecond<T, U>extends AbstractDelegate<Consumer<U>> implements BiConsumer<T, U> {
        ConsumerToBiConsumerSecond(Consumer<U> delegate) {
            super(delegate);
        }

        @Override
        public void accept(T t, U u) {
            this.delegate.accept(u);
        }
    }

    private static final class PredicateToBiPredicateFirst<T, U> extends AbstractDelegate<Predicate<T>> implements BiPredicate<T, U> {
        PredicateToBiPredicateFirst(Predicate<T> delegate) {
            super(delegate);
        }

        @Override
        public boolean test(T t, U u) {
            return this.delegate.test(t);
        }
    }

    private static final class PredicateToBiPredicateSecond<T, U> extends AbstractDelegate<Predicate<U>> implements BiPredicate<T, U> {
        PredicateToBiPredicateSecond(Predicate<U> delegate) {
            super(delegate);
        }

        @Override
        public boolean test(T t, U u) {
            return this.delegate.test(u);
        }
    }

    private static final class ConsumerToFunction<T, R> extends AbstractDelegate<Consumer<T>> implements Function<T, R> {
        ConsumerToFunction(Consumer<T> delegate) {
            super(delegate);
        }

        @Override
        public R apply(T t) {
            this.delegate.accept(t);
            return null;
        }
    }

    private static final class RunnableToFunction<T, R> extends AbstractDelegate<Runnable> implements Function<T, R> {
        RunnableToFunction(Runnable delegate) {
            super(delegate);
        }

        @Override
        public R apply(T t) {
            this.delegate.run();
            return null;
        }
    }

    private Delegates() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
