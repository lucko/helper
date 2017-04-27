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

package me.lucko.helper;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.utils.Color;
import me.lucko.helper.utils.Cooldown;
import me.lucko.helper.utils.CooldownCollection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * A functional command handling utility.
 */
public final class Commands {

    // Default failure messages
    private static final String DEFAULT_NO_PERMISSION_MESSAGE = "&cYou do not have permission to use this command.";
    private static final String DEFAULT_NOT_OP_MESSAGE = "&cOnly server operators are able to use this command.";
    private static final String DEFAULT_NOT_PLAYER_MESSAGE = "&cOnly players are able to use this command.";
    private static final String DEFAULT_NOT_CONSOLE_MESSAGE = "&cThis command is only available through the server console.";
    private static final String DEFAULT_INVALID_USAGE_MESSAGE = "&cInvalid usage. Try: {usage}.";
    private static final String DEFAULT_INVALID_ARGUMENT_MESSAGE = "&cInvalid argument '{arg}' at index {index}.";
    private static final String DEFAULT_INVALID_SENDER_MESSAGE = "&cYou are not able to use this command.";
    private static final String DEFAULT_ON_COOLDOWN_MESSAGE = "&cPlease wait {seconds} seconds before trying again.";

    /**
     * Creates and returns a new command builder
     *
     * @return a command builder
     */
    public static CommandBuilder<CommandSender> create() {
        return new CommandBuilderImpl<>();
    }

    /**
     * Builds a {@link FunctionalCommand}
     *
     * @param <T> the sender type
     */
    public interface CommandBuilder<T extends CommandSender> {

        /**
         * Asserts that the sender has the specified permission, and sends them the default failure message
         * if they don't have permission.
         *
         * @param permission the permission to check for
         * @return the builder instance
         */
        default CommandBuilder<T> assertPermission(String permission) {
            return assertPermission(permission, DEFAULT_NO_PERMISSION_MESSAGE);
        }

        /**
         * Asserts that the sender has the specified permission, and sends them the failure message if they
         * don't have permission.
         *
         * @param permission the permission to check for
         * @param failureMessage the failure message to send if they don't have permission
         * @return the builder instance
         */
        CommandBuilder<T> assertPermission(String permission, String failureMessage);

        /**
         * Asserts that the sender is op, and sends them the default failure message if they're not.
         *
         * @return the builder instance
         */
        default CommandBuilder<T> assertOp() {
            return assertOp(DEFAULT_NOT_OP_MESSAGE);
        }

        /**
         * Asserts that the sender is op, and sends them the failure message if they don't have permission
         *
         * @param failureMessage the failure message to send if they're not op
         * @return the builder instance
         */
        CommandBuilder<T> assertOp(String failureMessage);

        /**
         * Asserts that the sender is instance of Player, and sends them the default failure message if they're not.
         *
         * @return the builder instance
         */
        default CommandBuilder<Player> assertPlayer() {
            return assertPlayer(DEFAULT_NOT_PLAYER_MESSAGE);
        }

        /**
         * Asserts that the sender is instance of Player, and sends them the failure message if they're not
         *
         * @param failureMessage the failure message to send if they're not a player
         * @return the builder instance
         */
        CommandBuilder<Player> assertPlayer(String failureMessage);

        /**
         * Asserts that the sender is instance of ConsoleCommandSender, and sends them the default failure message if
         * they're not.
         *
         * @return the builder instance
         */
        default CommandBuilder<ConsoleCommandSender> assertConsole() {
            return assertConsole(DEFAULT_NOT_CONSOLE_MESSAGE);
        }

