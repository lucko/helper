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

package me.lucko.helper.command.functional;

import me.lucko.helper.command.Command;
import me.lucko.helper.command.context.CommandContext;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * Functional builder API for {@link Command}
 *
 * @param <T> the sender type
 */
@NonnullByDefault
public interface FunctionalCommandBuilder<T extends CommandSender> {
    
    // Default failure messages
    String DEFAULT_NOT_OP_MESSAGE = "&cOnly server operators are able to use this command.";
    String DEFAULT_NOT_PLAYER_MESSAGE = "&cOnly players are able to use this command.";
    String DEFAULT_NOT_CONSOLE_MESSAGE = "&cThis command is only available through the server console.";
    String DEFAULT_INVALID_USAGE_MESSAGE = "&cInvalid usage. Try: {usage}.";
    String DEFAULT_INVALID_ARGUMENT_MESSAGE = "&cInvalid argument '{arg}' at index {index}.";
    String DEFAULT_INVALID_SENDER_MESSAGE = "&cYou are not able to use this command.";

    static FunctionalCommandBuilder<CommandSender> newBuilder() {
        return new FunctionalCommandBuilderImpl<>();
    }

    /**
     * Sets the command description to the specified one.
     *
     * @param description the command description
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> description(String description);

    /**
     * Asserts that some function returns true.
     *
     * @param test the test to run
     * @return the builder instance
     */
    default FunctionalCommandBuilder<T> assertFunction(Predicate<? super CommandContext<? extends T>> test) {
        return assertFunction(test, null);
    }

    /**
     * Asserts that some function returns true.
     *
     * @param test the test to run
     * @param failureMessage the failure message if the test fails
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> assertFunction(Predicate<? super CommandContext<? extends T>> test, @Nullable String failureMessage);

    /**
     * Asserts that the sender has the specified permission, and sends them the default failure message
     * if they don't have permission.
     *
     * @param permission the permission to check for
     * @return the builder instance
     */
    default FunctionalCommandBuilder<T> assertPermission(String permission) {
        return assertPermission(permission, null);
    }

    /**
     * Asserts that the sender has the specified permission, and sends them the failure message if they
     * don't have permission.
     *
     * @param permission the permission to check for
     * @param failureMessage the failure message to send if they don't have permission
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> assertPermission(String permission, @Nullable String failureMessage);

    /**
     * Asserts that the sender is op, and sends them the default failure message if they're not.
     *
     * @return the builder instance
     */
    default FunctionalCommandBuilder<T> assertOp() {
        return assertOp(DEFAULT_NOT_OP_MESSAGE);
    }

    /**
     * Asserts that the sender is op, and sends them the failure message if they don't have permission
     *
     * @param failureMessage the failure message to send if they're not op
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> assertOp(String failureMessage);

    /**
     * Asserts that the sender is instance of Player, and sends them the default failure message if they're not.
     *
     * @return the builder instance
     */
    default FunctionalCommandBuilder<Player> assertPlayer() {
        return assertPlayer(DEFAULT_NOT_PLAYER_MESSAGE);
    }

    /**
     * Asserts that the sender is instance of Player, and sends them the failure message if they're not
     *
     * @param failureMessage the failure message to send if they're not a player
     * @return the builder instance
     */
    FunctionalCommandBuilder<Player> assertPlayer(String failureMessage);

    /**
     * Asserts that the sender is instance of ConsoleCommandSender, and sends them the default failure message if
     * they're not.
     *
     * @return the builder instance
     */
    default FunctionalCommandBuilder<ConsoleCommandSender> assertConsole() {
        return assertConsole(DEFAULT_NOT_CONSOLE_MESSAGE);
    }

    /**
     * Asserts that the sender is instance of ConsoleCommandSender, and sends them the failure message if they're not
     *
     * @param failureMessage the failure message to send if they're not console
     * @return the builder instance
     */
    FunctionalCommandBuilder<ConsoleCommandSender> assertConsole(String failureMessage);

    /**
     * Asserts that the arguments match the given usage string.
     *
     * Arguments should be separated by a " " space. Optional arguments are denoted by wrapping the argument name in
     * square quotes "[ ]"
     *
     * The default failure message is sent if they didn't provide enough arguments.
     *
     * @param usage the usage string
     * @return the builder instance
     */
    default FunctionalCommandBuilder<T> assertUsage(String usage) {
        return assertUsage(usage, DEFAULT_INVALID_USAGE_MESSAGE);
    }

    /**
     * Asserts that the arguments match the given usage string.
     *
     * Arguments should be separated by a " " space. Optional arguments are denoted by wrapping the argument name in
     * square quotes "[ ]"
     *
     * The failure message is sent if they didn't provide enough arguments. "{usage}" in this message will be replaced by
     * the usage for the command.
     *
     * @param usage the usage string
     * @param failureMessage the failure message to send if the arguments to not match the usage
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> assertUsage(String usage, String failureMessage);

    /**
     * Tests a given argument with the provided predicate.
     *
     * The default failure message is sent if the argument does not pass the predicate. If the argument is not
     * present at the given index, <code>null</code> is passed to the predicate.
     *
     * @param index the index of the argument to test
     * @param test  the test predicate
     * @return the builder instance
     */
    default FunctionalCommandBuilder<T> assertArgument(int index, Predicate<String> test) {
        return assertArgument(index, test, DEFAULT_INVALID_ARGUMENT_MESSAGE);
    }

    /**
     * Tests a given argument with the provided predicate.
     *
     * The failure message is sent if the argument does not pass the predicate. If the argument is not present at the
     * given index, <code>null</code> is passed to the predicate.
     *
     * "{arg}" and "{index}" will be replaced in the failure message with the index and actual argument value respectively.
     *
     * @param index the index of the argument to test
     * @param test the test predicate
     * @param failureMessage the failure message to send if the predicate fails
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> assertArgument(int index, Predicate<String> test, String failureMessage);

    /**
     * Tests the sender with the provided predicate.
     *
     * The default failure message is sent if the sender does not pass the predicate.
     *
     * @param test the test predicate
     * @return the builder instance
     */
    default FunctionalCommandBuilder<T> assertSender(Predicate<T> test) {
        return assertSender(test, DEFAULT_INVALID_SENDER_MESSAGE);
    }

    /**
     * Tests the sender with the provided predicate.
     *
     * The failure message is sent if the sender does not pass the predicate.
     *
     * @param test the test predicate
     * @param failureMessage the failure message to send if the predicate fails
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> assertSender(Predicate<T> test, String failureMessage);

    /**
     * Sets the tab handler to the provided one.
     *
     * @param tabHandler the tab handler
     * @return the builder instance
     */
    FunctionalCommandBuilder<T> tabHandler(FunctionalTabHandler<T> tabHandler);

    /**
     * Builds this {@link FunctionalCommandBuilder} into a {@link Command} instance.
     *
     * The command will not be registered with the server until {@link Command#register(String...)} is called.
     *
     * @param handler the command handler
     * @return the command instance.
     */
    Command handler(FunctionalCommandHandler<T> handler);
}
