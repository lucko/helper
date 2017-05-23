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

/**
 * Builder utilities for creating GSON Objects/Arrays.
 */
public final class JsonBuilder {

    public static JsonObjectBuilder object() {
        return new JsonObjectBuilder();
    }

    public static JsonObjectBuilder object(JsonObject object) {
        return new JsonObjectBuilder(object);
    }

    public static JsonArrayBuilder array() {
        return new JsonArrayBuilder();
    }

    public static JsonArrayBuilder array(JsonArray array) {
        return new JsonArrayBuilder(array);
    }

    public static class JsonObjectBuilder {
        private final JsonObject handle;

        private JsonObjectBuilder(JsonObject object) {
            this.handle = Preconditions.checkNotNull(object, "object");
        }

        private JsonObjectBuilder() {
            this.handle = new JsonObject();
        }

        public JsonObjectBuilder add(String property, JsonElement value) {
            handle.add(property, value);
            return this;
        }

        public JsonObjectBuilder add(String property, String value) {
            return add(property, new JsonPrimitive(value));
        }

        public JsonObjectBuilder add(String property, Number value) {
            return add(property, new JsonPrimitive(value));
        }

        public JsonObjectBuilder add(String property, Boolean value) {
            return add(property, new JsonPrimitive(value));
        }

        public JsonObjectBuilder add(String property, Character value) {
            return add(property, new JsonPrimitive(value));
        }

        public JsonObject build() {
            return handle;
        }
    }

    public static class JsonArrayBuilder {
        private final JsonArray handle;

        private JsonArrayBuilder(JsonArray array) {
            this.handle = Preconditions.checkNotNull(array, "object");
        }

        private JsonArrayBuilder() {
            this.handle = new JsonArray();
        }

        public JsonArrayBuilder add(JsonElement value) {
            handle.add(value);
            return this;
        }

        public JsonArrayBuilder add(String value) {
            return add(new JsonPrimitive(value));
        }

        public JsonArrayBuilder add(Number value) {
            return add(new JsonPrimitive(value));
        }

        public JsonArrayBuilder add(Boolean value) {
            return add(new JsonPrimitive(value));
        }

        public JsonArrayBuilder add(Character value) {
            return add(new JsonPrimitive(value));
        }

        public JsonArrayBuilder addAll(JsonArray array) {
            return addAll(array);
        }

        public JsonArrayBuilder addAll(Iterable<JsonElement> iterable) {
            for (JsonElement s : iterable) {
                add(s);
            }
            return this;
        }

        public JsonArrayBuilder addStrings(Iterable<String> iterable) {
            for (String s : iterable) {
                add(s);
            }
            return this;
        }

        public JsonArrayBuilder addNumbers(Iterable<Number> iterable) {
            for (Number s : iterable) {
                add(s);
            }
            return this;
        }

        public JsonArrayBuilder addBooleans(Iterable<Boolean> iterable) {
            for (Boolean s : iterable) {
                add(s);
            }
            return this;
        }

        public JsonArrayBuilder addCharacters(Iterable<Character> iterable) {
            for (Character s : iterable) {
                add(s);
            }
            return this;
        }

        public JsonArray build() {
            return handle;
        }
    }

    private JsonBuilder() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
