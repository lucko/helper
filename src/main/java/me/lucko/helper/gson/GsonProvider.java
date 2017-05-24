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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.Reader;

/**
 * Provides static instances of Gson
 */
public final class GsonProvider {

    private static final Gson STANDARD = new Gson();
    private static final Gson PRETTY_PRINT = new GsonBuilder().setPrettyPrinting().create();

    public static Gson get() {
        return STANDARD;
    }

    public static Gson getPrettyPrinting() {
        return PRETTY_PRINT;
    }

    public static JsonObject readObject(Reader reader) {
        return get().fromJson(reader, JsonObject.class);
    }

    public static JsonObject readObject(String s) {
        return get().fromJson(s, JsonObject.class);
    }

    public static void writeObject(Appendable writer, JsonObject data) {
        get().toJson(data, writer);
    }

    public static void writeObjectPretty(Appendable writer, JsonObject data) {
        getPrettyPrinting().toJson(data, writer);
    }

    private GsonProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
