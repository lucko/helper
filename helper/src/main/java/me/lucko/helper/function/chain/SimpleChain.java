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

import com.google.common.base.Preconditions;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

@NonnullByDefault
class SimpleChain<T> implements Chain<T> {

    @Nullable
    private T object;

    SimpleChain(@Nullable T object) {
        this.object = object;
    }

    @Override
    public Chain<T> apply(Consumer<? super T> action) {
        Preconditions.checkNotNull(action, "action");
        action.accept(object);
        return this;
    }

    @Override
    public Chain<T> applyIf(Predicate<? super T> test, Consumer<? super T> action) {
        Preconditions.checkNotNull(test, "test");
        Preconditions.checkNotNull(action, "action");
        if (test.test(object)) {
            action.accept(object);
        }
        return this;
    }

    @Override
    public Chain<T> applyIfNonNull(Consumer<? super T> action) {
        Preconditions.checkNotNull(action, "action");
        if (object != null) {
            action.accept(object);
        }
        return this;
    }

    @Override
    public Chain<T> orElse(Predicate<? super T> test, T failValue) {
        Preconditions.checkNotNull(test, "test");
        if (!test.test(object)) {
            this.object = failValue;
        }
        return this;
    }

    @Override
    public Chain<T> orElseIfNull(T otherValue) {
        if (object == null) {
            this.object = otherValue;
        }
        return this;
    }

    @Override
    public Chain<T> orElseGet(Predicate<? super T> test, Supplier<? extends T> failSupplier) {
        Preconditions.checkNotNull(test, "test");
        Preconditions.checkNotNull(failSupplier, "failSupplier");
        if (!test.test(object)) {
            this.object = failSupplier.get();
        }
        return this;
    }

    @Override
    public Chain<T> orElseGetIfNull(Supplier<? extends T> supplier) {
        Preconditions.checkNotNull(supplier, "supplier");
        if (object == null) {
            this.object = supplier.get();
        }
        return this;
    }

    @Override
    public <R> Chain<R> ifElse(Predicate<? super T> test, R passValue, R failValue) {
        Preconditions.checkNotNull(test, "test");
        return test.test(object) ? map(s -> passValue) : map(s -> failValue);
    }

    @Override
    public <R> Chain<R> map(Function<? super T, ? extends R> mapper) {
        Preconditions.checkNotNull(mapper, "mapper");

        final R result = mapper.apply(object);

        // try to reduce unnecessary instance creation
        if (result == object) {
            //noinspection unchecked
            return (Chain<R>) this;
        } else {
            return new SimpleChain<>(result);
        }
    }

    @Override
    public <R> Chain<R> mapOrElse(Predicate<? super T> test, Function<? super T, ? extends R> passedMapper, R otherValue) {
        Preconditions.checkNotNull(test, "test");
        Preconditions.checkNotNull(passedMapper, "passedMapper");

        return mapOrElse(test, passedMapper, s -> otherValue);
    }

    @Override
    public <R> Chain<R> mapOrElse(Predicate<? super T> test, Function<? super T, ? extends R> passedMapper, Function<? super T, ? extends R> failedMapper) {
        Preconditions.checkNotNull(test, "test");
        Preconditions.checkNotNull(passedMapper, "passedMapper");
        Preconditions.checkNotNull(failedMapper, "failedMapper");

        return test.test(object) ? map(passedMapper) : map(failedMapper);
    }

    @Override
    public <R> Chain<R> mapNullSafe(Function<? super T, ? extends R> nonNullMapper, R otherValue) {
        Preconditions.checkNotNull(nonNullMapper, "nonNullMapper");
        return mapNullSafeGet(nonNullMapper, () -> otherValue);
    }

    @Override
    public <R> Chain<R> mapNullSafeGet(Function<? super T, ? extends R> nonNullMapper, Supplier<? extends R> nullSupplier) {
        Preconditions.checkNotNull(nonNullMapper, "nonNullMapper");
        Preconditions.checkNotNull(nullSupplier, "nullSupplier");

        final R result = object != null ? nonNullMapper.apply(object) : nullSupplier.get();
        // try to reduce unnecessary instance creation
        if (result == object) {
            //noinspection unchecked
            return (Chain<R>) this;
        } else {
            return new SimpleChain<>(result);
        }
    }

    @Override
    public <R> Chain<R> flatMap(Function<? super T, ? extends Chain<? extends R>> mapper) {
        Preconditions.checkNotNull(mapper, "mapper");
        //noinspection unchecked
        return (Chain<R>) mapper.apply(object);
    }

    @Override
    public Optional<T> end() {
        return Optional.ofNullable(object);
    }

    @Nullable
    @Override
    public T endOrNull() {
        return object;
    }
}
