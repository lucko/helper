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

package me.lucko.helper.network.metadata;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

import me.lucko.helper.gson.GsonProvider;

import java.util.Objects;

public final class ServerMetadata {

    public static ServerMetadata of(String key, JsonElement data) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(data, "data");
        return new ServerMetadata(key, data);
    }

    public static <T> ServerMetadata of(String key, T data, Class<T> type) {
        Objects.requireNonNull(type, "type");
        return of(key, data, TypeToken.of(type));
    }

    public static <T> ServerMetadata of(String key, T data, TypeToken<T> type) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(type, "type");

        JsonElement json = GsonProvider.standard().toJsonTree(data, type.getType());
        return new ServerMetadata(key, json);
    }

    private final String key;
    private final JsonElement data;

    private ServerMetadata(String key, JsonElement data) {
        this.key = key;
        this.data = data;
    }

    public String key() {
        return this.key;
    }

    public JsonElement data() {
        return this.data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerMetadata that = (ServerMetadata) o;
        return this.key.equals(that.key) &&
                this.data.equals(that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.data);
    }

    @Override
    public String toString() {
        return "ServerMetadata{key=" + this.key + ", data=" + this.data + '}';
    }
}
