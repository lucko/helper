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
import me.lucko.helper.Commands;
import me.lucko.helper.command.CommandInterruptException;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Represents a command argument
 */
public interface Argument {

    /**
     * Gets the index of the argument
     *
     * @return the index
     */
    int index();

    /**
     * Gets the value of the argument
     *
     * @return the value
     */
    @Nonnull
    Optional<String> value();

    @Nonnull
    default <T> Optional<T> parse(@Nonnull ArgumentParser<T> parser) {
        return parser.parse(this);
    }

    @Nonnull
    default <T> T parseOrFail(@Nonnull ArgumentParser<T> parser) throws CommandInterruptException {
        return parser.parseOrFail(this);
    }

    @Nonnull
    default <T> Optional<T> parse(@Nonnull TypeToken<T> type) {
        return Commands.parserRegistry().find(type).flatMap(this::parse);
    }

    @Nonnull
    default <T> T parseOrFail(@Nonnull TypeToken<T> type) throws CommandInterruptException {
        ArgumentParser<T> parser = Commands.parserRegistry().find(type).orElse(null);
        if (parser == null) {
            throw new RuntimeException("Unable to find ArgumentParser for " + type);
        }
        return parseOrFail(parser);
    }

    @Nonnull
    default <T> Optional<T> parse(@Nonnull Class<T> clazz) {
        return Commands.parserRegistry().find(clazz).flatMap(this::parse);
    }

    @Nonnull
    default <T> T parseOrFail(@Nonnull Class<T> clazz) throws CommandInterruptException {
        ArgumentParser<T> parser = Commands.parserRegistry().find(clazz).orElse(null);
        if (parser == null) {
            throw new RuntimeException("Unable to find ArgumentParser for " + clazz);
        }
        return parseOrFail(parser);
    }

    /**
     * Gets if the argument is present
     *
     * @return true if present
     */
    boolean isPresent();

    /**
     * Asserts that the permission is present
     */
    default void assertPresent() throws CommandInterruptException {
        CommandInterruptException.makeAssertion(isPresent(), "&cArgument at index " + index() + " is not present.");
    }
}
