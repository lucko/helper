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

package me.lucko.helper.terminable;

import java.util.function.Consumer;

/**
 * Represents an object that can be unregistered, stopped, or gracefully halted.
 */
public interface Terminable {
    Terminable EMPTY = () -> true;

    static Terminable of(Runnable r) {
        return () -> {
            r.run();
            return true;
        };
    }

    /**
     * Terminate this instance
     * @return true if the object wasn't already terminated
     */
    boolean terminate();

    /**
     * Registers this terminable with a terminable consumer (usually the plugin instance)
     * @param consumer the terminable consumer
     */
    default void register(Consumer<Terminable> consumer) {
        consumer.accept(this);
    }

    /**
     * Used to help cleanup held terminable instances in registries
     * @return true if this terminable has been terminated already
     */
    default boolean hasTerminated() {
        return false;
    }

}
