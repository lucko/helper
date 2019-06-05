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

package me.lucko.helper.sql.plugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.lucko.helper.sql.DatabaseCredentials;
import me.lucko.helper.sql.Sql;
import me.lucko.helper.sql.batch.BatchBuilder;

import org.intellij.lang.annotations.Language;

import be.bendem.sqlstreams.SqlStream;
import be.bendem.sqlstreams.util.SqlConsumer;
import be.bendem.sqlstreams.util.SqlFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

public class HelperSql implements Sql {

    private static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);

    private static final String DATA_SOURCE_CLASS = "org.mariadb.jdbc.MySQLDataSource";

    // https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
    private static final int MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1;
    private static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);

    private static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30); // 30 Minutes
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10); // 10 seconds
    private static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(10); // 10 seconds

    private final HikariDataSource source;
    private final SqlStream stream;

    public HelperSql(@Nonnull DatabaseCredentials credentials) {
        final HikariConfig hikari = new HikariConfig();

        hikari.setPoolName("helper-sql-" + POOL_COUNTER.getAndIncrement());

        hikari.setDataSourceClassName(DATA_SOURCE_CLASS);
        hikari.addDataSourceProperty("serverName", credentials.getAddress());
        hikari.addDataSourceProperty("port", credentials.getPort());
        hikari.addDataSourceProperty("databaseName", credentials.getDatabase());

        hikari.setUsername(credentials.getUsername());
        hikari.setPassword(credentials.getPassword());

        hikari.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
        hikari.setMinimumIdle(MINIMUM_IDLE);

        hikari.setMaxLifetime(MAX_LIFETIME);
        hikari.setConnectionTimeout(CONNECTION_TIMEOUT);
        hikari.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);

        // ensure we use unicode (this calls #setProperties, a hack for the mariadb driver)
        hikari.addDataSourceProperty("properties", "useUnicode=true;characterEncoding=utf8");

        this.source = new HikariDataSource(hikari);
        this.stream = SqlStream.connect(this.source);
    }

    @Nonnull
    @Override
    public HikariDataSource getHikari() {
        return this.source;
    }

    @Nonnull
    @Override
    public Connection getConnection() throws SQLException {
        return Objects.requireNonNull(this.source.getConnection(), "connection is null");
    }

    @Nonnull
    @Override
    public SqlStream stream() {
        return this.stream;
    }

    @Override
    public void execute(@Language("MySQL") @Nonnull String statement, @Nonnull SqlConsumer<PreparedStatement> preparer) {
        try (Connection c = this.getConnection(); PreparedStatement s = c.prepareStatement(statement)) {
            preparer.accept(s);
            s.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public <R> Optional<R> query(@Language("MySQL") @Nonnull String query, @Nonnull SqlConsumer<PreparedStatement> preparer, @Nonnull SqlFunction<ResultSet, R> handler) {
        try (Connection c = this.getConnection(); PreparedStatement s = c.prepareStatement(query)) {
            preparer.accept(s);
            try (ResultSet r = s.executeQuery()) {
                return Optional.ofNullable(handler.apply(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void executeBatch(@Nonnull BatchBuilder builder) {
        if (builder.getHandlers().isEmpty()) {
            return;
        }

        if (builder.getHandlers().size() == 1) {
            this.execute(builder.getStatement(), builder.getHandlers().iterator().next());
            return;
        }

        try (Connection c = this.getConnection(); PreparedStatement s = c.prepareStatement(builder.getStatement())) {
            for (SqlConsumer<PreparedStatement> handlers : builder.getHandlers()) {
                handlers.accept(s);
                s.addBatch();
            }
            s.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BatchBuilder batch(@Language("MySQL") @Nonnull String statement) {
        return new HelperSqlBatchBuilder(this, statement);
    }

    @Override
    public void close() {
        this.source.close();
    }
}
