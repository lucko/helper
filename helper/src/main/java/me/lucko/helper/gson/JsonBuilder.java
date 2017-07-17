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

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * Builder utilities for creating GSON Objects/Arrays.
 */
public final class JsonBuilder {

    /**
     * Creates a new object builder
     *
     * <p>If copy is not true, the passed object will be mutated by the builder methods.</p>
     *
     * @param object the object to base the new builder upon
     * @param copy if the object should be deep copied, or just referenced.
     * @return a new builder
     */
    public static JsonObjectBuilder object(JsonObject object, boolean copy) {
        Preconditions.checkNotNull(object, "object");

        if (copy) {
            return object().addAll(object, true);
        } else {
            return new JsonObjectBuilderImpl(object);
        }
    }

    /**
     * Creates a new object builder, without copying the passed object.
     *
     * <p>Equivalent to calling {@link #object(JsonObject, boolean)} with copy = false.</p>
     *
     * @param object the object to base the new builder upon
     * @return a new builder
     */
    public static JsonObjectBuilder object(JsonObject object) {
        Preconditions.checkNotNull(object, "object");
        return object(object, false);
    }

    /**
     * Creates a new object builder, with no initial values
     *
     * @return a new builder
     */
    public static JsonObjectBuilder object() {
        return object(new JsonObject());
    }

    /**
     * Creates a new array builder
     *
     * <p>If copy is not true, the passed array will be mutated by the builder methods.</p>
     *
     * @param array the array to base the new builder upon
     * @param copy if the array should be deep copied, or just referenced.
     * @return a new builder
     */
    public static JsonArrayBuilder array(JsonArray array, boolean copy) {
        Preconditions.checkNotNull(array, "array");

        if (copy) {
            return array().addAll(array, true);
        } else {
            return new JsonArrayBuilderImpl(array);
        }
    }

    /**
     * Creates a new array builder, without copying the passed array.
     *
     * <p>Equivalent to calling {@link #array(JsonArray, boolean)} with copy = false.</p>
     *
     * @param array the array to base the new builder upon
     * @return a new builder
     */
    public static JsonArrayBuilder array(JsonArray array) {
        Preconditions.checkNotNull(array, "array");
        return array(array, false);
    }

    /**
     * Creates a new array builder, with no initial values
     *
     * @return a new builder
     */
    public static JsonArrayBuilder array() {
        return array(new JsonArray());
    }

    /**
     * Returns a collector which forms a JsonObject using the key and value mappers
     *
     * @param keyMapper the function to map from T to {@link String}
     * @param valueMapper the function to map from T to {@link JsonElement}
     * @param <T> the type
     * @return a new collector
     */
    public static <T> Collector<T, JsonObjectBuilder, JsonObject> collectToObject(Function<? super T, String> keyMapper, Function<? super T, JsonElement> valueMapper) {
        return Collector.of(
                JsonBuilder::object,
                (r, t) -> r.add(keyMapper.apply(t), valueMapper.apply(t)),
                (l, r) -> l.addAll(r.build()),
                JsonObjectBuilder::build
        );
    }

    /**
     * Returns a collector which forms a JsonArray using the value mapper
     *
     * @param valueMapper the function to map from T to {@link JsonElement}
     * @param <T> the type
     * @return a new collector
     */
    public static <T> Collector<T, JsonArrayBuilder, JsonArray> collectToArray(Function<? super T, JsonElement> valueMapper) {
        return Collector.of(
                JsonBuilder::array,
                (r, t) -> r.add(valueMapper.apply(t)),
                (l, r) -> l.addAll(r.build()),
                JsonArrayBuilder::build
        );
    }

    /**
     * Returns a collector which forms a JsonArray from JsonElements
     *
     * @return a new collector
     */
    public static Collector<JsonElement, JsonArrayBuilder, JsonArray> collectToArray() {
        return Collector.of(
                JsonBuilder::array,
                JsonArrayBuilder::add,
                (l, r) -> l.addAll(r.build()),
                JsonArrayBuilder::build
        );
    }

    /**
     * A {@link JsonObject} builder utility
     */
    public interface JsonObjectBuilder {

        JsonObjectBuilder add(String property, JsonElement value, boolean copy);

