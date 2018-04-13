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

package me.lucko.helper.messaging.bungee;

import me.lucko.helper.cache.Lazy;
import me.lucko.helper.internal.LoaderUtils;

/**
 * Contains a "global" {@link BungeeCord}, lazily loaded on first request.
 */
public final class BungeeCordProvider {
    private static final Lazy<BungeeCord> INSTANCE = Lazy.suppliedBy(() -> new BungeeCordImpl(LoaderUtils.getPlugin()));

    /**
     * Gets a {@link BungeeCord} instance.
     *
     * @return a BungeeCord instance
     */
    public static BungeeCord get() {
        return INSTANCE.get();
    }

    private BungeeCordProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
