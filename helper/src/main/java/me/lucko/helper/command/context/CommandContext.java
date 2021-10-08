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

package me.lucko.helper.command.context;

import com.google.common.collect.ImmutableList;

import me.lucko.helper.command.argument.Argument;
import me.lucko.helper.utils.Players;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the context for a given command execution
 *
 * @param <T> the sender type
 */
public interface CommandContext<T extends CommandSender> {

    /**
     * Gets the sender who executed the command
     *
     * @return the sender who executed the command
     */
    @Nonnull
    T sender();

    /**
     * Sends a message to the {@link #sender()}.
     *
     * @param message the message to send
     */
    default void reply(String... message) {
        Players.msg(sender(), message);
    }

    /**
     * Gets an immutable list of the supplied arguments
     *
     * @return an immutable list of the supplied arguments
     */
    @Nonnull
    ImmutableList<String> args();

    /**
     * Gets the argument at a the given index
     *
     * @param index the index
     * @return the argument
     */
    @Nonnull
    Argument arg(int index);

    /**
     * Gets the argument at the given index.
     * Returns null if no argument is present at that index.
     *
     * @param index the index
     * @return the argument, or null if one was not present
     */
    @Nullable
    String rawArg(int index);

    /**
     * Gets the command label which was used to execute this command
     *
     * @return the command label which was used to execute this command
     */
    @Nonnull
    String label();

    /**
     * Gets the aliases of the command.
     *
     * @return the aliases of the command
     */
    @Nonnull
    List<String> aliases();
}
