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

package me.lucko.helper.profiles.plugin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import me.lucko.helper.Events;
import me.lucko.helper.Scheduler;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.profiles.Profile;
import me.lucko.helper.profiles.ProfileRepository;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.sql.DatabaseCredentials;
import me.lucko.helper.sql.HelperDataSource;
import me.lucko.helper.sql.SqlProvider;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public class ProfilesPlugin extends ExtendedJavaPlugin implements ProfileRepository {

    private static final String CREATE =
            "CREATE TABLE IF NOT EXISTS {table} (" +
            "`uniqueid` BINARY(16) NOT NULL PRIMARY KEY, " +
            "`name` VARCHAR(16) NOT NULL, " +
            "`lastupdate` TIMESTAMP NOT NULL)";

    private static final String INSERT = "INSERT INTO {table} VALUES(UNHEX(?), ?, ?) ON DUPLICATE KEY UPDATE `name` = ?, `lastupdate` = ?";
    private static final String SELECT_UID = "SELECT `name`, `lastupdate` FROM {table} WHERE `uniqueid` = UNHEX(?)";
    private static final String SELECT_NAME = "SELECT HEX(`uniqueid`) AS `canonicalid`, `name`, `lastupdate` FROM {table} WHERE `name` = ? ORDER BY `lastupdate` DESC LIMIT 1";
    private static final String SELECT_ALL = "SELECT HEX(`uniqueid`) AS `canonicalid`, `name`, `lastupdate` FROM {table}";
    private static final String SELECT_ALL_RECENT = "SELECT HEX(`uniqueid`) AS `canonicalid`, `name`, `lastupdate` FROM {table} ORDER BY `lastupdate` DESC LIMIT ?";
    private static final String SELECT_ALL_UIDS = "SELECT HEX(`uniqueid`) AS `canonicalid`, `name`, `lastupdate` FROM {table} WHERE `uniqueid` IN %s";
    private static final String SELECT_ALL_NAMES = "SELECT HEX(`uniqueid`) AS `canonicalid`, `name`, `lastupdate` FROM {table} WHERE `name` IN %s GROUP BY `name` ORDER BY `lastupdate` DESC";

    private final Cache<UUID, ImmutableProfile> profileMap = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(6, TimeUnit.HOURS)
            .build();

    private HelperDataSource sql;
    private String tableName;

    @Override
    protected void enable() {
        SqlProvider sqlProvider = getService(SqlProvider.class);
        if (sqlProvider == null) {
            throw new RuntimeException("Unable to obtain SqlProvider!");
        }

        // load sql instance
        YamlConfiguration config = loadConfig("config.yml");
        if (config.getBoolean("use-global-credentials", true)) {
            sql = sqlProvider.getDataSource();
        } else {
            sql = sqlProvider.getDataSource(DatabaseCredentials.fromConfig(config));
        }

        // init the table
        tableName = config.getString("table-name", "helper_profiles");
        try (Connection c = sql.getConnection()) {
            try (Statement s = c.createStatement()) {
                s.execute(replaceTableName(CREATE));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // preload data
        int preloadAmount = config.getInt("preload-amount", 2000);
        if (preloadAmount > 0) {
            getLogger().info("Preloading the most recent " + preloadAmount + " entries...");
            long start = System.currentTimeMillis();
            int found = preload(preloadAmount);
            long time = System.currentTimeMillis() - start;
            getLogger().info("Preloaded " + found + " profiles into the cache! - took " + time + "ms");
        }

        // observe logins
        Events.subscribe(PlayerLoginEvent.class, EventPriority.MONITOR)
                .filter(e -> e.getResult() == PlayerLoginEvent.Result.ALLOWED)
                .handler(e -> {
                    ImmutableProfile profile = new ImmutableProfile(e.getPlayer().getUniqueId(), e.getPlayer().getName());
                    updateCache(profile);
                    Scheduler.runAsync(() -> saveProfile(profile));
                })
                .bindWith(this);

        // provide the ProfileRepository service
        provideService(ProfileRepository.class, this);
    }

    private String replaceTableName(String s) {
        return s.replace("{table}", tableName);
    }

    private void updateCache(ImmutableProfile profile) {
        ImmutableProfile existing = profileMap.getIfPresent(profile.getUniqueId());
        if (existing == null || existing.getTimestamp() < profile.getTimestamp()) {
            profileMap.put(profile.getUniqueId(), profile);
        }
    }

    private void saveProfile(ImmutableProfile profile) {
        try (Connection c = sql.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(replaceTableName(INSERT))) {
                ps.setString(1, UuidUtils.toString(profile.getUniqueId()));
                ps.setString(2, profile.getName().get());
                ps.setTimestamp(3, new Timestamp(profile.getTimestamp()));
                ps.setString(4, profile.getName().get());
                ps.setTimestamp(5, new Timestamp(profile.getTimestamp()));
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int preload(int numEntries) {
        int i = 0;
        try (Connection c = sql.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(replaceTableName(SELECT_ALL_RECENT))) {
                ps.setInt(1, numEntries);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String name = rs.getString("name");
                        Timestamp lastUpdate = rs.getTimestamp("lastupdate");
                        String uuidString = rs.getString("canonicalid");
                        UUID uuid = UuidUtils.fromString(uuidString);

                        ImmutableProfile p = new ImmutableProfile(uuid, name, lastUpdate.getTime());
                        updateCache(p);
                        i++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    @Nonnull
    @Override
    public Profile getProfile(@Nonnull UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId, "uniqueId");
        Profile profile = profileMap.getIfPresent(uniqueId);
        if (profile == null) {
            profile = new ImmutableProfile(uniqueId, null, 0);
        }
        return profile;
    }

    @Nonnull
    @Override
    public Optional<Profile> getProfile(@Nonnull String name) {
        Preconditions.checkNotNull(name, "name");
        for (Profile profile : profileMap.asMap().values()) {
            if (profile.getName().isPresent() && profile.getName().get().equalsIgnoreCase(name)) {
                return Optional.of(profile);
            }
        }
        return Optional.empty();
    }

    @Nonnull
    @Override
    public Collection<Profile> getKnownProfiles() {
        return Collections.unmodifiableCollection(profileMap.asMap().values());
    }

    @Nonnull
    @Override
    public Promise<Profile> lookupProfile(@Nonnull UUID uniqueId) {
        Preconditions.checkNotNull(uniqueId, "uniqueId");
        Profile profile = getProfile(uniqueId);
        if (profile.getName().isPresent()) {
            return Promise.completed(profile);
        }

        return Scheduler.supplyAsync(() -> {
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(replaceTableName(SELECT_UID))) {
                    ps.setString(1, UuidUtils.toString(uniqueId));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String name = rs.getString("name");
                            Timestamp lastUpdate = rs.getTimestamp("lastupdate");
                            ImmutableProfile p = new ImmutableProfile(uniqueId, name, lastUpdate.getTime());
                            updateCache(p);
                            return p;
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return new ImmutableProfile(uniqueId, null, 0);
        });
    }

    @Nonnull
    @Override
    public Promise<Optional<Profile>> lookupProfile(@Nonnull String name) {
        Preconditions.checkNotNull(name, "name");

        Optional<Profile> profile = getProfile(name);
        if (profile.isPresent()) {
            return Promise.completed(profile);
        }

        return Scheduler.supplyAsync(() -> {
            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(replaceTableName(SELECT_NAME))) {
                    ps.setString(1, name);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String remoteName = rs.getString("name"); // provide a case corrected name
                            Timestamp lastUpdate = rs.getTimestamp("lastupdate");
                            String uuidString = rs.getString("canonicalid");
                            UUID uuid = UuidUtils.fromString(uuidString);

                            ImmutableProfile p = new ImmutableProfile(uuid, remoteName, lastUpdate.getTime());
                            updateCache(p);
                            return Optional.of(p);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return Optional.empty();
        });
    }

    @Nonnull
    @Override
    public Promise<Collection<Profile>> lookupKnownProfiles() {
        return Scheduler.supplyAsync(() -> {
            Set<Profile> ret = new HashSet<>();

            try (Connection c = sql.getConnection()) {
                try (PreparedStatement ps = c.prepareStatement(replaceTableName(SELECT_ALL))) {
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String name = rs.getString("name");
                            Timestamp lastUpdate = rs.getTimestamp("lastupdate");
                            String uuidString = rs.getString("canonicalid");
                            UUID uuid = UuidUtils.fromString(uuidString);

                            ImmutableProfile p = new ImmutableProfile(uuid, name, lastUpdate.getTime());
                            updateCache(p);
                            ret.add(p);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return ret;
        });
    }

    @Nonnull
    @Override
    public Promise<Map<UUID, Profile>> lookupProfiles(@Nonnull Iterable<UUID> uniqueIds) {
        Set<UUID> toFind = new HashSet<>();
        Iterables.addAll(toFind, uniqueIds);

        Map<UUID, Profile> ret = new HashMap<>();

        for (Iterator<UUID> iterator = toFind.iterator(); iterator.hasNext(); ) {
            UUID u = iterator.next();
            Profile profile = getProfile(u);
            if (profile.getName().isPresent()) {
                ret.put(u, profile);
                iterator.remove();
            }
        }

        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (UUID uniqueId : toFind) {
            if (uniqueId == null) {
                continue;
            }

            if (!first) {
                sb.append(", ");
            }
            sb.append("UNHEX('").append(UuidUtils.toString(uniqueId)).append("')");
            first = false;
        }

        if (first) {
            return Promise.completed(ret);
        }
        sb.append(")");

        return Scheduler.supplyAsync(() -> {
            try (Connection c = sql.getConnection()) {
                try (Statement s = c.createStatement()) {
                    try (ResultSet rs = s.executeQuery(replaceTableName(String.format(SELECT_ALL_UIDS, sb.toString())))) {
                        while (rs.next()) {
                            String name = rs.getString("name");
                            Timestamp lastUpdate = rs.getTimestamp("lastupdate");
                            String uuidString = rs.getString("canonicalid");
                            UUID uuid = UuidUtils.fromString(uuidString);

                            ImmutableProfile p = new ImmutableProfile(uuid, name, lastUpdate.getTime());
                            updateCache(p);
                            ret.put(uuid, p);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return ret;
        });
    }

    @Nonnull
    @Override
    public Promise<Map<String, Profile>> lookupProfilesByName(@Nonnull Iterable<String> names) {
        Set<String> toFind = new HashSet<>();
        Iterables.addAll(toFind, names);

        Map<String, Profile> ret = new HashMap<>();

        for (Iterator<String> iterator = toFind.iterator(); iterator.hasNext(); ) {
            String u = iterator.next();
            Optional<Profile> profile = getProfile(u);
            if (profile.isPresent()) {
                ret.put(u, profile.get());
                iterator.remove();
            }
        }

        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (String name : names) {
            // check that all usernames are valid to prevent sql injection attempts
            if (name == null || !UuidUtils.isValidMcUsername(name)) {
                continue;
            }

            if (!first) {
                sb.append(", ");
            }
            sb.append("'").append(name).append("'");
            first = false;
        }

        if (first) {
            return Promise.completed(ret);
        }
        sb.append(")");

        return Scheduler.supplyAsync(() -> {
            try (Connection c = sql.getConnection()) {
                try (Statement s = c.createStatement()) {
                    try (ResultSet rs = s.executeQuery(replaceTableName(String.format(SELECT_ALL_NAMES, sb.toString())))) {
                        while (rs.next()) {
                            String name = rs.getString("name"); // provide a case corrected name
                            Timestamp lastUpdate = rs.getTimestamp("lastupdate");
                            String uuidString = rs.getString("canonicalid");
                            UUID uuid = UuidUtils.fromString(uuidString);

                            ImmutableProfile p = new ImmutableProfile(uuid, name, lastUpdate.getTime());
                            updateCache(p);
                            ret.put(name, p);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return ret;
        });
    }

}
