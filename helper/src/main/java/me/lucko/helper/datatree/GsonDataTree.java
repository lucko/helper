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

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

public class GsonDataTree implements DataTree {
    private final JsonElement element;

    public GsonDataTree(JsonElement element) {
        this.element = Objects.requireNonNull(element, "element");
    }

    public JsonElement getElement() {
        return this.element;
    }

    @Nonnull
    @Override
    public GsonDataTree resolve(@Nonnull Object... path) {
        if (path.length == 0) {
            return this;
        }

        JsonElement o = this.element;
        for (int i = 0; i < path.length; i++) {
            Object p = path[i];

            if (p instanceof String) {
                String memberName = (String) p;
                JsonObject obj = o.getAsJsonObject();
                if (!obj.has(memberName)) {
                    throw new IllegalArgumentException("Object " + obj + " does not have member: " + memberName);
                }
                o = obj.get(memberName);
            } else if (p instanceof Number) {
                o = o.getAsJsonArray().get(((Number) p).intValue());
            } else {
                throw new IllegalArgumentException("Unknown path node at index " + i + ": " + p);
            }
        }
        return new GsonDataTree(o);
    }

    @Nonnull
    @Override
    public Stream<Map.Entry<String, GsonDataTree>> asObject() {
        return this.element.getAsJsonObject().entrySet().stream()
                .map(entry -> Maps.immutableEntry(entry.getKey(), new GsonDataTree(entry.getValue())));
    }

    @Nonnull
    @Override
    public Stream<GsonDataTree> asArray() {
        return StreamSupport.stream(this.element.getAsJsonArray().spliterator(), false)
                .map(GsonDataTree::new);
    }

    @Nonnull
    @Override
    public Stream<Map.Entry<Integer, GsonDataTree>> asIndexedArray() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Map.Entry<Integer, GsonDataTree>>() {
            private final Iterator<JsonElement> iterator = GsonDataTree.this.element.getAsJsonArray().iterator();
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public Map.Entry<Integer, GsonDataTree> next() {
                return Maps.immutableEntry(this.index++, new GsonDataTree(this.iterator.next()));
            }
        }, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    @Nonnull
    @Override
    public String asString() {
        return this.element.getAsString();
    }

    @Nonnull
    @Override
    public Number asNumber() {
        return this.element.getAsNumber();
    }

    @Override
    public int asInt() {
        return this.element.getAsInt();
    }

    @Override
    public double asDouble() {
        return this.element.getAsDouble();
    }

    @Override
    public boolean asBoolean() {
        return this.element.getAsBoolean();
    }
}
