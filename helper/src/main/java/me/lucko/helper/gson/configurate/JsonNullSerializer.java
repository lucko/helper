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

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public class JsonNullSerializer implements TypeSerializer<JsonNull> {
    private static final TypeToken<JsonNull> JSON_NULL_TYPE = TypeToken.of(JsonNull.class);

    public static final JsonNullSerializer INSTANCE = new JsonNullSerializer();

    private JsonNullSerializer() {
    }

    @Override
    public JsonNull deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
        if (!type.equals(JSON_NULL_TYPE)) {
            throw new ObjectMappingException("Unable to map type: " + type.toString());
        }
        return JsonNull.INSTANCE;
    }

    @Override
    public void serialize(TypeToken<?> type, JsonNull jsonNull, ConfigurationNode node) throws ObjectMappingException {
        if (!type.equals(JSON_NULL_TYPE)) {
            throw new ObjectMappingException("Unable to map type: " + type.toString());
        }
        node.setValue(null);
    }

}
