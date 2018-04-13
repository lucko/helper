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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * Builder utilities for creating GSON Objects/Arrays.
 */
@NonnullByDefault
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
        Objects.requireNonNull(object, "object");

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
        Objects.requireNonNull(object, "object");
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
        Objects.requireNonNull(array, "array");

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
        Objects.requireNonNull(array, "array");
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
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, {@link #nullValue()} is returned.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     */
    public static JsonElement primitive(@Nullable String value) {
        return value == null ? nullValue() : new JsonPrimitive(value);
    }

    /**
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, {@link #nullValue()} is returned.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     */
    public static JsonElement primitive(@Nullable Number value) {
        return value == null ? nullValue() : new JsonPrimitive(value);
    }

    /**
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, {@link #nullValue()} is returned.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     */
    public static JsonElement primitive(@Nullable Boolean value) {
        return value == null ? nullValue() : new JsonPrimitive(value);
    }

    /**
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, {@link #nullValue()} is returned.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     */
    public static JsonElement primitive(@Nullable Character value) {
        return value == null ? nullValue() : new JsonPrimitive(value);
    }

    /**
     * Returns an instance of {@link JsonNull}.
     *
     * @return a json null instance
     */
    public static JsonNull nullValue() {
        return JsonNull.INSTANCE;
    }

    /**
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, a {@link NullPointerException} will be thrown.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     * @throws NullPointerException if value is null
     */
    public static JsonPrimitive primitiveNonNull(String value) {
        Objects.requireNonNull(value, "value");
        return new JsonPrimitive(value);
    }

    /**
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, a {@link NullPointerException} will be thrown.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     * @throws NullPointerException if value is null
     */
    public static JsonPrimitive primitiveNonNull(Number value) {
        Objects.requireNonNull(value, "value");
        return new JsonPrimitive(value);
    }

    /**
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, a {@link NullPointerException} will be thrown.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     * @throws NullPointerException if value is null
     */
    public static JsonPrimitive primitiveNonNull(Boolean value) {
        Objects.requireNonNull(value, "value");
        return new JsonPrimitive(value);
    }

    /**
     * Creates a JsonPrimitive from the given value.
     *
     * <p>If the value is null, a {@link NullPointerException} will be thrown.</p>
     *
     * @param value the value
     * @return a json primitive for the value
     * @throws NullPointerException if value is null
     */
    public static JsonPrimitive primitiveNonNull(Character value) {
        Objects.requireNonNull(value, "value");
        return new JsonPrimitive(value);
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
     * Returns a collector which forms a JsonArray from GsonSerializables
     *
     * @return a new collector
     */
    public static Collector<GsonSerializable, JsonArrayBuilder, JsonArray> collectSerializablesToArray() {
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
    public interface JsonObjectBuilder extends BiConsumer<String, JsonElement>, Consumer<Map.Entry<String, JsonElement>> {

        @Override
        default void accept(Map.Entry<String, JsonElement> entry) {
            Objects.requireNonNull(entry, "entry");
            add(entry.getKey(), entry.getValue());
        }

        @Override
        default void accept(String property, JsonElement value) {
            add(property, value);
        }

        JsonObjectBuilder add(String property, @Nullable JsonElement value, boolean copy);

        default JsonObjectBuilder add(String property, @Nullable JsonElement value) {
            return add(property, value, false);
        }

        default JsonObjectBuilder add(String property, @Nullable GsonSerializable serializable) {
            return serializable == null ? add(property, nullValue()) : add(property, serializable.serialize());
        }

        default JsonObjectBuilder add(String property, @Nullable String value) {
            return add(property, primitive(value));
        }

        default JsonObjectBuilder add(String property, @Nullable Number value) {
            return add(property, primitive(value));
        }

        default JsonObjectBuilder add(String property, @Nullable Boolean value) {
            return add(property, primitive(value));
        }

        default JsonObjectBuilder add(String property, @Nullable Character value) {
            return add(property, primitive(value));
        }

        JsonObjectBuilder addIfAbsent(String property, @Nullable JsonElement value, boolean copy);

        default JsonObjectBuilder addIfAbsent(String property, @Nullable JsonElement value) {
            return addIfAbsent(property, value, false);
        }

        default JsonObjectBuilder addIfAbsent(String property, @Nullable GsonSerializable serializable) {
            return serializable == null ? addIfAbsent(property, nullValue()) : addIfAbsent(property, serializable.serialize());
        }

        default JsonObjectBuilder addIfAbsent(String property, @Nullable String value) {
            return addIfAbsent(property, primitive(value));
        }

        default JsonObjectBuilder addIfAbsent(String property, @Nullable Number value) {
            return addIfAbsent(property, primitive(value));
        }

        default JsonObjectBuilder addIfAbsent(String property, @Nullable Boolean value) {
            return addIfAbsent(property, primitive(value));
        }

        default JsonObjectBuilder addIfAbsent(String property, @Nullable Character value) {
            return addIfAbsent(property, primitive(value));
        }

        default <T extends JsonElement> JsonObjectBuilder addAll(Iterable<Map.Entry<String, T>> iterable, boolean deepCopy) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, T> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue(), deepCopy);
            }
            return this;
        }

        default <T extends JsonElement> JsonObjectBuilder addAll(Iterable<Map.Entry<String, T>> iterable) {
            return addAll(iterable, false);
        }

        default <T extends JsonElement> JsonObjectBuilder addAll(Stream<Map.Entry<String, T>> stream, boolean deepCopy) {
            Objects.requireNonNull(stream, "stream");
            stream.forEach(e -> {
                if (e == null || e.getKey() == null) {
                    return;
                }
                add(e.getKey(), e.getValue(), deepCopy);
            });
            return this;
        }

        default <T extends JsonElement> JsonObjectBuilder addAll(Stream<Map.Entry<String, T>> stream) {
            return addAll(stream, false);
        }

        default JsonObjectBuilder addAll(JsonObject object, boolean deepCopy) {
            Objects.requireNonNull(object, "object");
            return addAll(object.entrySet(), deepCopy);
        }

        default JsonObjectBuilder addAll(JsonObject object) {
            return addAll(object, false);
        }

        default <T extends GsonSerializable> JsonObjectBuilder addAllSerializables(Iterable<Map.Entry<String, T>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, T> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllStrings(Iterable<Map.Entry<String, String>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, String> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default <T extends Number> JsonObjectBuilder addAllNumbers(Iterable<Map.Entry<String, T>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, T> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllBooleans(Iterable<Map.Entry<String, Boolean>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, Boolean> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllCharacters(Iterable<Map.Entry<String, Character>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, Character> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                add(e.getKey(), e.getValue());
            }
            return this;
        }

        default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(Iterable<Map.Entry<String, T>> iterable, boolean deepCopy) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, T> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                addIfAbsent(e.getKey(), e.getValue(), deepCopy);
            }
            return this;
        }

        default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(Iterable<Map.Entry<String, T>> iterable) {
            return addAllIfAbsent(iterable, false);
        }

        default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(Stream<Map.Entry<String, T>> stream, boolean deepCopy) {
            Objects.requireNonNull(stream, "stream");
            stream.forEach(e -> {
                if (e == null || e.getKey() == null) {
                    return;
                }
                addIfAbsent(e.getKey(), e.getValue(), deepCopy);
            });
            return this;
        }

        default <T extends JsonElement> JsonObjectBuilder addAllIfAbsent(Stream<Map.Entry<String, T>> stream) {
            return addAllIfAbsent(stream, false);
        }

        default JsonObjectBuilder addAllIfAbsent(JsonObject object, boolean deepCopy) {
            Objects.requireNonNull(object, "object");
            return addAllIfAbsent(object.entrySet(), deepCopy);
        }

        default JsonObjectBuilder addAllIfAbsent(JsonObject object) {
            return addAllIfAbsent(object, false);
        }

        default <T extends GsonSerializable> JsonObjectBuilder addAllSerializablesIfAbsent(Iterable<Map.Entry<String, T>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, T> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                addIfAbsent(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllStringsIfAbsent(Iterable<Map.Entry<String, String>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, String> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                addIfAbsent(e.getKey(), e.getValue());
            }
            return this;
        }

        default <T extends Number> JsonObjectBuilder addAllNumbersIfAbsent(Iterable<Map.Entry<String, T>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, T> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                addIfAbsent(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllBooleansIfAbsent(Iterable<Map.Entry<String, Boolean>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, Boolean> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                addIfAbsent(e.getKey(), e.getValue());
            }
            return this;
        }

        default JsonObjectBuilder addAllCharactersIfAbsent(Iterable<Map.Entry<String, Character>> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Map.Entry<String, Character> e : iterable) {
                if (e == null || e.getKey() == null) {
                    continue;
                }
                addIfAbsent(e.getKey(), e.getValue());
            }
            return this;
        }

        JsonObject build();

    }

    /**
     * A {@link JsonArray} builder utility
     */
    public interface JsonArrayBuilder extends Consumer<JsonElement> {

        @Override
        default void accept(JsonElement value) {
            add(value);
        }

        JsonArrayBuilder add(@Nullable JsonElement value, boolean copy);

        default JsonArrayBuilder add(@Nullable JsonElement value) {
            return add(value, false);
        }

        default JsonArrayBuilder add(@Nullable GsonSerializable serializable) {
            return serializable == null ? add(nullValue()) : add(serializable.serialize());
        }

        default JsonArrayBuilder add(@Nullable String value) {
            return add(primitive(value));
        }

        default JsonArrayBuilder add(@Nullable Number value) {
            return add(primitive(value));
        }

        default JsonArrayBuilder add(@Nullable Boolean value) {
            return add(primitive(value));
        }

        default JsonArrayBuilder add(@Nullable Character value) {
            return add(primitive(value));
        }

        default <T extends JsonElement> JsonArrayBuilder addAll(Iterable<T> iterable, boolean copy) {
            Objects.requireNonNull(iterable, "iterable");
            for (T e : iterable) {
                add(e, copy);
            }
            return this;
        }

        default <T extends JsonElement> JsonArrayBuilder addAll(Iterable<T> iterable) {
            return addAll(iterable, false);
        }

        default <T extends JsonElement> JsonArrayBuilder addAll(Stream<T> stream, boolean copy) {
            Objects.requireNonNull(stream, "iterable");
            stream.forEach(e -> add(e, copy));
            return this;
        }

        default <T extends JsonElement> JsonArrayBuilder addAll(Stream<T> stream) {
            return addAll(stream, false);
        }

        default <T extends GsonSerializable> JsonArrayBuilder addSerializables(Iterable<T> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (T e : iterable) {
                add(e);
            }
            return this;
        }

        default JsonArrayBuilder addStrings(Iterable<String> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (String e : iterable) {
                add(e);
            }
            return this;
        }

        default <T extends Number> JsonArrayBuilder addNumbers(Iterable<T> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (T e : iterable) {
                add(e);
            }
            return this;
        }

        default JsonArrayBuilder addBooleans(Iterable<Boolean> iterable) {
            Objects.requireNonNull(iterable, "iterable");
            for (Boolean e : iterable) {
                add(e);
            }
            return this;
        }

        default JsonArrayBuilder addCharacters(Iterable<Character> iterable) {
            Objects.requireNonNull(iterable, "iterable");
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
        public JsonObjectBuilder add(String property, @Nullable JsonElement value, boolean copy) {
            Objects.requireNonNull(property, "property");
            if (value == null) {
                value = nullValue();
            }

            if (copy && value.isJsonObject()) {
                this.handle.add(property, object(value.getAsJsonObject(), true).build());
            } else if (copy && value.isJsonArray()) {
                this.handle.add(property, array(value.getAsJsonArray(), true).build());
            } else {
                this.handle.add(property, value);
            }
            return this;
        }

        @Override
        public JsonObjectBuilder addIfAbsent(String property, @Nullable JsonElement value, boolean copy) {
            Objects.requireNonNull(property, "property");
            if (this.handle.has(property)) {
                return this;
            }
            return add(property, value, copy);
        }

        @Override
        public JsonObject build() {
            return this.handle;
        }
    }

    private static final class JsonArrayBuilderImpl implements JsonArrayBuilder {
        private final JsonArray handle;

        private JsonArrayBuilderImpl(JsonArray handle) {
            this.handle = handle;
        }

        @Override
        public JsonArrayBuilder add(@Nullable JsonElement value, boolean copy) {
            if (value == null) {
                value = nullValue();
            }

            if (copy && value.isJsonObject()) {
                this.handle.add(object(value.getAsJsonObject(), true).build());
            } else if (copy && value.isJsonArray()) {
                this.handle.add(array(value.getAsJsonArray(), true).build());
            } else {
                this.handle.add(value);
            }

            return this;
        }

        @Override
        public JsonArray build() {
            return this.handle;
        }
    }

    private JsonBuilder() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
