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

package me.lucko.helper.sql.batch;

import me.lucko.helper.promise.Promise;

import be.bendem.sqlstreams.util.SqlConsumer;

import java.sql.PreparedStatement;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Represents a statement meant to be executed more than a single time.
 *
 * <p>It will be executed all at once, using a single database connection.</p>
 */
public interface BatchBuilder {

    /**
     * Gets the statement to be executed when this batch is finished.
     *
     * @return the statement to be executed
     */
    @Nonnull
    String getStatement();

    /**
     * Gets a {@link Collection} of handlers for this statement.
     *
     * @return the handlers for this statement
     */
    @Nonnull
    Collection<SqlConsumer<PreparedStatement>> getHandlers();

    /**
     * Resets this BatchBuilder, making it possible to re-use
     * for multiple situations.
     *
     * @return this builder
     */
    BatchBuilder reset();

    /**
     * Adds an additional handler to be executed when this batch is finished.
     *
     * @param handler the statement handler
     * @return this builder
     */
    BatchBuilder batch(@Nonnull SqlConsumer<PreparedStatement> handler);

    /**
     * Executes the statement for this batch, with the handlers used to prepare it.
     */
    void execute();

    /**
     * Executes the statement for this batch, with the handlers used to prepare it.
     *
     * <p>Will return a {@link Promise} to do this.</p>
     *
     * @return a promise to execute this batch asynchronously
     */
    @Nonnull
    Promise<Void> executeAsync();
}
