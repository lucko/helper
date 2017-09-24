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

package me.lucko.helper.mongo;

import javax.annotation.Nonnull;

/**
 * Provides {@link HelperMongo} instances.
 */
public interface MongoProvider {

    /**
     * Gets the global datasource.
     *
     * @return the global datasource.
     */
    @Nonnull
    HelperMongo getDataSource();

    /**
     * Constructs a new datasource using the given credentials.
     *
     * <p>These instances are not cached, and a new datasource is created each
     * time this method is called.</p>
     *
     * @param credentials the credentials for the database
     * @return a new datasource
     */
    @Nonnull
    HelperMongo getDataSource(@Nonnull MongoDatabaseCredentials credentials);

    /**
     * Gets the global database credentials being used for the global datasource.
     *
     * @return the global credentials
     */
    @Nonnull
    MongoDatabaseCredentials getGlobalCredentials();

}
