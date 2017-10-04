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

package me.lucko.helper.gson.configurate;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import me.lucko.helper.gson.converter.GsonConverters;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class JsonPrimitiveSerializer implements TypeSerializer<JsonPrimitive> {
    public static final JsonPrimitiveSerializer INSTANCE = new JsonPrimitiveSerializer();

    private JsonPrimitiveSerializer() {
    }

    @Override
    public JsonPrimitive deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
        Object value = node.getValue(JsonNull.INSTANCE);
        if (value instanceof String) {
            return new JsonPrimitive(((String) value));
        } else if (value instanceof Character) {
            return new JsonPrimitive(((Character) value));
        } else if (value instanceof Boolean) {
            return new JsonPrimitive(((Boolean) value));
        } else if (value instanceof Number) {
            return new JsonPrimitive(((Number) value));
        } else {
            throw new ObjectMappingException("Unable to wrap object: " + value.getClass());
        }
    }

    @Override
    public void serialize(TypeToken<?> type, JsonPrimitive primitive, ConfigurationNode node) throws ObjectMappingException {
        node.setValue(GsonConverters.IMMUTABLE.unwarpPrimitive(primitive));
    }

}
