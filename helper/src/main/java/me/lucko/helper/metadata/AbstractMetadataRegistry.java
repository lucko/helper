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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * A basic implementation of {@link MetadataRegistry} using a LoadingCache.
 *
 * @param <T> the type
 */
public class AbstractMetadataRegistry<T> implements MetadataRegistry<T> {

    private static final CacheLoader<?, MetadataMap> LOADER = new Loader<>();
    private static <T> CacheLoader<T, MetadataMap> getLoader() {
        //noinspection unchecked
        return (CacheLoader) LOADER;
    }

    @Nonnull
    protected final LoadingCache<T, MetadataMap> cache = CacheBuilder.newBuilder().build(getLoader());

    @Nonnull
    @Override
    public MetadataMap provide(@Nonnull T id) {
        Objects.requireNonNull(id, "id");
        return this.cache.getUnchecked(id);
    }

    @Nonnull
    @Override
    public Optional<MetadataMap> get(@Nonnull T id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(this.cache.getIfPresent(id));
    }

    @Override
    public void remove(@Nonnull T id) {
        MetadataMap map = this.cache.asMap().remove(id);
        if (map != null) {
            map.clear();
        }
    }

    @Override
    public void cleanup() {
        // MetadataMap#isEmpty also removes expired values
        this.cache.asMap().values().removeIf(MetadataMap::isEmpty);
    }

    private static final class Loader<T> extends CacheLoader<T, MetadataMap> {
        @Override
        public MetadataMap load(@Nonnull T key) {
            return MetadataMap.create();
        }
    }
}
