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

import me.lucko.helper.adventure.Text;

import org.bukkit.command.CommandSender;

import java.util.function.Consumer;

/**
 * Exception thrown when the handling of a command should be interrupted.
 *
 * <p>This exception is silently swallowed by the command processing handler.</p>
 */
public class CommandInterruptException extends Exception {

    /**
     * Makes an assertion about a condition.
     *
     * <p>When used inside a command, command processing will be gracefully halted
     * if the condition is not true.</p>
     *
     * @param condition the condition
     * @param failMsg the message to send to the player if the assertion fails
     * @throws CommandInterruptException if the assertion fails
     */
    public static void makeAssertion(boolean condition, String failMsg) throws CommandInterruptException {
        if (!condition) {
            throw new CommandInterruptException(failMsg);
        }
    }

    private final Consumer<CommandSender> action;

    public CommandInterruptException(Consumer<CommandSender> action) {
        this.action = action;
    }

    public CommandInterruptException(String message) {
        this.action = cs -> cs.sendMessage(Text.colorize(message));
    }

    public Consumer<CommandSender> getAction() {
        return this.action;
    }
}
