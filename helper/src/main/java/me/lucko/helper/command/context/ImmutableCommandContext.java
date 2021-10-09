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
import me.lucko.helper.command.argument.SimpleArgument;

import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ImmutableCommandContext<T extends CommandSender> implements CommandContext<T> {
    private final T sender;
    private final String label;
    private final ImmutableList<String> args;
    private final ImmutableList<String> aliases;

    public ImmutableCommandContext(T sender, String label, String[] args, List<String> aliases) {
        this.sender = sender;
        this.label = label;
        this.args = ImmutableList.copyOf(args);
        this.aliases = ImmutableList.copyOf(aliases);
    }

    @Nonnull
    @Override
    public T sender() {
        return this.sender;
    }

    @Nonnull
    @Override
    public ImmutableList<String> args() {
        return this.args;
    }

    @Nonnull
    @Override
    public Argument arg(int index) {
        return new SimpleArgument(index, rawArg(index));
    }

    @Nullable
    @Override
    public String rawArg(int index) {
        if (index < 0 || index >= this.args.size()) {
            return null;
        }
        return this.args.get(index);
    }

    @Nonnull
    @Override
    public String label() {
        return this.label;
    }

    @Nonnull
    @Override
    public ImmutableList<String> aliases() {
        return this.aliases;
    }
}
