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
import com.google.common.reflect.TypeToken;

import me.lucko.helper.function.Numbers;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.timings.Timings;
import me.lucko.helper.utils.Color;
import me.lucko.helper.utils.Cooldown;
import me.lucko.helper.utils.CooldownCollection;
import me.lucko.helper.utils.Players;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import co.aikar.timings.lib.MCTiming;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A functional command handling utility.
 */
@NonnullByDefault
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

    // Some default assertions
    public static final Predicate<String> ASSERTION_INTEGER = s -> {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    public static final Predicate<String> ASSERTION_DOUBLE = s -> {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    };

    public static final Predicate<String> ASSERTION_UUID = s -> {
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    };

    @SuppressWarnings("deprecation")
    public static final Predicate<String> ASSERTION_ONLINE_PLAYER = s -> Players.get(s).isPresent();

    @SuppressWarnings("deprecation")
    public static final Predicate<String> ASSERTION_OFFLINE_PLAYER = s -> Bukkit.getOfflinePlayer(s) != null;

    // Global argument parsers
    private static final ArgumentParserRegistry PARSER_REGISTRY;

    @Nonnull
    public static ArgumentParserRegistry parserRegistry() {
        return PARSER_REGISTRY;
    }

    static {
        PARSER_REGISTRY = new ArgumentRegistryImpl();

        // setup default argument parsers
        PARSER_REGISTRY.register(String.class, Optional::of);
        PARSER_REGISTRY.register(Number.class, Numbers::parse);
        PARSER_REGISTRY.register(Integer.class, Numbers::parseIntegerOpt);
        PARSER_REGISTRY.register(Long.class, Numbers::parseLongOpt);
        PARSER_REGISTRY.register(Float.class, Numbers::parseFloatOpt);
        PARSER_REGISTRY.register(Double.class, Numbers::parseDoubleOpt);
        PARSER_REGISTRY.register(Byte.class, Numbers::parseByteOpt);
        PARSER_REGISTRY.register(Boolean.class, s -> s.equalsIgnoreCase("true") ? Optional.of(true) : s.equalsIgnoreCase("false") ? Optional.of(false) : Optional.empty());
        PARSER_REGISTRY.register(UUID.class, s -> {
            try {
                return Optional.of(UUID.fromString(s));
            } catch (IllegalArgumentException e) {
                return Optional.empty();
            }
        });
        PARSER_REGISTRY.register(Player.class, s -> {
            try {
                return Players.get(UUID.fromString(s));
            } catch (IllegalArgumentException e) {
                return Players.get(s);
            }
        });
        PARSER_REGISTRY.register(OfflinePlayer.class, s -> {
            try {
                return Players.getOffline(UUID.fromString(s));
            } catch (IllegalArgumentException e) {
                return Players.getOffline(s);
            }
        });
        PARSER_REGISTRY.register(World.class, Helper::world);
    }

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
            throw new InformedCommandInterruptException(failMsg);
        }
    }

    /**
     * Makes an assertion about a condition.
     *
     * <p>When used inside a command, command processing will be gracefully halted
     * if the condition is not true.</p>
     *
     * @param condition the condition
     * @throws CommandInterruptException if the assertion fails
     */
    public static void makeAssertion(boolean condition) throws CommandInterruptException {
        if (!condition) {
            throw CommandInterruptException.INSTANCE;
        }
    }

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
    @NonnullByDefault
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
     * Exception thrown when the handling of a command should be interrupted.
     *
     * <p>This exception is silently swallowed by the command processing handler.</p>
     */
    public static class CommandInterruptException extends Exception {
        public static final CommandInterruptException INSTANCE = new CommandInterruptException();
    }

    @NonnullByDefault
    public static class InformedCommandInterruptException extends CommandInterruptException {
        private final Consumer<CommandSender> action;

        public InformedCommandInterruptException(Consumer<CommandSender> action) {
            this.action = action;
        }

        public InformedCommandInterruptException(String message) {
            this.action = cs -> cs.sendMessage(Color.colorize(message));
        }

        public Consumer<CommandSender> getAction() {
            return action;
        }
    }

    /**
     * Represents a command built from functional predicate calls
     */
    @NonnullByDefault
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
    @NonnullByDefault
    public interface CommandHandler<T extends CommandSender> {

        /**
         * Executes the handler using the given command context
         *
         * @param c the command context
         */
        void handle(CommandContext<T> c) throws CommandInterruptException;

    }

    /**
     * Represents the context for a command call
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
         * Gets the sender who executed the command
         *
         * @return the sender who executed the command
         * @deprecated in favour of {@link #sender()}
         */
        @Nonnull
        @Deprecated
        default T getSender() {
            return sender();
        }

        /**
         * Gets an immutable list of the supplied arguments
         *
         * @return an immutable list of the supplied arguments
         * @deprecated in favour of {@link #args()}
         */
        @Nonnull
        @Deprecated
        default ImmutableList<String> getArgs() {
            return args();
        }

        /**
         * Gets the argument at the given index. Returns null if no argument is present at that index.
         *
         * @param index the index
         * @return the argument, or null if one was not present
         * @deprecated in favour of {@link #rawArg(int)}
         */
        @Nullable
        @Deprecated
        default String getArg(int index) {
            return rawArg(index);
        }

        /**
         * Returns a {@link Optional} containing the argument if present
         *
         * @param index the index
         * @return the argument
         * @deprecated in favour of {@link #rawArg(int)}
         */
        @Nonnull
        @Deprecated
        default Optional<String> getArgIfPresent(int index) {
            return Optional.ofNullable(getArg(index));
        }

        /**
         * Gets the command label which was used to execute this command
         *
         * @return the command label which was used to execute this command
         * @deprecated in favour of {@link #label()}
         */
        @Nonnull
        @Deprecated
        default String getLabel() {
            return label();
        }

    }

    /**
     * Represents a command argument
     */
    public interface Argument {

        /**
         * Gets the index of the argument
         *
         * @return the index
         */
        int index();

        /**
         * Gets the value of the argument
         *
         * @return the value
         */
        @Nonnull
        Optional<String> value();

        @Nonnull
        default <T> Optional<T> parse(@Nonnull ArgumentParser<T> parser) {
            return parser.parse(this);
        }

        @Nonnull
        default <T> T parseOrFail(@Nonnull ArgumentParser<T> parser) throws CommandInterruptException {
            return parser.parseOrFail(this);
        }

        @Nonnull
        default <T> Optional<T> parse(@Nonnull TypeToken<T> type) {
            return parserRegistry().find(type).flatMap(this::parse);
        }

        @Nonnull
        default <T> T parseOrFail(@Nonnull TypeToken<T> type) throws CommandInterruptException {
            ArgumentParser<T> parser = parserRegistry().find(type).orElse(null);
            if (parser == null) {
                throw new RuntimeException("Unable to find ArgumentParser for " + type);
            }

            return parseOrFail(parser);
        }

        @Nonnull
        default <T> Optional<T> parse(@Nonnull Class<T> clazz) {
            return parserRegistry().find(clazz).flatMap(this::parse);
        }

        @Nonnull
        default <T> T parseOrFail(@Nonnull Class<T> clazz) throws CommandInterruptException {
            ArgumentParser<T> parser = parserRegistry().find(clazz).orElse(null);
            if (parser == null) {
                throw new RuntimeException("Unable to find ArgumentParser for " + clazz);
            }

            return parseOrFail(parser);
        }

        /**
         * Gets if the argument is present
         *
         * @return true if present
         */
        boolean isPresent();

        /**
         * Asserts that the permission is present
         */
        default void assertPresent() throws CommandInterruptException {
            makeAssertion(isPresent(), "&cArgument at index " + index() + " is not present.");
        }

    }

    /**
     * Parses an argument from a String
     *
     * @param <T> the value type
     */
    public interface ArgumentParser<T> {

        /**
         * Parses the value from a string
         *
         * @param s the string
         * @return the value, if parsing was successful
         */
        @Nonnull
        Optional<T> parse(@Nonnull String s);

        /**
         * Parses the value from a string, throwing an interrupt exception if
         * parsing failed.
         *
         * @param s the string
         * @return the value
         */
        @Nonnull
        default T parseOrFail(@Nonnull String s) throws CommandInterruptException {
            Optional<T> ret = parse(s);
            if (!ret.isPresent()) {
                throw new InformedCommandInterruptException("&cUnable to parse argument: " + s);
            }
            return ret.get();
        }

        /**
         * Tries to parse the value from the argument
         *
         * @param argument the argument
         * @return the value, if parsing was successful
         */
        @Nonnull
        default Optional<T> parse(@Nonnull Argument argument) {
            return argument.value().flatMap(this::parse);
        }

        /**
         * Parses the value from an argument, throwing an interrupt exception if
         * parsing failed.
         *
         * @param argument the argument
         * @return the value
         */
        default T parseOrFail(@Nonnull Argument argument) throws CommandInterruptException {
            Optional<T> ret = parse(argument);
            if (!ret.isPresent()) {
                throw new InformedCommandInterruptException("&cUnable to parse argument at index " + argument.index() + ".");
            }
            return ret.get();
        }

        /**
         * Creates a new parser which first tries to obtain a value from
         * this parser, then from another if the former was not successful.
         *
         * @param other the other parser
         * @return the combined parser
         */
        default ArgumentParser<T> thenTry(ArgumentParser<T> other) {
            ArgumentParser<T> first = this;
            return t -> {
                Optional<T> ret = first.parse(t);
                return ret.isPresent() ? ret : other.parse(t);
            };
        }

    }

    /**
     * A collection of {@link ArgumentParser}s
     */
    public interface ArgumentParserRegistry {

        /**
         * Tries to find an argument parser for the given type
         *
         * @param type the argument type
         * @param <T> the type
         * @return an argument, if one was found
         */
        @Nonnull
        <T> Optional<ArgumentParser<T>> find(@Nonnull TypeToken<T> type);

        /**
         * Tries to find an argument parser for the given class
         *
         * @param clazz the argument class
         * @param <T> the class type
         * @return an argument, if one was found
         */
        @Nonnull
        default <T> Optional<ArgumentParser<T>> find(@Nonnull Class<T> clazz) {
            return find(TypeToken.of(clazz));
        }

        /**
         * Finds all known parsers for a given type
         *
         * @param type the argument type
         * @param <T> the type
         * @return a collection of argument parsers
         */
        @Nonnull
        <T> Collection<ArgumentParser<T>> findAll(@Nonnull TypeToken<T> type);

        /**
         * Finds all known parsers for a given class
         *
         * @param clazz the argument class
         * @param <T> the class type
         * @return a collection of argument parsers
         */
        @Nonnull
        default <T> Collection<ArgumentParser<T>> findAll(@Nonnull Class<T> clazz) {
            return findAll(TypeToken.of(clazz));
        }

        /**
         * Registers a new parser with the registry
         *
         * @param type the argument type
         * @param parser the parser
         * @param <T> the type
         */
        <T> void register(@Nonnull TypeToken<T> type, @Nonnull ArgumentParser<T> parser);

        /**
         * Registers a new parser with the registry
         *
         * @param clazz the argument class
         * @param parser the parser
         * @param <T> the class type
         */
        default <T> void register(@Nonnull Class<T> clazz, @Nonnull ArgumentParser<T> parser) {
            register(TypeToken.of(clazz), parser);
        }

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
                if (context.sender().hasPermission(permission)) {
                    return true;
                }

                context.sender().sendMessage(Color.colorize(failureMessage));
                return false;
            });
            return this;
        }

        @Override
        public CommandBuilder<T> assertOp(String failureMessage) {
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
        public CommandBuilder<Player> assertPlayer(String failureMessage) {
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                if (context.sender() instanceof Player) {
                    return true;
                }

                context.sender().sendMessage(Color.colorize(failureMessage));
                return false;
            });
            // cast the generic type
            return new CommandBuilderImpl<>(predicates);
        }

        @Override
        public CommandBuilder<ConsoleCommandSender> assertConsole(String failureMessage) {
            Preconditions.checkNotNull(failureMessage, "failureMessage");
            predicates.add(context -> {
                if (context.sender() instanceof ConsoleCommandSender) {
                    return true;
                }

                context.sender().sendMessage(Color.colorize(failureMessage));
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
                if (context.args().size() >= finalRequiredArgs) {
                    return true;
                }

                context.sender().sendMessage(Color.colorize(failureMessage.replace("{usage}", "/" + context.label() + " " + usage)));
                return false;
            });

            return this;
        }

        @Override
        public CommandBuilder<T> assertArgument(int index, Predicate<String> test, String failureMessage) {
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
        public CommandBuilder<T> assertSender(Predicate<T> test, String failureMessage) {
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
        public CommandBuilder<T> withCooldown(Cooldown cooldown, String failureMessage) {
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
        public CommandBuilder<T> withCooldown(CooldownCollection<T> cooldown, String failureMessage) {
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
        public FunctionalCommand handler(CommandHandler<T> handler) {
            Preconditions.checkNotNull(handler, "handler");
            return new FunctionalCommandImpl(predicates.build(), handler);
        }

    }

    private static final class FunctionalCommandImpl implements FunctionalCommand, CommandExecutor {
        private final ImmutableList<Predicate<CommandContext<?>>> predicates;
        private final CommandHandler handler;

        @Nullable
        private MCTiming timing = null;

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

            try {
                //noinspection unchecked
                handler.handle(context);
            } catch (InformedCommandInterruptException e) {
                e.getAction().accept(context.sender());
            } catch (CommandInterruptException e) {
                // ignore
            }
        }

        @Override
        public void register(ExtendedJavaPlugin plugin, String... aliases) {
            plugin.registerCommand(this, aliases);
            timing = Timings.of("helper-commands: " + plugin.getName() + " - " + Arrays.toString(aliases));
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            try {
                if (timing != null) {
                    timing.startTiming();
                }

                CommandContext<CommandSender> context = new ImmutableCommandContext<>(sender, label, args);
                handle(context);
                return true;

            } finally {
                if (timing != null) {
                    timing.stopTiming();
                }
            }
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

        @Nonnull
        @Override
        public T sender() {
            return sender;
        }

        @Nonnull
        @Override
        public ImmutableList<String> args() {
            return args;
        }

        @Nonnull
        @Override
        public Argument arg(int index) {
            return new ArgumentImpl(index, rawArg(index));
        }

        @Nullable
        @Override
        public String rawArg(int index) {
            if (index < 0 || index >= args.size()) {
                return null;
            }
            return args.get(index);
        }

        @Nonnull
        @Override
        public String label() {
            return label;
        }
    }

    @NonnullByDefault
    private static final class ArgumentImpl implements Argument {
        private final int index;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<String> value;

        private ArgumentImpl(int index, @Nullable String value) {
            this.index = index;
            this.value = Optional.ofNullable(value);
        }

        @Override
        public int index() {
            return index;
        }

        @Nonnull
        @Override
        public Optional<String> value() {
            return value;
        }

        @Override
        public boolean isPresent() {
            return value.isPresent();
        }
    }

    private static final class ArgumentRegistryImpl implements ArgumentParserRegistry {
        private final Map<TypeToken<?>, List<ArgumentParser<?>>> parsers = new ConcurrentHashMap<>();

        @Nonnull
        @Override
        public <T> Optional<ArgumentParser<T>> find(@Nonnull TypeToken<T> type) {
            Preconditions.checkNotNull(type, "type");
            List<ArgumentParser<?>> parsers = this.parsers.get(type);
            if (parsers == null || parsers.isEmpty()) {
                return Optional.empty();
            }

            //noinspection unchecked
            return Optional.of((ArgumentParser<T>) parsers.get(0));
        }

        @Nonnull
        @Override
        public <T> Collection<ArgumentParser<T>> findAll(@Nonnull TypeToken<T> type) {
            Preconditions.checkNotNull(type, "type");
            List<ArgumentParser<?>> parsers = this.parsers.get(type);
            if (parsers == null || parsers.isEmpty()) {
                return ImmutableList.of();
            }

            //noinspection unchecked
            return (Collection) Collections.unmodifiableList(parsers);
        }

        @Override
        public <T> void register(@Nonnull TypeToken<T> type, @Nonnull ArgumentParser<T> parser) {
            Preconditions.checkNotNull(type, "type");
            Preconditions.checkNotNull(parser, "parser");
            List<ArgumentParser<?>> list = parsers.computeIfAbsent(type, t -> new CopyOnWriteArrayList<>());
            if (!list.contains(parser)) {
                list.add(parser);
            }
        }
    }

    private Commands() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
