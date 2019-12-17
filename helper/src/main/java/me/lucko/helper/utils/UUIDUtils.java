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

package me.lucko.helper.utils;

import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * An utility to manage {@link UUID}s.
 */
public final class UUIDUtils {

    /**
     * Gets the online {@link UUID} of a player by fetching it from the Mojang API.
     * The Mojang API has a limit of 600 requests per 10 minutes.
     *
     * @param username the username of the player from which to get the {@link UUID}
     * @return the online {@link UUID} of the player with the supplied name
     */
    public static UUID getOnlineUUID(String username) {
        try {
            URLConnection conn = new URL("https://api.mojang.com/users/profiles/minecraft/" + username).openConnection();
            JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
            reader.beginObject();
            reader.skipValue();
            UUID uuid = fromString(reader.nextString());
            reader.close();
            return uuid;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the offline {@link UUID} of a player by calculating it from his name.
     *
     * @param username the name of the player from which to calculate the {@link UUID}
     * @return the offline {@link UUID} of the player with the supplied name
     */
    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }

    /**
     * Checks if the supplied {@link UUID} matches with the online one of the player with the supplied username.
     * This method uses the Mojang API, that has a limit of 600 requests per 10 minutes.
     *
     * @param username the username of the player to check if his online {@link UUID} matches with the supplied one
     * @param uuid the {@link UUID} to check if it's equal to the online one of the player with the supplied username
     * @return true if the supplied uuid is equal to the online one of the player with the supplied username, false otherwise
     */
    public static boolean isOnlineUUID(String username, UUID uuid) {
        return Objects.equals(getOnlineUUID(username), uuid);
    }

    /**
     * Checks if the supplied {@link UUID} is offline, compared to the one calculated from the supplied username.
     *
     * @param username the name of the player from which to calculate the offline uuid, in order to compare it to the supplied one
     * @param uuid the {@link UUID} to check if is offline
     * @return true if the supplied uuid is equals to the offline one calculated from the supplied username, otherwise false
     */
    public static boolean isOfflineUUID(String username, UUID uuid) {
        return getOfflineUUID(username).equals(uuid);
    }

    /**
     * Gets the online username of the player with the supplied {@link UUID} by fetching it from the Mojang API.
     * The Mojang API has a limit of 600 requests per 10 minutes.
     *
     * @param uuid the {@link UUID} of the player from which to get the name
     * @return the online username of the player with the supplied uuid, or null if it doesn't exists
     */
    public static String getName(UUID uuid) {
        List<String> names = getNameHistory(uuid);
        return names.isEmpty() ? null : names.get(names.size() - 1);
    }

    /**
     * Gets the history of the names owned by the player with the supplied {@link UUID}, in chronological order, fetching it from the Mojang API.
     * The Mojang API has a limit of 600 requests per 10 minutes.
     *
     * @param uuid the {@link UUID} of the player from which to fetch the name history
     * @return a {@link List<String>} with all the names owned by the player with the supplied uuid, in chronological order
     */
    public static List<String> getNameHistory(UUID uuid) {
        final List<String> names = new ArrayList<>();
        try {
            URLConnection conn = new URL("https://api.mojang.com/user/profiles/" + trimUUID(uuid) + "/names").openConnection();
            JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
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
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return names;
    }

    /**
     * Gets the {@link UUID} from the {@link String} representation of it, even if it is without dashes.
     *
     * @param uuid the {@link String} from which to parse the {@link UUID}
     * @return the {@link UUID} represented by the supplied {@link String}
     */
    public static UUID fromString(String uuid) {
        if (uuid.contains("-"))
            return UUID.fromString(uuid);
        StringBuilder builder = new StringBuilder(uuid.trim());
        builder.insert(20, "-");
        builder.insert(16, "-");
        builder.insert(12, "-");
        builder.insert(8, "-");
        return UUID.fromString(builder.toString());
    }

    /**
     * Gets the trimmed version of the supplied {@link UUID}.
     *
     * @param uuid the uuid to trim
     * @return the {@link String} representation of the supplied uuid, without dashes
     */
    public static String trimUUID(UUID uuid) {
        return uuid.toString().trim().replace("-", "");
    }

    private UUIDUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}