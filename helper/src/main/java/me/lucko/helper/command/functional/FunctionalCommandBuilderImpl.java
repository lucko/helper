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

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import me.lucko.helper.command.Command;
import me.lucko.helper.command.context.CommandContext;
import me.lucko.helper.utils.Color;
import me.lucko.helper.utils.Cooldown;
import me.lucko.helper.utils.CooldownCollection;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@NonnullByDefault
class FunctionalCommandBuilderImpl<T extends CommandSender> implements FunctionalCommandBuilder<T> {
    private final ImmutableList.Builder<Predicate<CommandContext<?>>> predicates;

    private FunctionalCommandBuilderImpl(ImmutableList.Builder<Predicate<CommandContext<?>>> predicates) {
        this.predicates = predicates;
    }

    FunctionalCommandBuilderImpl() {
        this(ImmutableList.builder());
    }

    @Override
    public FunctionalCommandBuilder<T> assertPermission(String permission, String failureMessage) {
        Preconditions.checkNotNull(permission, "permission");
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            if (context.sender().hasPermission(permission)) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage));
            return false;
        });
        return this;
    }

    @Override
    public FunctionalCommandBuilder<T> assertOp(String failureMessage) {
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            if (context.sender().isOp()) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage));
            return false;
        });
        return this;
    }

    @Override
    public FunctionalCommandBuilder<Player> assertPlayer(String failureMessage) {
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            if (context.sender() instanceof Player) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage));
            return false;
        });
        // cast the generic type
        return new FunctionalCommandBuilderImpl<>(predicates);
    }

    @Override
    public FunctionalCommandBuilder<ConsoleCommandSender> assertConsole(String failureMessage) {
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            if (context.sender() instanceof ConsoleCommandSender) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage));
            return false;
        });
        // cast the generic type
        return new FunctionalCommandBuilderImpl<>(predicates);
    }

    @Override
    public FunctionalCommandBuilder<T> assertUsage(String usage, String failureMessage) {
        Preconditions.checkNotNull(usage, "usage");
        Preconditions.checkNotNull(failureMessage, "failureMessage");

        List<String> usageParts = Splitter.on(" ").splitToList(usage);

        int requiredArgs = 0;
        for (String usagePart : usageParts) {
            if (!usagePart.startsWith("[") && !usagePart.endsWith("]")) {
                // assume it's a required argument
                requiredArgs++;
            }
        }

        int finalRequiredArgs = requiredArgs;
        predicates.add(context -> {
            if (context.args().size() >= finalRequiredArgs) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage.replace("{usage}", "/" + context.label() + " " + usage)));
            return false;
        });

        return this;
    }

    @Override
    public FunctionalCommandBuilder<T> assertArgument(int index, Predicate<String> test, String failureMessage) {
        Preconditions.checkNotNull(test, "test");
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            String arg = context.rawArg(index);
            if (test.test(arg)) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage.replace("{arg}", arg).replace("{index}", Integer.toString(index))));
            return false;
        });
        return this;
    }

    @Override
    public FunctionalCommandBuilder<T> assertSender(Predicate<T> test, String failureMessage) {
        Preconditions.checkNotNull(test, "test");
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            //noinspection unchecked
            T sender = (T) context.sender();
            if (test.test(sender)) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage));
            return false;
        });
        return this;
    }

    @Override
    public FunctionalCommandBuilder<T> withCooldown(Cooldown cooldown, String failureMessage) {
        Preconditions.checkNotNull(cooldown, "cooldown");
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            if (cooldown.test()) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage.replace("{seconds}", Long.toString(cooldown.remainingTime(TimeUnit.SECONDS)))));
            return false;
        });
        return this;
    }

    @Override
    public FunctionalCommandBuilder<T> withCooldown(CooldownCollection<T> cooldown, String failureMessage) {
        Preconditions.checkNotNull(cooldown, "cooldown");
        Preconditions.checkNotNull(failureMessage, "failureMessage");
        predicates.add(context -> {
            //noinspection unchecked
            T sender = (T) context.sender();
            if (cooldown.test(sender)) {
                return true;
            }

            context.sender().sendMessage(Color.colorize(failureMessage.replace("{seconds}", Long.toString(cooldown.remainingTime(sender, TimeUnit.SECONDS)))));
            return false;
        });
        return this;
    }

    @Override
    public Command handler(FunctionalCommandHandler handler) {
        Preconditions.checkNotNull(handler, "handler");
        return new FunctionalCommand(predicates.build(), handler);
    }
}
