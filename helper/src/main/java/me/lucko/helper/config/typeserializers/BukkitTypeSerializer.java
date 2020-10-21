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

package me.lucko.helper.config.typeserializers;

import com.google.common.reflect.TypeToken;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.LinkedHashMap;
import java.util.Map;

public final class BukkitTypeSerializer implements TypeSerializer<ConfigurationSerializable> {
    private static final TypeToken<Map<String, Object>> TYPE = new TypeToken<Map<String, Object>>(){};

    public static final BukkitTypeSerializer INSTANCE = new BukkitTypeSerializer();

    private BukkitTypeSerializer() {
    }

    @Override
    public ConfigurationSerializable deserialize(TypeToken<?> type, ConfigurationNode from) throws ObjectMappingException {
        Map<String, Object> map = from.getValue(TYPE);
        deserializeChildren(map);
        return ConfigurationSerialization.deserializeObject(map);
    }

    @Override
    public void serialize(TypeToken<?> type, ConfigurationSerializable from, ConfigurationNode to) {
        Map<String, Object> serialized = from.serialize();

        Map<String, Object> map = new LinkedHashMap<>(serialized.size() + 1);
        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(from.getClass()));
        map.putAll(serialized);

        to.setValue(map);
    }

    private static void deserializeChildren(Map<String, Object> map) {
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
                double doubleVal = ((Number) entry.getValue()).doubleValue();
                int intVal = (int) doubleVal;
                long longVal = (long) doubleVal;

                if (intVal == doubleVal) {
                    entry.setValue(intVal);
                } else if (longVal == doubleVal) {
                    entry.setValue(longVal);
                } else {
                    entry.setValue(doubleVal);
                }
            }
        }
    }
}
