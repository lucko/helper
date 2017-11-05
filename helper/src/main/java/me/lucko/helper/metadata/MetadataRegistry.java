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

package me.lucko.helper.metadata;

import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * A registry which provides and stores {@link MetadataMap}s for a given type.
 *
 * @param <T> the type
 */
public interface MetadataRegistry<T> {

    /**
     * Produces a {@link MetadataMap} for the given object.
     *
     * @param id the object
     * @return a metadata map
     */
    @Nonnull
    MetadataMap provide(@Nonnull T id);

    /**
     * Gets a {@link MetadataMap} for the given object, if one already exists and has
     * been cached in this registry.
     *
     * @param id the object
     * @return a metadata map, if present
     */
    @Nonnull
    Optional<MetadataMap> get(@Nonnull T id);

    /**
     * Deletes the {@link MetadataMap} and all contained {@link MetadataKey}s for
     * the given object.
     *
     * @param id the object
     */
    void remove(@Nonnull T id);

    /**
     * Performs cache maintenance to remove empty map instances and expired transient values.
     */
    void cleanup();

}
