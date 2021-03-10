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

package me.lucko.helper.network;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.messaging.InstanceData;
import me.lucko.helper.profiles.Profile;

import java.util.Map;
import java.util.UUID;

/**
 * Represents an individual server within a {@link Network}.
 */
public interface Server extends InstanceData {

    /**
     * Gets if the server is currently online
     *
     * @return if the server is online
     */
    boolean isOnline();

    /**
     * Gets the time the last ping was received from this server.
     *
     * @return the time of the last time, as a unix timestamp in milliseconds
     */
    long getLastPing();

    /**
     * Gets the players known to be online on this server.
     *
     * @return the online players.
     */
    Map<UUID, Profile> getOnlinePlayers();

    /**
     * Gets the maximum amount of players allowed on this server.
     *
     * @return the max players
     */
    int getMaxPlayers();

    /**
     * Checks if the server is full.
     *
     * @return if the server is full or not
     */
    boolean isFull();

    /**
     * Gets whether the server is currently whitelisted.
     *
     * @return if the server is whitelisted
     */
    boolean isWhitelisted();

    Map<String, JsonElement> getRawMetadata();

    default <T> T getMetadata(String key, Class<T> type) {
        return getMetadata(key, TypeToken.of(type));
    }

    default <T> T getMetadata(String key, TypeToken<T> type) {
        JsonElement data = getRawMetadata().get(key);
        if (data == null) {
            return null;
        }
        return GsonProvider.standard().fromJson(data, type.getType());
    }
}
