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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.Map;

public class JsonObjectSerializer implements TypeSerializer<JsonObject> {
    private static final TypeToken<JsonElement> JSON_ELEMENT_TYPE = TypeToken.of(JsonElement.class);

    public static final JsonObjectSerializer INSTANCE = new JsonObjectSerializer();

    private JsonObjectSerializer() {
    }

    @Override
    public JsonObject deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
        JsonObject object = new JsonObject();
        for (Map.Entry<Object, ? extends ConfigurationNode> child : node.getChildrenMap().entrySet()) {
            object.add(child.getKey().toString(), child.getValue().getValue(JSON_ELEMENT_TYPE));
        }
        return object;
    }

    @Override
    public void serialize(TypeToken<?> type, JsonObject object, ConfigurationNode node) throws ObjectMappingException {
        for (Map.Entry<String, JsonElement> e : object.entrySet()) {
            node.getNode(e.getKey()).setValue(JSON_ELEMENT_TYPE, e.getValue());
        }
    }
}
