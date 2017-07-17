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

package me.lucko.helper.messaging.bungee;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.lucko.helper.Scheduler;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableRegistry;
import me.lucko.helper.utils.LoaderUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Wrapper class for the BungeeCord Plugin Messaging API, providing callbacks to read response data
 *
 * <p>See: https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel and
 * https://github.com/SpigotMC/BungeeCord/blob/master/proxy/src/main/java/net/md_5/bungee/connection/DownstreamBridge.java#L223</p>
 */
public final class BungeeMessaging implements PluginMessageListener {

    private static BungeeMessaging instance = null;
    private static synchronized BungeeMessaging get() {
        if (instance == null) {
            instance = new BungeeMessaging();
        }
        return instance;
    }

    /**
     * Server name to represent all servers on the proxy
     */
    public static final String ALL_SERVERS = "ALL";

    /**
     * Server name to represent only the online servers on the proxy
     */
    public static final String ONLINE_SERVERS = "ONLINE";

    /**
     * The name of the BungeeCord plugin channel
     */
    private static final String CHANNEL = "BungeeCord";

    /**
     * Registers a message agent with the handler
     *
     * @param agent the agent to register
     */
    private static void register(MessageAgent agent) {
        get().sendMessage(agent);
    }

    /**
     * Connects a player to said subserver
     *
     * @param player the player to connect
     * @param serverName the name of the server to connect to
     */
    public static void connect(Player player, String serverName) {
        register(new ConnectAgent(player, serverName));
    }

    /**
     * Connects a named player to said subserver
     *
     * @param playerName the username of the player to connect
     * @param serverName the name of the server to connect to
     */
    public static void connectOther(String playerName, String serverName) {
        register(new ConnectOtherAgent(playerName, serverName));
    }

    /**
     * Get the real IP of a player
     *
     * @param player the player to get the IP of
     * @param callback a callback to consume the ip and port
     */
    public static void ip(Player player, Consumer<Map.Entry<String, Integer>> callback) {
        register(new IPAgent(player, callback));
    }

    /**
     * Gets the amount of players on a certain server, or all servers
     *
     * @param serverName the name of the server to get the player count for. Use {@link #ALL_SERVERS} to get the global count
     * @param callback a callback to consume the count
     */
    public static void playerCount(String serverName, Consumer<Integer> callback) {
        register(new PlayerCountAgent(serverName, callback));
    }

    /**
     * Gets a list of players connected on a certain server, or all servers.
     *
     * @param serverName the name of the server to get the player list for. Use {@link #ALL_SERVERS} to get the global list
     * @param callback a callback to consume the player list
     */
    public static void playerList(String serverName, Consumer<List<String>> callback) {
        register(new PlayerListAgent(serverName, callback));
    }

    /**
     * Get a list of server name strings, as defined in the BungeeCord config
     *
     * @param callback a callback to consume the list of server names
     */
    public static void getServers(Consumer<List<String>> callback) {
        register(new GetServersAgent(callback));
    }

    /**
     * Send a message (as in chat message) to the specified player
     *
     * @param playerName the username of the player to send the message to
     * @param message the message to send
     */
    public static void message(String playerName, String message) {
        register(new PlayerMessageAgent(playerName, message));
    }

    /**
     * Gets this servers name, as defined in the BungeeCord config
     *
     * @param callback a callback to consume the name
     */
    public static void getServer(Consumer<String> callback) {
        register(new GetServerAgent(callback));
    }

    /**
     * Get the UUID of a player
     *
     * @param player the player to get the uuid of
     * @param callback a callback to consume the uuid
     */
    public static void uuid(Player player, Consumer<UUID> callback) {
        register(new UUIDAgent(player, callback));
    }

    /**
     * Get the UUID of any player connected to the proxy
     *
     * @param playerName the username of the player to get the uuid of
     * @param callback a callback to consume the uuid
     */
    public static void uuidOther(String playerName, Consumer<UUID> callback) {
        register(new UUIDOtherAgent(playerName, callback));
    }

    /**
     * Get the IP of any server connected to the proxy
     *
     * @param serverName the name of the server to get the ip of
     * @param callback a callback to consume the ip and port
     */
    public static void serverIp(String serverName, Consumer<Map.Entry<String, Integer>> callback) {
        register(new ServerIPAgent(serverName, callback));
    }

