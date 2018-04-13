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

package me.lucko.helper.redis;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the credentials for a remote redis instance.
 */
public final class RedisCredentials {

    public static RedisCredentials of(@Nonnull String address, int port, @Nullable String password) {
        return new RedisCredentials(address, port, password);
    }

    public static RedisCredentials of(@Nonnull String address, int port) {
        return new RedisCredentials(address, port, null);
    }

    public static RedisCredentials fromConfig(@Nonnull ConfigurationSection config) {
        return of(
                config.getString("address", "localhost"),
                config.getInt("port", 6379),
                config.getString("password")
        );
    }

    private final String address;
    private final int port;
    private final String password;

    private RedisCredentials(@Nonnull String address, int port, @Nullable String password) {
        this.address = Objects.requireNonNull(address);
        this.port = port;
        this.password = password == null ? "" : password;
    }

    @Nonnull
    public String getAddress() {
        return this.address;
    }

    public int getPort() {
        return this.port;
    }

    @Nonnull
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RedisCredentials)) return false;
        final RedisCredentials other = (RedisCredentials) o;

        return this.getAddress().equals(other.getAddress()) &&
                this.getPort() == other.getPort() &&
                this.getPassword().equals(other.getPassword());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getAddress().hashCode();
        result = result * PRIME + this.getPort();
        result = result * PRIME + this.getPassword().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "RedisCredentials(" +
                "address=" + this.getAddress() + ", " +
                "port=" + this.getPort() + ", " +
                "password=" + this.getPassword() + ")";
    }
}
