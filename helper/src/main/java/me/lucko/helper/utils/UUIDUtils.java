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
        final List<String> names = new ArrayList<String>();
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