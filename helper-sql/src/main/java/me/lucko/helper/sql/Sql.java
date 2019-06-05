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

package me.lucko.helper.sql;

import com.zaxxer.hikari.HikariDataSource;

import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.sql.batch.BatchBuilder;
import me.lucko.helper.terminable.Terminable;

import org.intellij.lang.annotations.Language;

import be.bendem.sqlstreams.SqlStream;
import be.bendem.sqlstreams.util.SqlConsumer;
import be.bendem.sqlstreams.util.SqlFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * Represents an individual SQL datasource, created by the library.
 */
public interface Sql extends Terminable {

    /**
     * Gets the Hikari instance backing the datasource
     *
     * @return the hikari instance
     */
    @Nonnull
    HikariDataSource getHikari();

    /**
     * Gets a connection from the datasource.
     *
     * <p>The connection should be returned once it has been used.</p>
     *
     * @return a connection
     */
    @Nonnull
    Connection getConnection() throws SQLException;

    /**
     * Gets a {@link SqlStream} instance for this {@link Sql}.
     *
     * @return a instance of the stream library for this connection.
     */
    @Nonnull
    SqlStream stream();

    /**
     * Executes a database statement with no preparation.
     *
     * <p>This will be executed on an asynchronous thread.</p>
     *
     * @param statement the statement to be executed
     * @return a Promise of an asynchronous database execution
     * @see #execute(String) to perform this action synchronously
     */
    @Nonnull
    default Promise<Void> executeAsync(@Language("MySQL") @Nonnull String statement) {
        return Schedulers.async().run(() -> this.execute(statement));
    }

    /**
     * Executes a database statement with no preparation.
     *
     * <p>This will be executed on whichever thread it's called from.</p>
     *
     * @param statement the statement to be executed
     * @see #executeAsync(String) to perform the same action asynchronously
     */
    default void execute(@Language("MySQL") @Nonnull String statement) {
        this.execute(statement, stmt -> {});
    }

    /**
     * Executes a database statement with preparation.
     *
     * <p>This will be executed on an asynchronous thread.</p>
     *
     * @param statement the statement to be executed
     * @param preparer the preparation used for this statement
     * @return a Promise of an asynchronous database execution
     * @see #executeAsync(String, SqlConsumer) to perform this action synchronously
     */
    @Nonnull
    default Promise<Void> executeAsync(@Language("MySQL") @Nonnull String statement, @Nonnull SqlConsumer<PreparedStatement> preparer) {
        return Schedulers.async().run(() -> this.execute(statement, preparer));
    }

    /**
     * Executes a database statement with preparation.
     *
     * <p>This will be executed on whichever thread it's called from.</p>
     *
     * @param statement the statement to be executed
     * @param preparer the preparation used for this statement
     * @see #executeAsync(String, SqlConsumer) to perform this action asynchronously
     */
    void execute(@Language("MySQL") @Nonnull String statement, @Nonnull SqlConsumer<PreparedStatement> preparer);

    /**
     * Executes a database query with no preparation.
     *
     * <p>This will be executed on an asynchronous thread.</p>
     *
     * <p>In the case of a {@link SQLException} or in the case of
     * no data being returned, or the handler evaluating to null,
     * this method will return an {@link Optional#empty()} object.</p>
     *
     * @param query the query to be executed
     * @param handler the handler for the data returned by the query
     * @param <R> the returned type
     * @return a Promise of an asynchronous database query
     * @see #query(String, SqlFunction) to perform this query synchronously
     */
    default <R> Promise<Optional<R>> queryAsync(@Language("MySQL") @Nonnull String query, @Nonnull SqlFunction<ResultSet, R> handler) {
        return Schedulers.async().supply(() -> this.query(query, handler));
    }

    /**
     * Executes a database query with no preparation.
     *
     * <p>This will be executed on whichever thread it's called from.</p>
     *
     * <p>In the case of a {@link SQLException} or in the case of
     * no data being returned, or the handler evaluating to null,
     * this method will return an {@link Optional#empty()} object.</p>
     *
     * @param query the query to be executed
     * @param handler the handler for the data returned by the query
     * @param <R> the returned type
     * @return the results of the database query
     * @see #queryAsync(String, SqlFunction) to perform this query asynchronously
     */
    default <R> Optional<R> query(@Language("MySQL") @Nonnull String query, @Nonnull SqlFunction<ResultSet, R> handler) {
        return this.query(query, stmt -> {}, handler);
    }

    /**
     * Executes a database query with preparation.
     *
     * <p>This will be executed on an asynchronous thread.</p>
     *
     * <p>In the case of a {@link SQLException} or in the case of
     * no data being returned, or the handler evaluating to null,
     * this method will return an {@link Optional#empty()} object.</p>
     *
     * @param query the query to be executed
     * @param preparer the preparation used for this statement
     * @param handler the handler for the data returned by the query
     * @param <R> the returned type
     * @return a Promise of an asynchronous database query
     * @see #query(String, SqlFunction) to perform this query synchronously
     */
    default <R> Promise<Optional<R>> queryAsync(@Language("MySQL") @Nonnull String query, @Nonnull SqlConsumer<PreparedStatement> preparer, @Nonnull SqlFunction<ResultSet, R> handler) {
        return Schedulers.async().supply(() -> this.query(query, preparer, handler));
    }
    /**
     * Executes a database query with preparation.
     *
     * <p>This will be executed on whichever thread it's called from.</p>
     *
     * <p>In the case of a {@link SQLException} or in the case of
     * no data being returned, or the handler evaluating to null,
     * this method will return an {@link Optional#empty()} object.</p>
     *
     * @param query the query to be executed
     * @param preparer the preparation used for this statement
     * @param handler the handler for the data returned by the query
     * @param <R> the returned type
     * @return the results of the database query
     * @see #queryAsync(String, SqlFunction) to perform this query asynchronously
     */
    <R> Optional<R> query(@Language("MySQL") @Nonnull String query, @Nonnull SqlConsumer<PreparedStatement> preparer, @Nonnull SqlFunction<ResultSet, R> handler);

    /**
     * Executes a batched database execution.
     *
     * <p>This will be executed on an asynchronous thread.</p>
     *
     * <p>Note that proper implementations of this method should determine
     * if the provided {@link BatchBuilder} is actually worth of being a
     * batched statement. For instance, a BatchBuilder with only one
     * handler can safely be referred to {@link #executeAsync(String, SqlConsumer)}</p>
     *
     * @param builder the builder to be used.
     * @return a Promise of an asynchronous batched database execution
     * @see #executeBatch(BatchBuilder) to perform this action synchronously
     */
    default Promise<Void> executeBatchAsync(@Nonnull BatchBuilder builder) {
        return Schedulers.async().run(() -> this.executeBatch(builder));
    }

    /**
     * Executes a batched database execution.
     *
     * <p>This will be executed on whichever thread it's called from.</p>
     *
     * <p>Note that proper implementations of this method should determine
     * if the provided {@link BatchBuilder} is actually worth of being a
     * batched statement. For instance, a BatchBuilder with only one
     * handler can safely be referred to {@link #execute(String, SqlConsumer)}</p>
     *
     * @param builder the builder to be used.
     * @see #executeBatchAsync(BatchBuilder) to perform this action asynchronously
     */
    void executeBatch(@Nonnull BatchBuilder builder);

    /**
     * Gets a {@link BatchBuilder} for the provided statement.
     *
     * @param statement the statement to prepare for batching.
     * @return a BatchBuilder
     */
    BatchBuilder batch(@Language("MySQL") @Nonnull String statement);
}
