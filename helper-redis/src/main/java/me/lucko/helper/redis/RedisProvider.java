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

package me.lucko.helper.redis;

import javax.annotation.Nonnull;

/**
 * Provides {@link Redis} instances.
 */
public interface RedisProvider {

    /**
     * Gets the global redis instance.
     *
     * @return the global redis instance.
     */
    @Nonnull
    Redis getRedis();

    /**
     * Constructs a new redis instance using the given credentials.
     *
     * <p>These instances are not cached, and a new redis instance is created each
     * time this method is called.</p>
     *
     * @param credentials the credentials for the redis instance
     * @return a new redis instance
     */
    @Nonnull
    Redis getRedis(@Nonnull RedisCredentials credentials);

    /**
     * Gets the global redis credentials being used for the global redis instance.
     *
     * @return the global credentials
     */
    @Nonnull
    RedisCredentials getGlobalCredentials();

}
