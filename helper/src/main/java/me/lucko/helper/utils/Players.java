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

package me.lucko.helper.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import me.lucko.helper.Helper;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.reflect.MinecraftVersions;
import me.lucko.helper.reflect.ServerReflection;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * A collection of Player related utilities
 */
@NonnullByDefault
public final class Players {
    @Nullable private static final Object OPENBOOK_PACKET;

    @Nullable private static final Object TITLE_ENUM;
    @Nullable private static final Object SUBTITLE_ENUM;
    @Nullable private static final Constructor<?> TITLE_CONSTRUCTOR;

    @Nullable private static final Method ICHATBASECOMPONENT_A_METHOD;

    @Nullable private static final Constructor<?> TABLIST_CONSTRUCTOR;

    @Nullable private static final Object ACTIONBAR_ENUM;
    @Nullable private static final Constructor<?> ACTIONBAR_CONSTRUCTOR;

    static {
        Object title_Enum = null;
        Object subtitle_Enum = null;
        Constructor<?> title_Constructor = null;
        Method iChatBaseComponent_A_Method = null;
        Constructor<?> tablist_Constructor = null;
        Object actionbar_Enum = null;
        Constructor<?> actionbar_Constructor = null;
        try {
            title_Enum = ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
            subtitle_Enum = ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
            title_Constructor = ServerReflection.nmsClass("PacketPlayOutTitle").getConstructor(ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0], ServerReflection.nmsClass("IChatBaseComponent"), int.class, int.class, int.class);
            iChatBaseComponent_A_Method = ServerReflection.nmsClass("IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class);
            tablist_Constructor = ServerReflection.nmsClass("PacketPlayOutPlayerListHeaderFooter").getConstructor();
            actionbar_Enum = ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0].getField("ACTIONBAR").get(null);
            actionbar_Constructor = ServerReflection.nmsClass("PacketPlayOutTitle").getConstructor(ServerReflection.nmsClass("PacketPlayOutTitle").getDeclaredClasses()[0], ServerReflection.nmsClass("IChatBaseComponent"));
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        TITLE_ENUM = title_Enum;
        SUBTITLE_ENUM = subtitle_Enum;
        TITLE_CONSTRUCTOR = title_Constructor;
        ICHATBASECOMPONENT_A_METHOD = iChatBaseComponent_A_Method;
        TABLIST_CONSTRUCTOR = tablist_Constructor;
        ACTIONBAR_ENUM = actionbar_Enum;
        ACTIONBAR_CONSTRUCTOR = actionbar_Constructor;

        Object openBook_Packet = null;
        try {
            Constructor<?> packetConstructor;
            Enum<?> enumHand;
            Object packetDataSerializer;
            Object packetDataSerializerArg;
            Object minecraftKey;
            switch (ServerReflection.getNmsVersion()) {
                case v1_14_R1:
                    enumHand = (Enum<?>) ServerReflection.nmsClass("EnumHand").getField("MAIN_HAND").get(null);
                    packetConstructor = ServerReflection.nmsClass("PacketPlayOutOpenBook").getConstructor(ServerReflection.nmsClass("EnumHand"));
                    openBook_Packet = packetConstructor.newInstance(enumHand);
                    break;

                case v1_13_R2:
                case v1_13_R1:
                    enumHand = (Enum<?>) ServerReflection.nmsClass("EnumHand").getField("MAIN_HAND").get(null);
                    minecraftKey = ServerReflection.nmsClass("MinecraftKey").getMethod("a", String.class).invoke(null, "minecraft:book_open");
                    packetDataSerializerArg = ServerReflection.nmsClass("PacketDataSerializer").getConstructor(ByteBuf.class).newInstance(Unpooled.buffer());
                    packetDataSerializer = ServerReflection.nmsClass("PacketDataSerializer").getMethod("a", Enum.class).invoke(packetDataSerializerArg, enumHand);
                    packetConstructor = ServerReflection.nmsClass("PacketPlayOutCustomPayload").getConstructor(ServerReflection.nmsClass("MinecraftKey"), ServerReflection.nmsClass("PacketDataSerializer"));
                    openBook_Packet = packetConstructor.newInstance(minecraftKey, packetDataSerializer);
                    break;

                case v1_12_R1:
                case v1_11_R1:
                case v1_10_R1:
                case v1_9_R2:
                case v1_9_R1:
                    enumHand = (Enum<?>) ServerReflection.nmsClass("EnumHand").getField("MAIN_HAND").get(null);
                    packetDataSerializerArg = ServerReflection.nmsClass("PacketDataSerializer").getConstructor(ByteBuf.class).newInstance(Unpooled.buffer());
                    packetDataSerializer = ServerReflection.nmsClass("PacketDataSerializer").getMethod("a", Enum.class).invoke(packetDataSerializerArg, enumHand);
                    packetConstructor = ServerReflection.nmsClass("PacketPlayOutCustomPayload").getConstructor(String.class, ServerReflection.nmsClass("PacketDataSerializer"));
                    openBook_Packet = packetConstructor.newInstance("MC|BOpen", packetDataSerializer);
                    break;

                case v1_8_R3:
                case v1_8_R2:
                case v1_8_R1:
                    packetDataSerializer = ServerReflection.nmsClass("PacketDataSerializer").getConstructor(ByteBuf.class).newInstance(Unpooled.buffer());
                    packetConstructor = ServerReflection.nmsClass("PacketPlayOutCustomPayload").getConstructor(String.class, ServerReflection.nmsClass("PacketDataSerializer"));
                    openBook_Packet = packetConstructor.newInstance("MC|BOpen", packetDataSerializer);
                    break;
            }
        } catch (NoSuchFieldException | InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        OPENBOOK_PACKET = openBook_Packet;
    }

    /**
     * Gets a player by uuid.
     *
     * @param uuid the uuid
     * @return a player, or null
     */
    @Nullable
    public static Player getNullable(UUID uuid) {
        return Helper.server().getPlayer(uuid);
    }

    /**
     * Gets a player by uuid.
     *
     * @param uuid the uuid
     * @return an optional player
     */
    public static Optional<Player> get(UUID uuid) {
        return Optional.ofNullable(getNullable(uuid));
    }

    /**
     * Gets a player by username.
     *
     * @param username the players username
     * @return the player, or null
     */
    @Nullable
    public static Player getNullable(String username) {
        //noinspection deprecation
        return Helper.server().getPlayerExact(username);
    }

    /**
     * Gets a player by username.
     *
     * @param username the players username
     * @return an optional player
     */
    public static Optional<Player> get(String username) {
        return Optional.ofNullable(getNullable(username));
    }

    /**
     * Gets all players on the server.
     *
     * @return all players on the server
     */
    public static Collection<Player> all() {
        //noinspection unchecked
        return (Collection<Player>) Bukkit.getOnlinePlayers();
    }

    /**
     * Gets a stream of all players on the server.
     *
     * @return a stream of all players on the server
     */
    public static Stream<Player> stream() {
        return all().stream();
    }

    /**
     * Applies a given action to all players on the server
     *
     * @param consumer the action to apply
     */
    public static void forEach(Consumer<Player> consumer) {
        all().forEach(consumer);
    }

    /**
     * Applies an action to each object in the iterable, if it is a player.
     *
     * @param objects the objects to iterate
     * @param consumer the action to apply
     */
    public static void forEachIfPlayer(Iterable<Object> objects, Consumer<Player> consumer) {
        for (Object o : objects) {
            if (o instanceof Player) {
                consumer.accept(((Player) o));
            }
        }
    }

    /**
     * Gets a stream of all players within a given radius of a point
     *
     * @param center the point
     * @param radius the radius
     * @return a stream of players
     */
    public static Stream<Player> streamInRange(Location center, double radius) {
        return center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e));
    }

    /**
     * Applies an action to all players within a given radius of a point
     *
     * @param center the point
     * @param radius the radius
     * @param consumer the action to apply
     */
    public static void forEachInRange(Location center, double radius, Consumer<Player> consumer) {
        center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e))
                .forEach(consumer);
    }

    /**
     * Messages a sender a set of messages.
     *
     * @param sender the sender
     * @param msgs the messages to send
     */
    public static void msg(CommandSender sender, String... msgs) {
        for (String s : msgs) {
            sender.sendMessage(Text.colorize(s));
        }
    }

    /**
     * Sends a message to a set of senders.
     *
     * @param msg the message to send
     * @param senders the senders to whom send the message
     */
    public static void msg(String msg, CommandSender... senders) {
        for (CommandSender sender : senders) {
            sender.sendMessage(Text.colorize(msg));
        }
    }

    /**
     * Sends a title to a set of players.
     *
     * @param title the title to send
     * @param subtitle the subtitle to send
     * @param fadeIn the time, in ticks, that the title should use to appear
     * @param stay the title duration
     * @param fadeOut the time, in ticks, that the title should use to disappear
     * @param players the players to whom send the title
     */
    public static void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Player... players) {
        if (MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_11)) {
            for (Player player : players) {
                player.sendTitle(Text.colorize(title), Text.colorize(subtitle), fadeIn, stay, fadeOut);
            }
            return;
        }
        try {
            Objects.requireNonNull(ICHATBASECOMPONENT_A_METHOD);
            Objects.requireNonNull(TITLE_CONSTRUCTOR);
            title = title.replace("\\", "\\\\").replace("\\\"", "\"");
            subtitle = subtitle.replace("\\", "\\\\").replace("\\\"", "\"");

            for (Player player : players) {
                Object titleChat = ICHATBASECOMPONENT_A_METHOD.invoke(null, "{\"text\":\"" + title + "\"}");
                Object subtitleChat = ICHATBASECOMPONENT_A_METHOD.invoke(null, "{\"text\":\"" + subtitle + "\"}");

                Object titlePacket = TITLE_CONSTRUCTOR.newInstance(TITLE_ENUM, titleChat, fadeIn, stay, fadeOut);
                Object subtitlePacket = TITLE_CONSTRUCTOR.newInstance(SUBTITLE_ENUM, subtitleChat, fadeIn, stay, fadeOut);

                ServerReflection.sendPacket(titlePacket, player);
                ServerReflection.sendPacket(subtitlePacket, player);
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends an action bar to a set of players.
     *
     * @param text the action bar text
     * @param players the players to whom send the action bar
     */
    public static void sendActionBar(String text, Player... players) {
        Objects.requireNonNull(ICHATBASECOMPONENT_A_METHOD);
        Objects.requireNonNull(ACTIONBAR_CONSTRUCTOR);
        text = text.replace("\\", "\\\\").replace("\"", "\\\"");
        try {
            for (Player player : players) {
                Object chatText = ICHATBASECOMPONENT_A_METHOD.invoke(null, "{\"text\":\"" + text + "\"}");
                Object titlePacket = ACTIONBAR_CONSTRUCTOR.newInstance(ACTIONBAR_ENUM, chatText);
                ServerReflection.sendPacket(titlePacket, player);
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the action bar to a set of players.
     *
     * @param players the players to whom clear the action bar
     */
    public static void clearActionBar(Player... players) {
        sendActionBar("", players);
    }

    /**
     * Clears the title to a set of players.
     *
     * @param players the players to whom clear the title
     */
    public static void clearTitle(Player... players) {
        sendTitle("", "", 0, 0, 0, players);
    }

    /**
     * Sends a tab title to a set of players.
     *
     * @param header the tab header
     * @param footer the tab footer
     * @param players the players to whom send the tab title
     */
    public static void sendTabTitle(String header, String footer, Player... players) {
        Objects.requireNonNull(ICHATBASECOMPONENT_A_METHOD);
        Objects.requireNonNull(TABLIST_CONSTRUCTOR);
        header = header.replace("\\", "\\\\").replace("\"", "\\\"");
        footer = footer.replace("\\", "\\\\").replace("\"", "\\\"");
        try {
            for (Player player : players) {
                Object tabHeader = ICHATBASECOMPONENT_A_METHOD.invoke(null, "{\"text\":\"" + header + "\"}");
                Object tabFooter = ICHATBASECOMPONENT_A_METHOD.invoke(null, "{\"text\":\"" + footer + "\"}");
                Object packet = TABLIST_CONSTRUCTOR.newInstance();
                ServerReflection.setField(packet.getClass(), packet, "a", tabHeader);
                ServerReflection.setField(packet.getClass(), packet, "b", tabFooter);
                ServerReflection.sendPacket(packet, player);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a book to a set of players.
     *
     * @param book the book to open
     * @param players the players to whom open the book
     */
    public static void openBook(ItemStack book, Player... players) {
        if (!book.getType().equals(Material.WRITTEN_BOOK)) {
            return;
        }
        if (MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersion.of(1, 14, 2))) {
            for (Player player : players) {
                player.openBook(book);
            }
            return;
        }
        for (Player player : players) {
            int slot = player.getInventory().getHeldItemSlot();
            ItemStack old = player.getInventory().getItem(slot);
            player.getInventory().setItem(slot, book);
            ServerReflection.sendPacket(OPENBOOK_PACKET, player);
            player.getInventory().setItem(slot, old);
        }
    }

    /**
     * Gets the ping of a player.
     *
     * @param player the player of which to get the ping
     * @return the player's ping or -1 if the player is null
     */
    public static int getPing(Player player) {
        int ping = -1;
        try {
            Object craftPlayer = ServerReflection.getHandle(player);
            if (craftPlayer != null) {
                ping = (int) ServerReflection.getDeclaredField(craftPlayer.getClass(), "ping").get(craftPlayer);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return ping;
    }

    @Nullable
    public static OfflinePlayer getOfflineNullable(UUID uuid) {
        return Helper.server().getOfflinePlayer(uuid);
    }

    public static Optional<OfflinePlayer> getOffline(UUID uuid) {
        return Optional.ofNullable(getOfflineNullable(uuid));
    }

    @Nullable
    public static OfflinePlayer getOfflineNullable(String username) {
        //noinspection deprecation
        return Helper.server().getOfflinePlayer(username);
    }

    public static Optional<OfflinePlayer> getOffline(String username) {
        return Optional.ofNullable(getOfflineNullable(username));
    }

    public static Collection<OfflinePlayer> allOffline() {
        return ImmutableList.copyOf(Bukkit.getOfflinePlayers());
    }

    public static Stream<OfflinePlayer> streamOffline() {
        return Arrays.stream(Bukkit.getOfflinePlayers());
    }

    public static void forEachOffline(Consumer<OfflinePlayer> consumer) {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            consumer.accept(player);
        }
    }

    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    public static void playSound(Player player, Location location, Sound sound) {
        player.playSound(location, sound, 1.0f, 1.0f);
    }

    public static void playSound(Location location, Sound sound) {
        location.getWorld().playSound(location, sound, 1.0f, 1.0f);
    }

    @SuppressWarnings("deprecation")
    public static void sendBlockChange(Player player, Location loc, Material type, int data) {
        player.sendBlockChange(loc, type, (byte) data);
    }

    public static void sendBlockChange(Player player, Block block, Material type, int data) {
        sendBlockChange(player, block.getLocation(), type, data);
    }

    public static void sendBlockChange(Player player, Location loc, Material type) {
        sendBlockChange(player, loc, type, 0);
    }

    public static void sendBlockChange(Player player, Block block, Material type) {
        sendBlockChange(player, block, type, 0);
    }

    public static void spawnParticle(Player player, Location location, Particle particle) {
        player.spawnParticle(particle, location, 1);
    }

    public static void spawnParticle(Location location, Particle particle) {
        location.getWorld().spawnParticle(particle, location, 1);
    }

    public static void spawnParticle(Player player, Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        player.spawnParticle(particle, location, amount);
    }

    public static void spawnParticle(Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        location.getWorld().spawnParticle(particle, location, amount);
    }

    public static void spawnParticleOffset(Player player, Location location, Particle particle, double offset) {
        player.spawnParticle(particle, location, 1, offset, offset, offset);
    }

    public static void spawnParticleOffset(Location location, Particle particle, double offset) {
        location.getWorld().spawnParticle(particle, location, 1, offset, offset, offset);
    }

    public static void spawnParticleOffset(Player player, Location location, Particle particle, int amount, double offset) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        player.spawnParticle(particle, location, amount, offset, offset, offset);
    }

    public static void spawnParticleOffset(Location location, Particle particle, int amount, double offset) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        location.getWorld().spawnParticle(particle, location, amount, offset, offset, offset);
    }

    public static void spawnEffect(Player player, Location location, Effect effect) {
        player.playEffect(location, effect, null);
    }

    public static void spawnEffect(Location location, Effect effect) {
        location.getWorld().playEffect(location, effect, null);
    }

    public static void spawnEffect(Player player, Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        for (int i = 0; i < amount; i++) {
            player.playEffect(location, effect, null);
        }
    }

    public static void spawnEffect(Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        for (int i = 0; i < amount; i++) {
            location.getWorld().playEffect(location, effect, null);
        }
    }

    public static void resetWalkSpeed(Player player) {
        player.setWalkSpeed(0.2f);
    }

    public static void resetFlySpeed(Player player) {
        player.setFlySpeed(0.1f);
    }

    private Players() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