    /**
     * Kick a player from the proxy
     *
     * @param playerName the username of the player to kick
     * @param reason the reason to display to the player when they are kicked
     */
    public static void kickPlayer(String playerName, String reason) {
        register(new KickPlayerAgent(playerName, reason));
    }

    /**
     * Sends a custom plugin message to a given server.
     *
     * <p>You can use {@link #registerForwardCallbackRaw(String, Predicate)} to register listeners on a given subchannel.</p>
     *
     * @param serverName the name of the server to send to. use {@link #ALL_SERVERS} to send to all servers, or {@link #ONLINE_SERVERS} to only send to servers which are online.
     * @param channelName the name of the subchannel
     * @param data the data to send
     */
    public static void forward(String serverName, String channelName, byte[] data) {
        register(new ForwardAgent(serverName, channelName, data));
    }

    /**
     * Sends a custom plugin message to a given server.
     *
     * <p>You can use {@link #registerForwardCallback(String, Predicate)} to register listeners on a given subchannel.</p>
     *
     * @param serverName the name of the server to send to. use {@link #ALL_SERVERS} to send to all servers, or {@link #ONLINE_SERVERS} to only send to servers which are online.
     * @param channelName the name of the subchannel
     * @param data the data to send
     */
    public static void forward(String serverName, String channelName, ByteArrayDataOutput data) {
        register(new ForwardAgent(serverName, channelName, data));
    }

    /**
     * Sends a custom plugin message to a given server.
     *
     * <p>You can use {@link #registerForwardCallbackRaw(String, Predicate)} to register listeners on a given subchannel.</p>
     *
     * @param playerName the username of a player. BungeeCord will send the forward message to their server.
     * @param channelName the name of the subchannel
     * @param data the data to send
     */
    public static void forwardToPlayer(String playerName, String channelName, byte[] data) {
        register(new ForwardToPlayerAgent(playerName, channelName, data));
    }

    /**
     * Sends a custom plugin message to a given server.
     *
     * <p>You can use {@link #registerForwardCallback(String, Predicate)} to register listeners on a given subchannel.</p>
     *
     * @param playerName the username of a player. BungeeCord will send the forward message to their server.
     * @param channelName the name of the subchannel
     * @param data the data to send
     */
    public static void forwardToPlayer(String playerName, String channelName, ByteArrayDataOutput data) {
        register(new ForwardToPlayerAgent(playerName, channelName, data));
    }

    /**
     * Registers a callback to listen for messages sent on forwarded subchannels.
     *
     * <p>Use {@link #forward(String, String, byte[])} to dispatch messages.</p>
     *
     * @param channelName the name of the channel to listen on
     * @param callback the callback. the predicate should return true when the callback should be unregistered.
     */
    public static void registerForwardCallbackRaw(String channelName, Predicate<byte[]> callback) {
        ForwardCustomCallback customCallback = new ForwardCustomCallback(channelName, callback);
        get().registerListener(customCallback);
    }

    /**
     * Registers a callback to listen for messages sent on forwarded subchannels.
     *
     * <p>Use {@link #forward(String, String, ByteArrayDataOutput)} to dispatch messages.</p>
     *
     * @param channelName the name of the channel to listen on
     * @param callback the callback. the predicate should return true when the callback should be unregistered.
     */
    public static void registerForwardCallback(String channelName, Predicate<ByteArrayDataInput> callback) {
        final Predicate<ByteArrayDataInput> cb = Preconditions.checkNotNull(callback, "callback");
        ForwardCustomCallback customCallback = new ForwardCustomCallback(channelName, bytes -> cb.test(ByteStreams.newDataInput(bytes)));
        get().registerListener(customCallback);
    }

    private final Plugin plugin;
    private final TerminableRegistry terminableRegistry = TerminableRegistry.create();
    private final List<MessageCallback> listeners = new LinkedList<>();
    private final Set<MessageAgent> queuedMessages = ConcurrentHashMap.newKeySet();
    private final ReentrantLock lock = new ReentrantLock();

