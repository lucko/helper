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

package me.lucko.helper.gson;

import com.google.common.math.DoubleMath;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public final class BukkitSerializableAdapterFactory implements TypeAdapterFactory {

    @Nonnull
    public static final BukkitSerializableAdapterFactory INSTANCE = new BukkitSerializableAdapterFactory();

    private BukkitSerializableAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> clazz = type.getRawType();

        if (!ConfigurationSerializable.class.isAssignableFrom(clazz)) {
            return null;
        }

        //noinspection unchecked
        return (TypeAdapter<T>) new Adapter(gson);
    }

    private static final class Adapter extends TypeAdapter<ConfigurationSerializable> {
        private final Type RAW_OUTPUT_TYPE = new TypeToken<Map<String, Object>>(){}.getType();
        private final Gson gson;

        private Adapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, ConfigurationSerializable value) {
            Map<String, Object> serialized = value.serialize();

            Map<String, Object> map = new LinkedHashMap<>(serialized.size() + 1);
            map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(value.getClass()));
            map.putAll(serialized);

            gson.toJson(map, RAW_OUTPUT_TYPE, out);
        }

        @Override
        public ConfigurationSerializable read(JsonReader in) {
            Map<String, Object> map = gson.fromJson(in, RAW_OUTPUT_TYPE);
            deserializeChildren(map);
            return ConfigurationSerialization.deserializeObject(map);
        }

        private void deserializeChildren(Map<String, Object> map) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    try {
                        //noinspection unchecked
                        Map<String, Object> value = (Map) entry.getValue();

                        deserializeChildren(value);

                        if (value.containsKey("==")) {
                            entry.setValue(ConfigurationSerialization.deserializeObject(value));
                        }

                    } catch (Exception e) {
                        // ignore
                    }
                }

                if (entry.getValue() instanceof Number) {
                    Number value = (Number) entry.getValue();

                    // cast gson's number interpretation down to integer if possible
                    if (value instanceof Double && DoubleMath.isMathematicalInteger(value.doubleValue())) {
                        value = value.intValue();
                    }

                    if (value instanceof Long) {
                        long l = value.longValue();
                        if (l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) {
                            value = value.intValue();
                        }
                    }

                    entry.setValue(value);
                }

            }
        }
    }

}
