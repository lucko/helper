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

package me.lucko.helper.scheduler.builder;

import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.promise.ThreadContext;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

class ContextualPromiseBuilderImpl implements ContextualPromiseBuilder {
    private final ThreadContext context;

    ContextualPromiseBuilderImpl(ThreadContext context) {
        this.context = context;
    }

    @Nonnull
    @Override
    public <T> Promise<T> supply(@Nonnull Supplier<T> supplier) {
        return Schedulers.get(this.context).supply(supplier);
    }

    @Nonnull
    @Override
    public <T> Promise<T> call(@Nonnull Callable<T> callable) {
        return Schedulers.get(this.context).call(callable);
    }

    @Nonnull
    @Override
    public Promise<Void> run(@Nonnull Runnable runnable) {
        return Schedulers.get(this.context).run(runnable);
    }
}
