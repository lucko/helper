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

package me.lucko.helper.datatree;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ninja.leaping.configurate.ConfigurationNode;

import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * An easier way of parsing in-memory data structures.
 *
 * <p>Given the following example data:</p>
 * <pre>
 * {
 *   "some-object": {
 *     "some-nested-object": {
 *       "a-string": "some special string",
 *       "a-boolean": true,
 *       "some-numbers": [4, 5, 7]
 *        }
 *   }
 * }
 * </pre>
 *
 * <p>Various elements could be parsed as:</p>
 * <p></p>
 * <ul>
 *     <li><code>DataTree.from(root).resolve("some-object", "some-nested-object", "some-numbers", 2).asInt()</code>
 *     would result in the value <code>5</code>.</li>
 *     <li><code>DataTree.from(root).resolve("some-object", "some-nested-object").map(Map.Entry::getKey).toArray(String[]::new)</code>
 *     would result in <code>["a-string", "a-boolean", "some-numbers"]</code></li>
 * </ul>
 *
 * <p>Methods always return a value, throwing an exception if a value isn't present or appropriate.</p>
 */
public interface DataTree {

    /**
     * Creates a new {@link DataTree} from a {@link JsonElement}.
     *
     * @param element the element
     * @return a tree
     */
    @Nonnull
    static GsonDataTree from(@Nonnull JsonElement element) {
        return new GsonDataTree(element);
    }

    /**
     * Creates a new {@link DataTree} from a {@link ConfigurationNode}.
     *
     * @param node the node
     * @return a tree
     */
    @Nonnull
    static ConfigurateDataTree from(@Nonnull ConfigurationNode node) {
        return new ConfigurateDataTree(node);
    }

    /**
     * Resolves the given path.
     *
     * <p>Paths can be made up of {@link String} or {@link Integer} components. Strings are used to
     * resolve a member of an object, and integers resolve a member of an array.</p>
     *
     * @param path the path
     * @return the resultant tree node
     */
    @Nonnull
    DataTree resolve(@Nonnull Object... path);

    /**
     * Gets a stream of the member nodes, as if this tree was a {@link JsonObject}.
     *
     * @return the members
     */
    @Nonnull
    Stream<? extends Map.Entry<String, ? extends DataTree>> asObject();

    /**
     * Gets a stream of the member nodes, as if this tree was a {@link JsonArray}.
     *
     * @return the members
     */
    @Nonnull
    Stream<? extends DataTree> asArray();

    /**
     * Gets an indexed stream of the member nodes, as if this tree was a {@link JsonArray}.
     *
     * @return the members
     */
    @Nonnull
    Stream<? extends Map.Entry<Integer, ? extends DataTree>> asIndexedArray();

    /**
     * Returns a {@link String} representation of this node.
     *
     * @return this as a string
     */
    @Nonnull
    String asString();

    /**
     * Returns a {@link Number} representation of this node.
     *
     * @return this as a number
     */
    @Nonnull
    Number asNumber();

    /**
     * Returns an {@link Integer} representation of this node.
     *
     * @return this as an integer
     */
    int asInt();

    /**
     * Returns a {@link Double} representation of this node.
     *
     * @return this as a double
     */
    double asDouble();

    /**
     * Returns a {@link Boolean} representation of this node.
     *
     * @return this as a boolean
     */
    boolean asBoolean();

}
