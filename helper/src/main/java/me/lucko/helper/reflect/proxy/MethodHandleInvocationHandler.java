/*
 * This file is part of lunar, licensed under the MIT License.
 *
 * Copyright (c) 2017-2018 KyoriPowered
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.lucko.helper.reflect.proxy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;

/**
 * An abstract implementation of an invocation handler that uses {@link MethodHandle method handles}.
 */
public abstract class MethodHandleInvocationHandler implements InvocationHandler {
    // A shared cache of unbound method handles.
    private static final LoadingCache<Method, MethodHandle> SHARED_CACHE = CacheBuilder.newBuilder()
            .build(CacheLoader.from(MoreMethodHandles::unreflect));

    // A local cache of bound method handles.
    private final LoadingCache<Method, MethodHandle> cache = CacheBuilder.newBuilder()
            .build(CacheLoader.from(method -> {
                final Object object = this.object(method);
                return SHARED_CACHE.getUnchecked(method).bindTo(object);
            }));

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return this.cache.getUnchecked(method).invokeWithArguments(args);
    }

    @Nonnull
    protected abstract Object object(@Nonnull Method method);
}
