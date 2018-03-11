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

package me.lucko.helper.command;

import me.lucko.helper.command.context.CommandContext;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableConsumer;

import javax.annotation.Nonnull;

/**
 * Represents a command
 */
public interface Command extends Terminable {

    /**
     * Registers this command with the server, via the given plugin instance
     *
     * @param aliases the aliases for the command
     */
    void register(@Nonnull String... aliases);

    /**
     * Registers this command with the server, via the given plugin instance, and then binds it with the composite terminable.
     *
     * @param consumer the terminable consumer to bind with
     * @param aliases the aliases for the command
     */
    default void registerAndBind(@Nonnull TerminableConsumer consumer, @Nonnull String... aliases) {
        register(aliases);
        bindWith(consumer);
    }

    /**
     * Calls the command handler
     *
     * @param context the contexts for the command
     */
    void call(@Nonnull CommandContext<?> context) throws CommandInterruptException;

}