        /**
         * Asserts that the sender is instance of ConsoleCommandSender, and sends them the failure message if they're not
         *
         * @param failureMessage the failure message to send if they're not console
         * @return the builder instance
         */
        CommandBuilder<ConsoleCommandSender> assertConsole(String failureMessage);

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
        default CommandBuilder<T> assertUsage(String usage) {
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
        CommandBuilder<T> assertUsage(String usage, String failureMessage);

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
        default CommandBuilder<T> assertArgument(int index, Predicate<String> test) {
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
        CommandBuilder<T> assertArgument(int index, Predicate<String> test, String failureMessage);

        /**
         * Tests the sender with the provided predicate.
         *
         * The default failure message is sent if the sender does not pass the predicate.
         *
         * @param test the test predicate
         * @return the builder instance
         */
        default CommandBuilder<T> assertSender(Predicate<T> test) {
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
        CommandBuilder<T> assertSender(Predicate<T> test, String failureMessage);

        /**
         * Tests the command attempt against the given cooldown.
         *
         * The default failure message is sent if the command does not pass the cooldown.
         *
         * @param cooldown the cooldown
         * @return the builder instance
         */
        default CommandBuilder<T> withCooldown(Cooldown cooldown) {
            return withCooldown(cooldown, DEFAULT_ON_COOLDOWN_MESSAGE);
        }

        /**
         * Tests the command attempt against the given cooldown.
         *
         * The default failure message is sent if the command does not pass the cooldown. "{seconds}" is replaced in the
         * failure message by the number of seconds until the cooldown expires
         *
         * @param cooldown the cooldown
         * @param failureMessage the failure message to send if cooldown fails
         * @return the builder instance
         */
        CommandBuilder<T> withCooldown(Cooldown cooldown, String failureMessage);

        /**
         * Tests the command attempt against the given cooldown.
         *
         * The default failure message is sent if the command does not pass the cooldown.
         *
         * @param cooldown the cooldown
         * @return the builder instance
         */
        default CommandBuilder<T> withCooldown(CooldownCollection<T> cooldown) {
            return withCooldown(cooldown, DEFAULT_ON_COOLDOWN_MESSAGE);
        }

        /**
         * Tests the command attempt against the given cooldown.
         *
         * The default failure message is sent if the command does not pass the cooldown. "{seconds}" is replaced in the
         * failure message by the number of seconds until the cooldown expires
         *
         * @param cooldown the cooldown
         * @param failureMessage the failure message to send if cooldown fails
         * @return the builder instance
         */
        CommandBuilder<T> withCooldown(CooldownCollection<T> cooldown, String failureMessage);

        /**
         * Builds this {@link CommandBuilder} into a {@link FunctionalCommand} instance.
         *
         * The command will not be registered with the server until {@link FunctionalCommand#register(ExtendedJavaPlugin, String...)} is called.
         *
         * @param handler the command handler
         * @return the command instance.
         */
        FunctionalCommand handler(CommandHandler<T> handler);

    }

    /**
     * Represents a command built from functional predicate calls
     */
    public interface FunctionalCommand {

        /**
         * Registers this command with the server, via the given plugin instance
         *
         * @param plugin the plugin instance
         * @param aliases the aliases for the command
         */
        void register(ExtendedJavaPlugin plugin, String... aliases);

        /**
         * Calls the command handler
         *
         * @param context the contexts for the command
         */
        void handle(CommandContext<?> context);

    }

    /**
     * Represents a handler for a {@link FunctionalCommand}
     *
     * @param <T> the sender type
     */
    @FunctionalInterface
    public interface CommandHandler<T extends CommandSender> {

        /**
         * Executes the handler using the given command context
         * @param c the command context
         */
        void handle(CommandContext<T> c);

    }

    /**
     * Represents the context for a command call
     * @param <T> the sender type
     */
    public interface CommandContext<T extends CommandSender> {

        /**
         * Gets the sender who executed the command
         *
         * @return the sender who executed the command
         */
        T getSender();

        /**
         * Gets an immutable list of the supplied arguments
         *
         * @return an immutable list of the supplied arguments
         */
        ImmutableList<String> getArgs();

        /**
         * Gets the argument at the given index. Returns null if no argument is present at that index.
         *
         * @param index the index
         * @return the argument, or null if one was not present
         */
        String getArg(int index);

        /**
         * Returns a {@link Optional} containing the argument if present
         *
         * @param index the index
         * @return the argument
         */
        default Optional<String> getArgIfPresent(int index) {
            return Optional.ofNullable(getArg(index));
        }

        /**
         * Gets the command label which was used to execute this command
         *
         * @return the command label which was used to execute this command
         */
        String getLabel();

    }

    private static final class CommandBuilderImpl<T extends CommandSender> implements CommandBuilder<T> {
        private final ImmutableList.Builder<Predicate<CommandContext<?>>> predicates;

        private CommandBuilderImpl(ImmutableList.Builder<Predicate<CommandContext<?>>> predicates) {
            this.predicates = predicates;
        }

        private CommandBuilderImpl() {
            this(ImmutableList.builder());
        }

        @Override
        public CommandBuilder<T> assertPermission(String permission, String failureMessage) {
            Preconditions.checkNotNull(permission, "permission");
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                if (context.getSender().hasPermission(permission)) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage));
                return false;
            });
            return this;
        }

