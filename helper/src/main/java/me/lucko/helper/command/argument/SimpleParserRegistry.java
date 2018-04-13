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

package me.lucko.helper.command.argument;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

public class SimpleParserRegistry implements ArgumentParserRegistry {
    private final Map<TypeToken<?>, List<ArgumentParser<?>>> parsers = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    public <T> Optional<ArgumentParser<T>> find(@Nonnull TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        List<ArgumentParser<?>> parsers = this.parsers.get(type);
        if (parsers == null || parsers.isEmpty()) {
            return Optional.empty();
        }

        //noinspection unchecked
        return Optional.of((ArgumentParser<T>) parsers.get(0));
    }

    @Nonnull
    @Override
    public <T> Collection<ArgumentParser<T>> findAll(@Nonnull TypeToken<T> type) {
        Objects.requireNonNull(type, "type");
        List<ArgumentParser<?>> parsers = this.parsers.get(type);
        if (parsers == null || parsers.isEmpty()) {
            return ImmutableList.of();
        }

        //noinspection unchecked
        return (Collection) Collections.unmodifiableList(parsers);
    }

    @Override
    public <T> void register(@Nonnull TypeToken<T> type, @Nonnull ArgumentParser<T> parser) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(parser, "parser");
        List<ArgumentParser<?>> list = this.parsers.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>());
        if (!list.contains(parser)) {
            list.add(parser);
        }
    }
}
