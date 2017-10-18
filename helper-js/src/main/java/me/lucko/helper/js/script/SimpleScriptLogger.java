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

package me.lucko.helper.js.script;

import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleScriptLogger implements ScriptLogger {

    private final Script script;

    public SimpleScriptLogger(@Nonnull Script script) {
        this.script = script;
    }

    @Override
    public void info(@Nullable Object... message) {
        getLogger().info("[" + script.getName() + "]" + format(message));
    }

    @Override
    public void warn(@Nullable Object... message) {
        getLogger().warning("[" + script.getName() + "]" + format(message));
    }

    @Override
    public void error(@Nullable Object... message) {
        getLogger().severe("[" + script.getName() + "]" + format(message));
    }

    private static String format(@Nullable Object[] message) {
        if (message == null || message.length == 0) {
            return " ";
        } else if (message.length == 1) {
            return " " + String.valueOf(message[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            for (Object o : message) {
                sb.append(" ").append(String.valueOf(o));
            }
            return sb.toString();
        }
    }

    private Logger getLogger() {
        return script.getBindings().getPlugin().getPluginLogger();
    }
}
