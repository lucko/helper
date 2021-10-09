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
import me.lucko.helper.command.context.ImmutableCommandContext;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.utils.CommandMapUtil;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nullable;
import java.util.List;

/**
 * An abstract implementation of {@link Command} and {@link CommandExecutor}
 */
@NonnullByDefault
public abstract class AbstractCommand implements Command, CommandExecutor, TabCompleter {

    protected @Nullable String permission;
    protected @Nullable String permissionMessage;
    protected @Nullable String description;

    @Override
    public void register(String... aliases) {
        LoaderUtils.getPlugin().registerCommand(this, permission, permissionMessage, description, aliases);
    }

    @Override
    public void close() {
        CommandMapUtil.unregisterCommand(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        CommandContext<CommandSender> context = new ImmutableCommandContext<>(sender, label, args, command.getAliases());
        try {
            call(context);
        } catch (CommandInterruptException e) {
            e.getAction().accept(context.sender());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        CommandContext<CommandSender> context = new ImmutableCommandContext<>(sender, label, args, command.getAliases());
        try {
            return callTabCompleter(context);
        } catch (CommandInterruptException e) {
            e.getAction().accept(context.sender());
        }
        return null;
    }
}
