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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class GsonSerializableAdapterFactory implements TypeAdapterFactory {
    public static final GsonSerializableAdapterFactory INSTANCE = new GsonSerializableAdapterFactory();

    private GsonSerializableAdapterFactory() {
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> clazz = type.getRawType();

        // we're only trying to provide adapters for classes which implement the helper GsonSerializable interface
        if (!GsonSerializable.class.isAssignableFrom(clazz)) {
            return null;
        }

        // make sure they also implement the static deserialize method
        Method deserializeMethod;
        try {
            deserializeMethod = clazz.getDeclaredMethod("deserialize", JsonElement.class);
        } catch (NoSuchMethodException e) {
            return null;
        }

        if (!Modifier.isStatic(deserializeMethod.getModifiers())) {
            return null;
        }

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter writer, T t) throws IOException {
                GsonSerializable serializable = (GsonSerializable) t;
                gson.toJson(serializable.serialize(), writer);
            }

            @Override
            public T read(JsonReader reader) throws IOException {
                JsonElement element = gson.fromJson(reader, JsonElement.class);

                try {
                    //noinspection unchecked
                    return (T) deserializeMethod.invoke(null, element);
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };

    }

}