        @Override
        public CommandBuilder<T> assertOp(String failureMessage) {
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                if (context.getSender().isOp()) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage));
                return false;
            });
            return this;
        }

        @Override
        public CommandBuilder<Player> assertPlayer(String failureMessage) {
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                if (context.getSender() instanceof Player) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage));
                return false;
            });
            // cast the generic type
            return new CommandBuilderImpl<>(predicates);
        }

        @Override
        public CommandBuilder<ConsoleCommandSender> assertConsole(String failureMessage) {
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                if (context.getSender() instanceof ConsoleCommandSender) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage));
                return false;
            });
            // cast the generic type
            return new CommandBuilderImpl<>(predicates);
        }

        @Override
        public CommandBuilder<T> assertUsage(String usage, String failureMessage) {
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
                if (context.getArgs().size() >= finalRequiredArgs) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage.replace("{usage}", "/" + context.getLabel() + " " + usage)));
                return false;
            });

            return this;
        }

        @Override
        public CommandBuilder<T> assertArgument(int index, Predicate<String> test, String failureMessage) {
            Preconditions.checkNotNull(test, "test");
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                String arg = context.getArg(index);
                if (test.test(arg)) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage.replace("{arg}", arg).replace("{index}", Integer.toString(index))));
                return false;
            });
            return this;
        }

        @Override
        public CommandBuilder<T> assertSender(Predicate<T> test, String failureMessage) {
            Preconditions.checkNotNull(test, "test");
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                //noinspection unchecked
                T sender = (T) context.getSender();
                if (test.test(sender)) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage));
                return false;
            });
            return this;
        }

        @Override
        public CommandBuilder<T> withCooldown(Cooldown cooldown, String failureMessage) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                if (cooldown.test()) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage.replace("{seconds}", Long.toString(cooldown.remainingTime(TimeUnit.SECONDS)))));
                return false;
            });
            return this;
        }

        @Override
        public CommandBuilder<T> withCooldown(CooldownCollection<T> cooldown, String failureMessage) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                //noinspection unchecked
                T sender = (T) context.getSender();
                if (cooldown.test(sender)) {
                    return true;
                }

                context.getSender().sendMessage(Color.colorize(failureMessage.replace("{seconds}", Long.toString(cooldown.remainingTime(sender, TimeUnit.SECONDS)))));
                return false;
            });
            return this;
        }

        @Override
        public FunctionalCommand handler(CommandHandler<T> handler) {
            Preconditions.checkNotNull(handler, "handler");
            return new FunctionalCommandImpl(predicates.build(), handler);
        }

    }

    private static final class FunctionalCommandImpl implements FunctionalCommand, CommandExecutor {
        private final ImmutableList<Predicate<CommandContext<?>>> predicates;
        private final CommandHandler handler;

        private FunctionalCommandImpl(ImmutableList<Predicate<CommandContext<?>>> predicates, CommandHandler handler) {
            this.predicates = predicates;
            this.handler = handler;
        }

        @Override
        public void handle(CommandContext<?> context) {
            for (Predicate<CommandContext<?>> predicate : predicates) {
                if (!predicate.test(context)) {
                    return;
                }
            }

            //noinspection unchecked
            handler.handle(context);
        }

        @Override
        public void register(ExtendedJavaPlugin plugin, String... aliases) {
            plugin.registerCommand(this, aliases);
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            CommandContext<CommandSender> context = new ImmutableCommandContext<>(sender, label, args);
            handle(context);
            return true;
        }
    }

    private static final class ImmutableCommandContext<T extends CommandSender> implements CommandContext<T> {

        private final T sender;
        private final String label;
        private final ImmutableList<String> args;

        ImmutableCommandContext(T sender, String label, String[] args) {
            this.sender = sender;
            this.label = label;
            this.args = ImmutableList.copyOf(args);
        }

        @Override
        public T getSender() {
            return sender;
        }

        @Override
        public ImmutableList<String> getArgs() {
            return args;
        }

        @Override
        public String getArg(int index) {
            if (index < 0 || index >= args.size()) {
                return null;
            }
            return args.get(index);
        }

        @Override
        public String getLabel() {
            return label;
        }
    }

    private Commands() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
