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

package me.lucko.helper.event.functional;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * A functional builder which accumulates event handlers
 *
 * @param <T> the handled type
 * @param <R> the resultant subscription type
 */
public interface FunctionalHandlerList<T, R> {

    /**
     * Add a {@link Consumer} handler.
     *
     * @param handler the handler
     * @return this handler list
     */
    @Nonnull
    FunctionalHandlerList<T, R> consumer(@Nonnull Consumer<? super T> handler);

    /**
     * Add a {@link BiConsumer} handler.
     *
     * @param handler the handler
     * @return this handler list
     */
    @Nonnull
    FunctionalHandlerList<T, R> biConsumer(@Nonnull BiConsumer<R, ? super T> handler);

    /**
     * Builds and registers the Handler.
     *
     * @return a registered {@link R} instance.
     * @throws IllegalStateException if no handlers have been registered
     */
    @Nonnull
    R register();

}
