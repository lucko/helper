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

import com.google.common.reflect.TypeToken;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Objects;

@NonnullByDefault
final class MetadataKeyImpl<T> implements MetadataKey<T> {

    private final String id;
    private final TypeToken<T> type;

    MetadataKeyImpl(String id, TypeToken<T> type) {
        this.id = id.toLowerCase();
        this.type = type;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public TypeToken<T> getType() {
        return this.type;
    }

    @Override
    public T cast(Object object) throws ClassCastException {
        Objects.requireNonNull(object, "object");
        //noinspection unchecked
        return (T) this.type.getRawType().cast(object);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetadataKeyImpl && ((MetadataKeyImpl) obj).getId().equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
