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

import com.google.common.reflect.TypeToken;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nonnull;

/**
 * A collection of {@link ArgumentParser}s
 */
public interface ArgumentParserRegistry {

    /**
     * Tries to find an argument parser for the given type
     *
     * @param type the argument type
     * @param <T> the type
     * @return an argument, if one was found
     */
    @Nonnull
    <T> Optional<ArgumentParser<T>> find(@Nonnull TypeToken<T> type);

    /**
     * Tries to find an argument parser for the given class
     *
     * @param clazz the argument class
     * @param <T> the class type
     * @return an argument, if one was found
     */
    @Nonnull
    default <T> Optional<ArgumentParser<T>> find(@Nonnull Class<T> clazz) {
        return find(TypeToken.of(clazz));
    }

    /**
     * Finds all known parsers for a given type
     *
     * @param type the argument type
     * @param <T> the type
     * @return a collection of argument parsers
     */
    @Nonnull
    <T> Collection<ArgumentParser<T>> findAll(@Nonnull TypeToken<T> type);

    /**
     * Finds all known parsers for a given class
     *
     * @param clazz the argument class
     * @param <T> the class type
     * @return a collection of argument parsers
     */
    @Nonnull
    default <T> Collection<ArgumentParser<T>> findAll(@Nonnull Class<T> clazz) {
        return findAll(TypeToken.of(clazz));
    }

    /**
     * Registers a new parser with the registry
     *
     * @param type the argument type
     * @param parser the parser
     * @param <T> the type
     */
    <T> void register(@Nonnull TypeToken<T> type, @Nonnull ArgumentParser<T> parser);

    /**
     * Registers a new parser with the registry
     *
     * @param clazz the argument class
     * @param parser the parser
     * @param <T> the class type
     */
    default <T> void register(@Nonnull Class<T> clazz, @Nonnull ArgumentParser<T> parser) {
        register(TypeToken.of(clazz), parser);
    }
}
