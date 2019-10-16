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

public final class UUIDUtils {

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

    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }

    public static boolean isOnlineUUID(String username, UUID uuid) {
        return Objects.equals(getOnlineUUID(username), uuid);
    }

    public static boolean isOfflineUUID(String username, UUID uuid) {
        return getOfflineUUID(username).equals(uuid);
    }

    public static String getName(UUID uuid) {
        List<String> names = getNameHistory(uuid);
        return names.size() == 0 ? null : names.get(names.size() - 1);
    }

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

    public static String trimUUID(UUID uuid) {
        return uuid.toString().trim().replace("-", "");
    }

    private UUIDUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}