        default JsonObjectBuilder add(String property, JsonElement value) {
            return add(property, value, false);
        }

        default JsonObjectBuilder add(String property, GsonSerializable serializable) {
            Preconditions.checkNotNull(serializable, "serializable");
            return add(property, serializable.serialize());
        }

        default JsonObjectBuilder add(String property, String value) {
            Preconditions.checkNotNull(value, "value");
            return add(property, new JsonPrimitive(value));
        }

        default JsonObjectBuilder add(String property, Number value) {
            Preconditions.checkNotNull(value, "value");
            return add(property, new JsonPrimitive(value));
        }

        default JsonObjectBuilder add(String property, Boolean value) {
            Preconditions.checkNotNull(value, "value");
            return add(property, new JsonPrimitive(value));
        }

        default JsonObjectBuilder add(String property, Character value) {
            Preconditions.checkNotNull(value, "value");
            return add(property, new JsonPrimitive(value));
        }

        JsonObjectBuilder addIfAbsent(String property, JsonElement value, boolean copy);

        default JsonObjectBuilder addIfAbsent(String property, JsonElement value) {
            return addIfAbsent(property, value, false);
        }

        default JsonObjectBuilder addIfAbsent(String property, GsonSerializable serializable) {
            Preconditions.checkNotNull(serializable, "serializable");
            return addIfAbsent(property, serializable.serialize());
        }

        default JsonObjectBuilder addIfAbsent(String property, String value) {
            Preconditions.checkNotNull(value, "value");
            return addIfAbsent(property, new JsonPrimitive(value));
        }

        default JsonObjectBuilder addIfAbsent(String property, Number value) {
            Preconditions.checkNotNull(value, "value");
            return addIfAbsent(property, new JsonPrimitive(value));
        }

        default JsonObjectBuilder addIfAbsent(String property, Boolean value) {
            Preconditions.checkNotNull(value, "value");
            return addIfAbsent(property, new JsonPrimitive(value));
        }

        default JsonObjectBuilder addIfAbsent(String property, Character value) {
            Preconditions.checkNotNull(value, "value");
            return addIfAbsent(property, new JsonPrimitive(value));
        }

        JsonObjectBuilder addAll(Iterable<Map.Entry<String, JsonElement>> iterable, boolean deepCopy);

        default JsonObjectBuilder addAll(Iterable<Map.Entry<String, JsonElement>> iterable) {
            return addAll(iterable, false);
        }

        default JsonObjectBuilder addAll(JsonObject object, boolean deepCopy) {
            Preconditions.checkNotNull(object, "object");
            return addAll(object.entrySet(), deepCopy);
        }

        default JsonObjectBuilder addAll(JsonObject object) {
            return addAll(object, false);
        }

        default JsonObjectBuilder addAllStrings(Iterable<Map.Entry<String, String>> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Map.Entry<String, String> e : iterable) {
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllNumbers(Iterable<Map.Entry<String, Number>> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Map.Entry<String, Number> e : iterable) {
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllBooleans(Iterable<Map.Entry<String, Boolean>> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Map.Entry<String, Boolean> e : iterable) {
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllCharacters(Iterable<Map.Entry<String, Character>> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Map.Entry<String, Character> e : iterable) {
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        JsonObjectBuilder addAllIfAbsent(Iterable<Map.Entry<String, JsonElement>> iterable, boolean deepCopy);

        default JsonObjectBuilder addAllIfAbsent(Iterable<Map.Entry<String, JsonElement>> iterable) {
            return addAllIfAbsent(iterable, false);
        }

        default JsonObjectBuilder addAllIfAbsent(JsonObject object, boolean deepCopy) {
            Preconditions.checkNotNull(object, "object");
            return addAllIfAbsent(object.entrySet(), deepCopy);
        }

        default JsonObjectBuilder addAllIfAbsent(JsonObject object) {
            return addAllIfAbsent(object, false);
        }

        JsonObject build();

    }

    /**
     * A {@link JsonArray} builder utility
     */
    public interface JsonArrayBuilder {

        JsonArrayBuilder add(JsonElement value, boolean copy);

        default JsonArrayBuilder add(JsonElement value) {
            return add(value, false);
        }

        default JsonArrayBuilder add(GsonSerializable serializable) {
            Preconditions.checkNotNull(serializable, "serializable");
            return add(serializable.serialize());
        }

        default JsonArrayBuilder add(String value) {
            Preconditions.checkNotNull(value, "value");
            return add(new JsonPrimitive(value));
        }

        default JsonArrayBuilder add(Number value) {
            Preconditions.checkNotNull(value, "value");
            return add(new JsonPrimitive(value));
        }

        default JsonArrayBuilder add(Boolean value) {
            Preconditions.checkNotNull(value, "value");
            return add(new JsonPrimitive(value));
        }

        default JsonArrayBuilder add(Character value) {
            Preconditions.checkNotNull(value, "value");
            return add(new JsonPrimitive(value));
        }

        JsonArrayBuilder addAll(Iterable<JsonElement> iterable, boolean copy);

        default JsonArrayBuilder addAll(Iterable<JsonElement> iterable) {
            return addAll(iterable, false);
        }

        default JsonArrayBuilder addStrings(Iterable<String> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (String e : iterable) {
                add(e);
            }
            return this;
        }

        default JsonArrayBuilder addNumbers(Iterable<Number> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Number e : iterable) {
                add(e);
            }
            return this;
        }

        default JsonArrayBuilder addBooleans(Iterable<Boolean> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Boolean e : iterable) {
                add(e);
            }
            return this;
        }

        default JsonArrayBuilder addCharacters(Iterable<Character> iterable) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Character e : iterable) {
                add(e);
            }
            return this;
        }

