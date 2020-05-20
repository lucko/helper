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

package me.lucko.helper.profiles;

import com.google.common.collect.Lists;
import com.google.gson.stream.JsonReader;

import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.utils.UndashedUuids;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Utilities for interacting with the Mojang API.
 */
@Deprecated // API subject to change
public final class MojangApi {
    public static final String PROFILES_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    public static final String NAME_HISTORY_URL = "https://api.mojang.com/user/profiles/%s/names";

    /**
     * Gets a promise for the online {@link UUID} of a player by fetching it from the Mojang API.
     * The Mojang API has a limit of 600 requests per 10 minutes.
     *
     * @param username the username of the player from which to get the {@link UUID}
     * @return a promise for the online {@link UUID} of the player with the supplied name
     */
    public static Promise<UUID> usernameToUuid(String username) {
        Objects.requireNonNull(username, "username");
        if (username.isEmpty()) {
            throw new IllegalArgumentException("empty");
        }

        return Schedulers.async().supply(() -> {
            try (JsonReader reader = new JsonReader(new InputStreamReader(new URL(String.format(PROFILES_URL, username)).openConnection().getInputStream()))) {
                reader.beginObject();
                reader.skipValue();
                return UndashedUuids.fromString(reader.nextString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    /**
     * Gets a promise of the online username of the player with the supplied {@link UUID} by fetching it from the Mojang API.
     * The Mojang API has a limit of 600 requests per 10 minutes.
     *
     * @param uuid the {@link UUID} of the player from which to get the name
     * @return a promise of the online username of the player with the supplied uuid, or null if it doesn't exists
     */
    public static Promise<String> uuidToUsername(UUID uuid) {
        return getUsernameHistory(uuid).thenApplyAsync(names -> names.isEmpty() ? null : names.get(names.size() - 1));
    }

    /**
     * Gets a promise of the history of the names owned by the player with the supplied {@link UUID}, in chronological order, fetching it from the Mojang API.
     * The Mojang API has a limit of 600 requests per 10 minutes.
     *
     * @param uuid the {@link UUID} of the player from which to fetch the name history
     * @return a promise of a List String with all the names owned by the player with the supplied uuid, in chronological order
     */
    public static Promise<List<String>> getUsernameHistory(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");

        return Schedulers.async().supply(() -> {
            List<String> names = Lists.newArrayList();

            try (JsonReader reader = new JsonReader(new InputStreamReader(new URL(String.format(NAME_HISTORY_URL, UndashedUuids.toString(uuid))).openConnection().getInputStream()))) {
                reader.beginArray();
                for (int i = 0; reader.hasNext(); i++) {
                    reader.beginObject();
                    reader.skipValue();
                    names.add(reader.nextString());
                    if (i != 0) {
                        reader.skipValue();
                        reader.skipValue();
                    }
                    reader.endObject();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return names;
        });
    }

    private MojangApi() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
