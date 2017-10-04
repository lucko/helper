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

/**
 * A utility for converting between GSON's {@link JsonElement} family of classes, and standard Java types.
 *
 * <p>All conversions are deep, meaning for collections, contained values are also converted.</p>
 */
@NonnullByDefault
public interface GsonConverter {

    /**
     * Converts a {@link JsonObject} to a {@link Map}.
     *
     * @param object the json object
     * @return a new map
     */
    Map<String, Object> unwrapObject(JsonObject object);

    /**
     * Converts a {@link JsonArray} to a {@link List}.
     *
     * @param array the json array
     * @return a new list
     */
    List<Object> unwrapArray(JsonArray array);

    /**
     * Converts a {@link JsonArray} to a {@link Set}.
     *
     * @param array the json array
     * @return a new set
     */
    Set<Object> unwrapArrayToSet(JsonArray array);

    /**
     * Extracts the underlying {@link Object} from an {@link JsonPrimitive}.
     *
     * @param primitive the json primitive
     * @return the underlying object
     */
    Object unwarpPrimitive(JsonPrimitive primitive);

    /**
     * Converts a {@link JsonElement} to a {@link Object}.
     *
     * @param element the json element
     * @return the object
     */
    @Nullable
    Object unwrapElement(JsonElement element);

    /**
     * Tries to wrap an object to a {@link JsonElement}.
     *
     * <p>Supported types: {@link String}, {@link Number}, {@link Boolean},
     * {@link Character}, {@link Iterable}, and {@link Map}, where the key is a {@link String}.</p>
     *
     * @param object the object to wrap
     * @return the new json element
     */
    JsonElement wrap(Object object);

}
