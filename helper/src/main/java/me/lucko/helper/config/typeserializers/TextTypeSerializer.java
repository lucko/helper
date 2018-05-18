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
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import me.lucko.helper.gson.GsonProvider;

import net.kyori.text.Component;
import net.kyori.text.serializer.ComponentSerializers;
import net.kyori.text.serializer.GsonComponentSerializer;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class TextTypeSerializer implements TypeSerializer<Component> {
    private static final GsonComponentSerializer DELEGATE = (GsonComponentSerializer) ComponentSerializers.JSON;

    public static final TextTypeSerializer INSTANCE = new TextTypeSerializer();

    private TextTypeSerializer() {
    }

    @Override
    public Component deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
        JsonElement json = node.getValue(TypeToken.of(JsonElement.class));
        return DELEGATE.deserialize(json, typeToken.getType(), new GsonContext());
    }

    @Override
    public void serialize(TypeToken<?> typeToken, Component component, ConfigurationNode node) throws ObjectMappingException {
        JsonElement element = DELEGATE.serialize(component, typeToken.getType(), new GsonContext());
        node.setValue(TypeToken.of(JsonElement.class), element);
    }

    private static final class GsonContext implements JsonSerializationContext, JsonDeserializationContext {
        @Override
        public JsonElement serialize(Object src) {
            return GsonProvider.standard().toJsonTree(src);
        }

        @Override
        public JsonElement serialize(Object src, Type typeOfSrc) {
            return GsonProvider.standard().toJsonTree(src, typeOfSrc);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R> R deserialize(JsonElement json, Type typeOfT) throws JsonParseException {
            return (R) GsonProvider.standard().fromJson(json, typeOfT);
        }
    }
}
