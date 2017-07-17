package me.lucko.helper.sql;

import com.zaxxer.hikari.HikariDataSource;

import me.lucko.helper.terminable.Terminable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents an individual datasource, created by the library.
 */
public interface HelperDataSource extends Terminable {

    /**
     * Gets the Hikari instance backing the datasource
     *
     * @return the hikari instance
     */
    HikariDataSource getHikari();

    /**
     * Gets a connection from the datasource.
     *
     * <p>The connection should be returned once it has been used.</p>
     *
     * @return a connection
     */
    Connection getConnection() throws SQLException;

}
