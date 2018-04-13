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

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Represents the credentials for a remote database.
 */
public final class MongoDatabaseCredentials {

    @Nonnull
    public static MongoDatabaseCredentials of(@Nonnull String address, int port, @Nonnull String database, @Nonnull String username, @Nonnull String password) {
        return new MongoDatabaseCredentials(address, port, database, username, password);
    }

    @Nonnull
    public static MongoDatabaseCredentials fromConfig(@Nonnull ConfigurationSection config) {
        return of(
                config.getString("address", "localhost"),
                config.getInt("port", 27017),
                config.getString("database", "minecraft"),
                config.getString("username", "root"),
                config.getString("password", "passw0rd")
        );
    }

    private final String address;
    private final int port;
    private final String database;
    private final String username;
    private final String password;

    private MongoDatabaseCredentials(@Nonnull String address, int port, @Nonnull String database, @Nonnull String username, @Nonnull String password) {
        this.address = Objects.requireNonNull(address);
        this.port = port;
        this.database = Objects.requireNonNull(database);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    @Nonnull
    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    @Nonnull
    public String getDatabase() {
        return this.database;
    }

    @Nonnull
    public String getUsername() {
        return this.username;
    }

    @Nonnull
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MongoDatabaseCredentials)) return false;
        final MongoDatabaseCredentials other = (MongoDatabaseCredentials) o;

        return this.getAddress().equals(other.getAddress()) &&
                this.getPort() == other.getPort() &&
                this.getDatabase().equals(other.getDatabase()) &&
                this.getUsername().equals(other.getUsername()) &&
                this.getPassword().equals(other.getPassword());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getPort();
        result = result * PRIME + this.getAddress().hashCode();
        result = result * PRIME + this.getDatabase().hashCode();
        result = result * PRIME + this.getUsername().hashCode();
        result = result * PRIME + this.getPassword().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MongoDatabaseCredentials(" +
                "address=" + this.getAddress() + ", " +
                "port=" + this.getPort() + ", " +
                "database=" + this.getDatabase() + ", " +
                "username=" + this.getUsername() + ", " +
                "password=" + this.getPassword() + ")";
    }
}
