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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import me.lucko.helper.datatree.DataTree;
import me.lucko.helper.gson.typeadapters.BukkitSerializableAdapterFactory;
import me.lucko.helper.gson.typeadapters.GsonSerializableAdapterFactory;
import me.lucko.helper.gson.typeadapters.JsonElementTreeSerializer;

import net.kyori.text.serializer.gson.GsonComponentSerializer;

import java.io.Reader;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Provides static instances of Gson
 */
public final class GsonProvider {

    private static final Gson STANDARD_GSON = GsonComponentSerializer.populate(new GsonBuilder())
            .registerTypeHierarchyAdapter(DataTree.class, JsonElementTreeSerializer.INSTANCE)
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BukkitSerializableAdapterFactory.INSTANCE)
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    private static final Gson PRETTY_PRINT_GSON = GsonComponentSerializer.populate(new GsonBuilder())
            .registerTypeHierarchyAdapter(DataTree.class, JsonElementTreeSerializer.INSTANCE)
            .registerTypeAdapterFactory(GsonSerializableAdapterFactory.INSTANCE)
            .registerTypeAdapterFactory(BukkitSerializableAdapterFactory.INSTANCE)
            .serializeNulls()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    private static final JsonParser PARSER = new JsonParser();

    @Nonnull
    public static Gson standard() {
        return STANDARD_GSON;
    }

    @Nonnull
    public static Gson prettyPrinting() {
        return PRETTY_PRINT_GSON;
    }

    @Nonnull
    public static JsonParser parser() {
        return PARSER;
    }

    @Nonnull
    public static JsonObject readObject(@Nonnull Reader reader) {
        return PARSER.parse(reader).getAsJsonObject();
    }

    @Nonnull
    public static JsonObject readObject(@Nonnull String s) {
        return PARSER.parse(s).getAsJsonObject();
    }

    public static void writeObject(@Nonnull Appendable writer, @Nonnull JsonObject object) {
        standard().toJson(object, writer);
    }

    public static void writeObjectPretty(@Nonnull Appendable writer, @Nonnull JsonObject object) {
        prettyPrinting().toJson(object, writer);
    }

    public static void writeElement(@Nonnull Appendable writer, @Nonnull JsonElement element) {
        standard().toJson(element, writer);
    }

    public static void writeElementPretty(@Nonnull Appendable writer, @Nonnull JsonElement element) {
        prettyPrinting().toJson(element, writer);
    }

    @Nonnull
    public static String toString(@Nonnull JsonElement element) {
        return Objects.requireNonNull(standard().toJson(element));
    }

    @Nonnull
    public static String toStringPretty(@Nonnull JsonElement element) {
        return Objects.requireNonNull(prettyPrinting().toJson(element));
    }

    private GsonProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    @Nonnull
    @Deprecated
    public static Gson get() {
        return standard();
    }

    @Nonnull
    @Deprecated
    public static Gson getPrettyPrinting() {
        return prettyPrinting();
    }

}
