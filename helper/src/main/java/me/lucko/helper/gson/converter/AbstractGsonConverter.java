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

package me.lucko.helper.gson.converter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
abstract class AbstractGsonConverter<M extends Map<String, Object>, L extends List<Object>, S extends Set<Object>> implements GsonConverter {

    protected abstract MapBuilder<M, String, Object> newMapBuilder();
    protected abstract ListBuilder<L, Object> newListBuilder();
    protected abstract SetBuilder<S, Object> newSetBuilder();

    // gson --> standard java objects

    @Override
    public M unwrapObject(JsonObject object) {
        MapBuilder<M, String, Object> builder = newMapBuilder();
        for (Map.Entry<String, JsonElement> e : object.entrySet()) {
            builder.put(e.getKey(), unwrapElement(e.getValue()));
        }
        return builder.build();
    }

    @Override
    public L unwrapArray(JsonArray array) {
        ListBuilder<L, Object> builder = newListBuilder();
        for (JsonElement element : array) {
            builder.add(unwrapElement(element));
        }
        return builder.build();
    }

    @Override
    public S unwrapArrayToSet(JsonArray array) {
        SetBuilder<S, Object> builder = newSetBuilder();
        for (JsonElement element : array) {
            builder.add(unwrapElement(element));
        }
        return builder.build();
    }

    @Override
    public Object unwarpPrimitive(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        } else if (primitive.isNumber()) {
            return primitive.getAsNumber();
        } else if (primitive.isString()) {
            return primitive.getAsString();
        } else {
            throw new IllegalArgumentException("Unknown primitive type: " + primitive);
        }
    }

    @Override
    @Nullable
    public Object unwrapElement(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonArray()) {
            return unwrapArray(element.getAsJsonArray());
        } else if (element.isJsonObject()) {
            return unwrapObject(element.getAsJsonObject());
        } else if (element.isJsonPrimitive()) {
            return unwarpPrimitive(element.getAsJsonPrimitive());
        } else {
            throw new IllegalArgumentException("Unknown element type: " + element);
        }
    }

    // standard collections --> gson

    @Override
    public JsonElement wrap(Object object) {
        if (object instanceof JsonElement) {
            return ((JsonElement) object);
        } else if (object instanceof Iterable<?>) {
            Iterable iterable = (Iterable) object;
            JsonArray array = new JsonArray();
            for (Object o : iterable) {
                array.add(wrap(o));
            }
            return array;
        } else if (object instanceof Map<?, ?>) {
            Map<?, ?> map = ((Map) object);
            JsonObject obj = new JsonObject();
            for (Map.Entry<?, ?> e : map.entrySet()) {
                if (e.getKey() instanceof String) {
                    String key = (String) e.getKey();
                    obj.add(key, wrap(e.getValue()));
                }
            }
            return obj;
        } else if (object instanceof String) {
            return new JsonPrimitive(((String) object));
        } else if (object instanceof Character) {
            return new JsonPrimitive(((Character) object));
        } else if (object instanceof Boolean) {
            return new JsonPrimitive(((Boolean) object));
        } else if (object instanceof Number) {
            return new JsonPrimitive(((Number) object));
        } else {
            throw new IllegalArgumentException("Unable to wrap object: " + object.getClass());
        }
    }

    protected interface MapBuilder<M extends Map<K, V>, K, V> {
        void put(@Nullable K key, @Nullable V value);
        M build();
    }

    protected interface ListBuilder<L extends List<E>, E> {
        void add(@Nullable E element);
        L build();
    }

    protected interface SetBuilder<S extends Set<E>, E> {
        void add(@Nullable E element);
        S build();
    }

}
