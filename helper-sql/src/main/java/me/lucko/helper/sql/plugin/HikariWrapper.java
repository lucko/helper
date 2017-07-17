package me.lucko.helper.sql.plugin;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import me.lucko.helper.sql.DatabaseCredentials;
import me.lucko.helper.sql.HelperDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

class HikariWrapper implements HelperDataSource {
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    private final HikariDataSource hikari;

    HikariWrapper(DatabaseCredentials credentials) {
        HikariConfig config = new HikariConfig();

        config.setMaximumPoolSize(25);

        config.setPoolName("helper-sql-" + COUNTER.getAndIncrement());
        config.setDataSourceClassName("org.mariadb.jdbc.MySQLDataSource");
        config.addDataSourceProperty("serverName", credentials.getAddress());
        config.addDataSourceProperty("port", credentials.getPort());
        config.addDataSourceProperty("databaseName", credentials.getDatabase());
        config.setUsername(credentials.getUsername());
        config.setPassword(credentials.getPassword());

        // hack for the mariadb driver
        config.addDataSourceProperty("properties", "useUnicode=true;characterEncoding=utf8");

        // We will wait for 15 seconds to get a connection from the pool.
        // Default is 30, but it shouldn't be taking that long.
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(15)); // 15000

        // If a connection is not returned within 10 seconds, it's probably safe to assume it's been leaked.
        config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10)); // 10000

        hikari = new HikariDataSource(config);
    }

    @Override
    public HikariDataSource getHikari() {
        return Preconditions.checkNotNull(hikari, "hikari");
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getHikari().getConnection();
    }

    @Override
    public boolean terminate() {
        if (hikari != null) {
            hikari.close();
            return true;
        }
        return false;
    }
}
