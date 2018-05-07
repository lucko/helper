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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.Types;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;

public class ConfigurateDataTree implements DataTree {
    private final ConfigurationNode node;

    public ConfigurateDataTree(ConfigurationNode node) {
        this.node = Objects.requireNonNull(node, "node");
    }

    public ConfigurationNode getNode() {
        return this.node;
    }

    @Nonnull
    @Override
    public ConfigurateDataTree resolve(@Nonnull Object... path) {
        return new ConfigurateDataTree(this.node.getNode(path));
    }

    @Nonnull
    @Override
    public Stream<Map.Entry<String, ConfigurateDataTree>> asObject() {
        Preconditions.checkState(this.node.hasMapChildren(), "node does not have map children");
        return this.node.getChildrenMap().entrySet().stream()
                .map(entry -> Maps.immutableEntry(entry.getKey().toString(), new ConfigurateDataTree(entry.getValue())));
    }

    @Nonnull
    @Override
    public Stream<ConfigurateDataTree> asArray() {
        Preconditions.checkState(this.node.hasListChildren(), "node does not have list children");
        return this.node.getChildrenList().stream().map(ConfigurateDataTree::new);
    }

    @Nonnull
    @Override
    public Stream<Map.Entry<Integer, ConfigurateDataTree>> asIndexedArray() {
        Preconditions.checkState(this.node.hasListChildren(), "node does not have list children");
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Map.Entry<Integer, ConfigurateDataTree>>() {
            private final Iterator<? extends ConfigurationNode> iterator = ConfigurateDataTree.this.node.getChildrenList().iterator();
            private int index = 0;

            @Override
            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            @Override
            public Map.Entry<Integer, ConfigurateDataTree> next() {
                return Maps.immutableEntry(this.index++, new ConfigurateDataTree(this.iterator.next()));
            }
        }, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);
    }

    @Nonnull
    @Override
    public String asString() {
        return this.node.getValue(Types::asString);
    }

    @Nonnull
    @Override
    public Number asNumber() {
        return this.node.getValue(Types::asDouble);
    }

    @Override
    public int asInt() {
        return this.node.getValue(Types::asInt);
    }

    @Override
    public double asDouble() {
        return this.node.getValue(Types::asDouble);
    }

    @Override
    public boolean asBoolean() {
        return this.node.getValue(Types::asBoolean);
    }
}