    private BungeeMessaging() {
        plugin = LoaderUtils.getPlugin();
        
        if (plugin instanceof ExtendedJavaPlugin) {
            ExtendedJavaPlugin ejp = (ExtendedJavaPlugin) plugin;
            ejp.registerTerminable(Terminable.of(() -> {
                BungeeMessaging.instance = null;
                terminableRegistry.terminate();
            }));
        }

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, CHANNEL);
        terminableRegistry.accept(Terminable.of(() -> plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin, CHANNEL)));

        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, CHANNEL, this);
        terminableRegistry.accept(Terminable.of(() -> plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, CHANNEL, this)));

        Scheduler.runTaskRepeatingSync(() -> {
            if (queuedMessages.isEmpty()) {
                return;
            }

            Player p = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (p != null) {
                queuedMessages.removeIf(ma -> {
                    sendToChannel(ma, p);
                   return true;
                });
            }
        }, 60L, 60L).register(terminableRegistry);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] data) {
        if (!channel.equals(CHANNEL)) {
            return;
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        ByteArrayDataInput in = ByteStreams.newDataInput(inputStream);
        String subChannel = in.readUTF();

        // exclude the first value from the input stream
        inputStream.mark(0);

        lock.lock();
        try {
            Iterator<MessageCallback> it = listeners.iterator();
            while (it.hasNext()) {
                MessageCallback e = it.next();

                if (!e.getSubChannel().equals(subChannel)) {
                    continue;
                }

                inputStream.reset();
                boolean accepted = e.test(player, in);

                if (!accepted) {
                    continue;
                }

                inputStream.reset();
                boolean shouldRemove = e.accept(player, in);

                if (shouldRemove) {
                    it.remove();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void sendMessage(MessageAgent agent) {
        Player player = agent.getHandle();
        if (player != null) {
            if (!player.isOnline()) {
                throw new IllegalStateException("Player not online");
            }

            sendToChannel(agent, player);
            return;
        }

        // try to find a player
        player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player != null) {
            sendToChannel(agent, player);
        } else {
            // no players online, queue the message
            queuedMessages.add(agent);
        }
    }

    private void sendToChannel(MessageAgent agent, Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(agent.getSubChannel());
        agent.appendPayload(out);

        byte[] data = out.toByteArray();
        player.sendPluginMessage(plugin, CHANNEL, data);

        // register listener
        if (agent instanceof MessageCallback) {
            MessageCallback callback = (MessageCallback) agent;
            registerListener(callback);
        }
    }

    private void registerListener(MessageCallback callback) {
        lock.lock();
        try {
            listeners.add(callback);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Responsible for writing data to the output stream when the message is to be sent
     */
    private interface MessageAgent {

        /**
         * Gets the sub channel this message should be sent using
         *
         * @return the message channel
         */
        String getSubChannel();

        /**
         * Gets the player to send the message via
         *
         * @return the player to send the message via, or null if any player should be used
         */
        default Player getHandle() {
            return null;
        }

        /**
         * Appends the data for this message to the output stream
         *
         * @param out the output stream
         */
        default void appendPayload(ByteArrayDataOutput out) {

        }

    }

    /**
     * Responsible for monitoring incoming messages, and passing on the callback data if applicable
     */
    private interface MessageCallback {

        /**
         * Gets the sub channel this callback is listening for
         *
         * @return the message channel
         */
        String getSubChannel();

        /**
         * Returns true if the incoming data applies to this callback
         *
         * @param receiver the player instance which received the data
         * @param in the input data
         * @return true if the data is applicable
         */
        default boolean test(Player receiver, ByteArrayDataInput in) {
            return true;
        }

        /**
         * Accepts the incoming data, and returns true if this callback should now be de-registered
         *
         * @param receiver the player instance which received the data
         * @param in the input data
         * @return if the callback should be de-registered
         */
        boolean accept(Player receiver, ByteArrayDataInput in);

    }

    private static final class ConnectAgent implements MessageAgent {
        private static final String CHANNEL = "Connect";

        private final Player player;
        private final String serverName;

        private ConnectAgent(Player player, String serverName) {
            this.player = Preconditions.checkNotNull(player, "player");
            this.serverName = Preconditions.checkNotNull(serverName, "serverName");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public Player getHandle() {
            return player;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(serverName);
        }
    }

    private static final class ConnectOtherAgent implements MessageAgent {
        private static final String CHANNEL = "ConnectOther";

        private final String playerName;
        private final String serverName;

        private ConnectOtherAgent(String playerName, String serverName) {
            this.playerName = Preconditions.checkNotNull(playerName, "playerName");
            this.serverName = Preconditions.checkNotNull(serverName, "serverName");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(playerName);
            out.writeUTF(serverName);
        }
    }

    private static final class IPAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "IP";

        private final Player player;
        private final Consumer<Map.Entry<String, Integer>> callback;

        private IPAgent(Player player, Consumer<Map.Entry<String, Integer>> callback) {
            this.player = Preconditions.checkNotNull(player, "player");
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public Player getHandle() {
            return player;
        }

        @Override
        public boolean test(Player receiver, ByteArrayDataInput in) {
            return receiver.getUniqueId().equals(player.getUniqueId());
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            String ip = in.readUTF();
            int port = in.readInt();
            callback.accept(Maps.immutableEntry(ip, port));
            return true;
        }
    }

    private static final class PlayerCountAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "PlayerCount";

        private final String serverName;
        private final Consumer<Integer> callback;

        private PlayerCountAgent(String serverName, Consumer<Integer> callback) {
            this.serverName = Preconditions.checkNotNull(serverName, "serverName");
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(serverName);
        }

        @Override
        public boolean test(Player receiver, ByteArrayDataInput in) {
            return in.readUTF().equalsIgnoreCase(serverName);
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            in.readUTF();
            int count = in.readInt();
            callback.accept(count);
            return true;
        }
    }

    private static final class PlayerListAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "PlayerList";

        private final String serverName;
        private final Consumer<List<String>> callback;

        private PlayerListAgent(String serverName, Consumer<List<String>> callback) {
            this.serverName = Preconditions.checkNotNull(serverName, "serverName");
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(serverName);
        }

        @Override
        public boolean test(Player receiver, ByteArrayDataInput in) {
            return in.readUTF().equalsIgnoreCase(serverName);
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            in.readUTF();
            String csv = in.readUTF();

            if (csv.isEmpty()) {
                callback.accept(ImmutableList.of());
                return true;
            }

            callback.accept(ImmutableList.copyOf(Splitter.on(", ").splitToList(csv)));
            return true;
        }
    }

    private static final class GetServersAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "GetServers";
        
        private final Consumer<List<String>> callback;

        private GetServersAgent(Consumer<List<String>> callback) {
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            String csv = in.readUTF();

            if (csv.isEmpty()) {
                callback.accept(ImmutableList.of());
                return true;
            }

            callback.accept(ImmutableList.copyOf(Splitter.on(", ").splitToList(csv)));
            return true;
        }
    }

    private static final class PlayerMessageAgent implements MessageAgent {
        private static final String CHANNEL = "Message";

        private final String playerName;
        private final String message;

        private PlayerMessageAgent(String playerName, String message) {
            this.playerName = Preconditions.checkNotNull(playerName, "playerName");
            this.message = Preconditions.checkNotNull(message, "message");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(playerName);
            out.writeUTF(message);
        }
    }

    private static final class GetServerAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "GetServer";

        private final Consumer<String> callback;

        private GetServerAgent(Consumer<String> callback) {
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            callback.accept(in.readUTF());
            return true;
        }
    }

    private static final class UUIDAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "UUID";

        private final Player player;
        private final Consumer<java.util.UUID> callback;

        private UUIDAgent(Player player, Consumer<java.util.UUID> callback) {
            this.player = Preconditions.checkNotNull(player, "player");
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public Player getHandle() {
            return player;
        }

        @Override
        public boolean test(Player receiver, ByteArrayDataInput in) {
            return receiver.getUniqueId().equals(player.getUniqueId());
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            String uuid = in.readUTF();
            callback.accept(java.util.UUID.fromString(uuid));
            return true;
        }
    }

    private static final class UUIDOtherAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "UUIDOther";

        private final String playerName;
        private final Consumer<java.util.UUID> callback;

        private UUIDOtherAgent(String playerName, Consumer<java.util.UUID> callback) {
            this.playerName = Preconditions.checkNotNull(playerName, "playerName");
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(playerName);
        }

        @Override
        public boolean test(Player receiver, ByteArrayDataInput in) {
            return in.readUTF().equalsIgnoreCase(playerName);
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            in.readUTF();
            String uuid = in.readUTF();
            callback.accept(java.util.UUID.fromString(uuid));
            return true;
        }
    }

    private static final class ServerIPAgent implements MessageAgent, MessageCallback {
        private static final String CHANNEL = "ServerIP";

        private final String serverName;
        private final Consumer<Map.Entry<String, Integer>> callback;

        private ServerIPAgent(String serverName, Consumer<Map.Entry<String, Integer>> callback) {
            this.serverName = Preconditions.checkNotNull(serverName, "serverName");
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(serverName);
        }

        @Override
        public boolean test(Player receiver, ByteArrayDataInput in) {
            return in.readUTF().equalsIgnoreCase(serverName);
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            in.readUTF();
            String ip = in.readUTF();
            int port = in.readInt();
            callback.accept(Maps.immutableEntry(ip, port));
            return true;
        }
    }

    private static final class KickPlayerAgent implements MessageAgent {
        private static final String CHANNEL = "KickPlayer";

        private final String playerName;
        private final String reason;

        private KickPlayerAgent(String playerName, String reason) {
            this.playerName = Preconditions.checkNotNull(playerName, "playerName");
            this.reason = Preconditions.checkNotNull(reason, "reason");
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(playerName);
            out.writeUTF(reason);
        }
    }

    private static final class ForwardAgent implements MessageAgent {
        private static final String CHANNEL = "Forward";

        private final String serverName;
        private final String channelName;
        private final byte[] data;

        private ForwardAgent(String serverName, String channelName, byte[] data) {
            this.serverName = Preconditions.checkNotNull(serverName, "serverName");
            this.channelName = Preconditions.checkNotNull(channelName, "channelName");
            this.data = data;
        }

        private ForwardAgent(String serverName, String channelName, ByteArrayDataOutput data) {
            this.serverName = Preconditions.checkNotNull(serverName, "serverName");
            this.channelName = Preconditions.checkNotNull(channelName, "channelName");
            this.data = Preconditions.checkNotNull(data, "data").toByteArray();
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(serverName);
            out.writeUTF(channelName);
            out.writeShort(data.length);
            out.write(data);
        }
    }

    private static final class ForwardToPlayerAgent implements MessageAgent {
        private static final String CHANNEL = "ForwardToPlayer";

        private final String playerName;
        private final String channelName;
        private final byte[] data;

        private ForwardToPlayerAgent(String playerName, String channelName, byte[] data) {
            this.playerName = Preconditions.checkNotNull(playerName, "playerName");
            this.channelName = Preconditions.checkNotNull(channelName, "channelName");
            this.data = data;
        }

        private ForwardToPlayerAgent(String playerName, String channelName, ByteArrayDataOutput data) {
            this.playerName = Preconditions.checkNotNull(playerName, "playerName");
            this.channelName = Preconditions.checkNotNull(channelName, "channelName");
            this.data = Preconditions.checkNotNull(data, "data").toByteArray();
        }

        @Override
        public String getSubChannel() {
            return CHANNEL;
        }

        @Override
        public void appendPayload(ByteArrayDataOutput out) {
            out.writeUTF(playerName);
            out.writeUTF(channelName);
            out.writeShort(data.length);
            out.write(data);
        }
    }

    private static final class ForwardCustomCallback implements MessageCallback {
        private final String subChannel;
        private final Predicate<byte[]> callback;

        private ForwardCustomCallback(String subChannel, Predicate<byte[]> callback) {
            this.subChannel = Preconditions.checkNotNull(subChannel, "subChannel");
            this.callback = Preconditions.checkNotNull(callback, "callback");
        }

        @Override
        public String getSubChannel() {
            return subChannel;
        }

        @Override
        public boolean accept(Player receiver, ByteArrayDataInput in) {
            short len = in.readShort();
            byte[] data = new byte[len];
            in.readFully(data);

            return callback.test(data);
        }
    }

}
