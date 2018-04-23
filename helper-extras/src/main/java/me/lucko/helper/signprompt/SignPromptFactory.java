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

package me.lucko.helper.signprompt;

import org.bukkit.entity.Player;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Represents an object which can accept input from players using signs.
 */
public interface SignPromptFactory {

    /**
     * Opens a sign prompt.
     *
     * @param player the player to open the prompt for
     * @param lines the lines to fill the sign with initially
     * @param responseHandler the response handler.
     */
    void openPrompt(@Nonnull Player player, @Nonnull List<String> lines, @Nonnull ResponseHandler responseHandler);

    /**
     * Functional interface for handling responses to an active sign prompt.
     */
    @FunctionalInterface
    interface ResponseHandler {

        /**
         * Handles the response
         *
         * @param lines the response content
         * @return the response
         */
        @Nonnull
        Response handleResponse(@Nonnull List<String> lines);

    }

    /**
     * Encapsulates a response to the players input.
     */
    enum Response {

        /**
         * Marks that the response was accepted
         */
        ACCEPTED,

        /**
         * Marks that the response was not accepted, the player will be prompted
         * for another input.
         */
        TRY_AGAIN

    }

}
