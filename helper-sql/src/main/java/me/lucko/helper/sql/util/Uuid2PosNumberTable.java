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

package me.lucko.helper.sql.util;

import com.google.common.collect.Maps;

import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.sql.Sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class Uuid2PosNumberTable<T, O> {
    private static final String INSERT_ADD = "INSERT INTO `{table}` (uuid, value) VALUES(?, ?) ON DUPLICATE KEY UPDATE value = value + ?";
    private static final String INSERT_SET = "INSERT INTO `{table}` (uuid, value) VALUES(?, ?) ON DUPLICATE KEY UPDATE value = ?";
    private static final String UPDATE_TAKE = "UPDATE `{table}` SET value = value - ? WHERE uuid=? AND value >= ?";
    private static final String SELECT = "SELECT value FROM `{table}` WHERE uuid=?";
    private static final String SELECT_TOP = "SELECT uuid, value FROM `{table}` ORDER BY value DESC LIMIT ?,?";
    private static final String SELECT_MAX_PAGES = "SELECT COUNT(*) / ? max_pages FROM `{table}`";
    protected static final String SELECT_TOTAL = "SELECT SUM(value) as total FROM `{table}`";

    protected final Sql sql;
    protected final String table;

    protected Uuid2PosNumberTable(Sql sql, String table) {
        this.sql = sql;
        this.table = table;
    }

    protected abstract String getCreateStmt();

    protected abstract void set(PreparedStatement ps, int paramIndex, T value) throws SQLException;

    protected abstract T get(ResultSet rs, String columnLabel) throws SQLException;

    protected abstract O getOptional(ResultSet rs, String columnLabel) throws SQLException;

    protected abstract O emptyOptional();

    /**
     * Initialises the table.
     */
    public void init() {
        try (Connection c = sql.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.execute(getCreateStmt().replace("{table}", table));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected Promise<Void> doAdd(UUID uuid, T amount) {
        return Schedulers.async().call(() -> {
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(INSERT_ADD.replace("{table}", table))) {
                    ps.setString(1, uuid.toString());
                    set(ps, 2, amount);
                    set(ps, 3, amount);
                    ps.execute();
                }
            }
            return null;
        });
    }

    protected Promise<Void> doSet(UUID uuid, T amount) {
        return Schedulers.async().call(() -> {
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(INSERT_SET.replace("{table}", table))) {
                    ps.setString(1, uuid.toString());
                    set(ps, 2, amount);
                    set(ps, 3, amount);
                    ps.execute();
                }
            }
            return null;
        });
    }

    protected Promise<Boolean> doTake(UUID uuid, T amount) {
        return Schedulers.async().call(() -> {
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(UPDATE_TAKE.replace("{table}", table))) {
                    set(ps, 1, amount);
                    ps.setString(2, uuid.toString());
                    set(ps, 3, amount);
                    return ps.executeUpdate() != 0;
                }
            }
        });
    }

    /**
     * Gets the {@code amount}.
     *
     * @param uuid the uuid
     * @return the amount
     */
    public Promise<O> get(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");

        return Schedulers.async().call(() -> {
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(SELECT.replace("{table}", table))) {
                    ps.setString(1, uuid.toString());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return getOptional(rs, "value");
                        } else {
                            return emptyOptional();
                        }
                    }
                }
            }
        });
    }

    /**
     * Gets the max number of pages, assuming the given entries per page.
     *
     * @param entriesPerPage the entries per page
     * @return the max pages
     */
    public Promise<Integer> getOrderedMaxPages(int entriesPerPage) {
        return Schedulers.async().call(() -> {
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(SELECT_MAX_PAGES.replace("{table}", table))) {
                    ps.setInt(1, entriesPerPage);
                    try (ResultSet rs = ps.executeQuery()) {
                        return rs.next() ? (int) Math.ceil(rs.getDouble("max_pages")) : 0;
                    }
                }
            }
        });
    }

    /**
     * Gets a page.
     *
     * @param page the page
     * @param entriesPerPage the number of entries per page
     * @return the page
     */
    public Promise<List<Map.Entry<UUID, T>>> getOrderedPage(int page, int entriesPerPage) {
        int min = page * entriesPerPage;
        int max = page + entriesPerPage;
        return Schedulers.async().call(() -> {
            List<Map.Entry<UUID, T>> entries = new ArrayList<>();
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(SELECT_TOP.replace("{table}", table))) {
                    ps.setInt(1, min);
                    ps.setInt(2, max);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            entries.add(Maps.immutableEntry(
                                    UUID.fromString(rs.getString("uuid")),
                                    get(rs, "value")
                            ));
                        }
                    }
                }
            }
            return entries;
        });
    }

}