        JsonArray build();

    }

    private static final class JsonObjectBuilderImpl implements JsonObjectBuilder {
        private final JsonObject handle;

        private JsonObjectBuilderImpl(JsonObject handle) {
            this.handle = handle;
        }

        @Override
        public JsonObjectBuilder add(String property, JsonElement value, boolean copy) {
            Preconditions.checkNotNull(property, "property");
            Preconditions.checkNotNull(value, "value");
            if (copy && value.isJsonObject()) {
                handle.add(property, object(value.getAsJsonObject(), true).build());
            } else if (copy && value.isJsonArray()) {
                handle.add(property, array(value.getAsJsonArray(), true).build());
            } else {
                handle.add(property, value);
            }
            return this;
        }

        @Override
        public JsonObjectBuilder addIfAbsent(String property, JsonElement value, boolean copy) {
            Preconditions.checkNotNull(property, "property");
            if (handle.has(property)) {
                return this;
            }
            return add(property, value, copy);
        }

        @Override
        public JsonObjectBuilder addAll(Iterable<Map.Entry<String, JsonElement>> iterable, boolean deepCopy) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Map.Entry<String, JsonElement> e : iterable) {
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue(), deepCopy);
            }
            return this;
        }

        @Override
        public JsonObjectBuilder addAllIfAbsent(Iterable<Map.Entry<String, JsonElement>> iterable, boolean deepCopy) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (Map.Entry<String, JsonElement> e : iterable) {
                if (e == null || e.getKey() == null || e.getValue() == null) {
                    continue;
                }
                addIfAbsent(e.getKey(), e.getValue(), deepCopy);
            }
            return this;
        }

        @Override
        public JsonObject build() {
            return handle;
        }
    }

    private static final class JsonArrayBuilderImpl implements JsonArrayBuilder {
        private final JsonArray handle;

        private JsonArrayBuilderImpl(JsonArray handle) {
            this.handle = handle;
        }

        @Override
        public JsonArrayBuilder add(JsonElement value, boolean copy) {
            Preconditions.checkNotNull(value, "value");

            if (copy && value.isJsonObject()) {
                handle.add(object(value.getAsJsonObject(), true).build());
            } else if (copy && value.isJsonArray()) {
                handle.add(array(value.getAsJsonArray(), true).build());
            } else {
                handle.add(value);
            }

            return this;
        }

        @Override
        public JsonArrayBuilder addAll(Iterable<JsonElement> iterable, boolean copy) {
            Preconditions.checkNotNull(iterable, "iterable");
            for (JsonElement e : iterable) {
                if (e == null) {
                    continue;
                }
                add(e, copy);
            }
            return this;
        }

        @Override
        public JsonArray build() {
            return handle;
        }
    }

    private JsonBuilder() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
