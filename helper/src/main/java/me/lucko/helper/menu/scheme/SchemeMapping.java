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

package me.lucko.helper.menu.scheme;

import me.lucko.helper.menu.Item;

import java.util.Optional;

/**
 * Represents a mapping to be used in a {@link MenuScheme}
 */
public interface SchemeMapping {

    /**
     * Gets an item from the mapping which represents the given key.
     *
     * @param key the mapping key
     * @return an item if present, otherwise an empty optional
     */
    default Optional<Item> get(int key) {
        return Optional.ofNullable(getNullable(key));
    }

    /**
     * Gets an item from the mapping which represents the given key.
     *
     * @param key the mapping key
     * @return an item if present, otherwise null
     */
    Item getNullable(int key);

    /**
     * Gets if this scheme has a mapping for a given key
     *
     * @param key the mapping key
     * @return true if the scheme has a mapping for the key
     */
    boolean hasMappingFor(int key);

    /**
     * Makes a copy of this scheme mapping.
     *
     * @return a copy of this mapping.
     */
    SchemeMapping copy();

}
