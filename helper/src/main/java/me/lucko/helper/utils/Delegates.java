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
import me.lucko.helper.terminable.Terminable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A collection of utility methods for delegating Java 8 functions
 */
public final class Delegates {

    public static <T> Consumer<T> runnableToConsumer(Runnable runnable) {
        return new RunnableToConsumer<>(runnable);
    }

    public static <T, U> BiConsumer<T, U> consumerToBiConsumerFirst(Consumer<T> consumer) {
        return new ConsumerToBiConsumerFirst<>(consumer);
    }

    public static <T, U> BiConsumer<T, U> consumerToBiConsumerSecond(Consumer<U> consumer) {
        return new ConsumerToBiConsumerSecond<>(consumer);
    }

    public static <T, U> Function<T, U> consumerToFunction(Consumer<T> consumer) {
        return new ConsumerToFunction<>(consumer);
    }

    public static <T, U> Function<T, U> runnableToFunction(Runnable runnable) {
        return new RunnableToFunction<>(runnable);
    }

    public static Terminable runnableToTerminable(Runnable runnable) {
        return new RunnableToTerminable(runnable);
    }

    private static final class RunnableToConsumer<T> implements Consumer<T>, Delegate<Runnable> {
        private final Runnable delegate;
        private RunnableToConsumer(Runnable delegate) {
            this.delegate = delegate;
        }
        @Override public Runnable getDelegate() { return delegate; }

        @Override
        public void accept(T t) {
            delegate.run();
        }

    }

    private static final class ConsumerToBiConsumerFirst<T, U> implements BiConsumer<T, U>, Delegate<Consumer<T>> {
        private final Consumer<T> delegate;
        private ConsumerToBiConsumerFirst(Consumer<T> delegate) {
            this.delegate = delegate;
        }
        @Override public Consumer<T> getDelegate() { return delegate; }

        @Override
        public void accept(T t, U u) {
            delegate.accept(t);
        }
    }

    private static final class ConsumerToBiConsumerSecond<T, U> implements BiConsumer<T, U>, Delegate<Consumer<U>> {
        private final Consumer<U> delegate;
        private ConsumerToBiConsumerSecond(Consumer<U> delegate) {
            this.delegate = delegate;
        }
        @Override public Consumer<U> getDelegate() { return delegate; }

        @Override
        public void accept(T t, U u) {
            delegate.accept(u);
        }
    }

    private static final class ConsumerToFunction<T, R> implements Function<T, R>, Delegate<Consumer<T>> {
        private final Consumer<T> delegate;
        private ConsumerToFunction(Consumer<T> delegate) {
            this.delegate = delegate;
        }
        @Override public Consumer<T> getDelegate() { return delegate; }

        @Override
        public R apply(T t) {
            delegate.accept(t);
            return null;
        }
    }

    private static final class RunnableToFunction<T, R> implements Function<T, R>, Delegate<Runnable> {
        private final Runnable delegate;
        private RunnableToFunction(Runnable delegate) {
            this.delegate = delegate;
        }
        @Override public Runnable getDelegate() { return delegate; }

        @Override
        public R apply(T t) {
            delegate.run();
            return null;
        }
    }

    private static final class RunnableToTerminable implements Terminable, Delegate<Runnable> {
        private final Runnable delegate;
        private RunnableToTerminable(Runnable delegate) {
            this.delegate = delegate;
        }
        @Override public Runnable getDelegate() { return delegate; }

        @Override
        public boolean terminate() {
            delegate.run();
            return true;
        }

    }

    private Delegates() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
