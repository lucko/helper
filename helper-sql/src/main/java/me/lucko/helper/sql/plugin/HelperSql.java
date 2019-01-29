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

import javax.annotation.Nonnull;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HelperSql implements Sql {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final HikariDataSource hikari;

    public HelperSql(@Nonnull DatabaseCredentials credentials) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("helper-sql-" + COUNTER.getAndIncrement());

        config.setDataSourceClassName("org.mariadb.jdbc.MySQLDataSource");
        config.addDataSourceProperty("serverName", credentials.getAddress());
        config.addDataSourceProperty("port", credentials.getPort());
        config.addDataSourceProperty("databaseName", credentials.getDatabase());
        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());

        // pool settings
        config.setMaximumPoolSize(25);
        config.setMinimumIdle(10);

        // connections should not live for longer than 30 mins
        config.setMaxLifetime(TimeUnit.MINUTES.toMillis(30));
        // We will wait for 10 seconds to get a connection from the pool.
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(10));
        // If a connection is not returned within 10 seconds, it's probably safe to assume it's been leaked.
        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10));
        // ensure we use unicode (this calls #setProperties, a hack for the mariadb driver)
        config.addDataSourceProperty("properties", "useUnicode=true;characterEncoding=utf8");

        this.hikari = new HikariDataSource(config);
    }

    @Nonnull
    @Override
    public HikariDataSource getHikari() {
        return this.hikari;
    }

    @Nonnull
    @Override
    public Connection getConnection() throws SQLException {
        return Objects.requireNonNull(this.hikari.getConnection(), "connection is null");
    }

    @Override
    public void close() {
        this.hikari.close();
    }
}
