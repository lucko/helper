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

package me.lucko.helper.lilypad.extended;

import com.google.common.collect.ImmutableSet;

import me.lucko.helper.Schedulers;
import me.lucko.helper.lilypad.LilyPad;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.profiles.Profile;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.utils.Players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;

import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.request.impl.GetPlayersRequest;
import lilypad.client.connect.api.result.impl.GetPlayersResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

class LilyPadNetworkImpl implements LilyPadNetwork {
    private final CompositeTerminable compositeTerminable = CompositeTerminable.create();

    private final Map<String, Server> servers = new ConcurrentHashMap<>();
    private int overallPlayerCount = 0;

    public LilyPadNetworkImpl(LilyPad lilyPad) {
        Channel<ServerMessage> serverChannel = lilyPad.getChannel("hlp-server", ServerMessage.class);

        ChannelAgent<ServerMessage> serverChannelAgent = serverChannel.newAgent();
        serverChannelAgent.bindWith(this.compositeTerminable);
        serverChannelAgent.addListener((agent, message) -> this.servers.computeIfAbsent(message.id, Server::new).loadData(message));

        Schedulers.builder()
                .async()
                .afterAndEvery(3, TimeUnit.SECONDS)
                .run(() -> {
                    ServerMessage msg = new ServerMessage();
                    msg.time = System.currentTimeMillis();
                    msg.id = lilyPad.getId();
                    msg.groups = new ArrayList<>(lilyPad.getGroups());
                    msg.players = Players.stream().collect(Collectors.toMap(Entity::getUniqueId, HumanEntity::getName));
                    msg.maxPlayers = Bukkit.getMaxPlayers();

                    serverChannel.sendMessage(msg);
                })
                .bindWith(this.compositeTerminable);

        Schedulers.builder()
                .async()
                .afterAndEvery(3, TimeUnit.SECONDS)
                .run(() -> {
                    try {
                        GetPlayersResult result = lilyPad.getConnect().request(new GetPlayersRequest()).await();
                        this.overallPlayerCount = result.getCurrentPlayers();
                    } catch (InterruptedException | RequestException e) {
                        e.printStackTrace();
                    }
                })
                .bindWith(this.compositeTerminable);
    }

    @Override
    public Map<String, LilyPadServer> getServers() {
        return Collections.unmodifiableMap(this.servers);
    }

    @Override
    public Map<UUID, Profile> getOnlinePlayers() {
        Map<UUID, Profile> players = new HashMap<>();
        for (LilyPadServer server : this.servers.values()) {
            for (Profile profile : server.getOnlinePlayers()) {
                players.put(profile.getUniqueId(), profile);
            }
        }
        return players;
    }

    @Override
    public int getOverallPlayerCount() {
        return this.overallPlayerCount;
    }

    @Override
    public void close() {
        this.compositeTerminable.closeAndReportException();
    }

    private static final class Server implements LilyPadServer {
        private final String id;

        private long lastPing = 0;
        private Set<String> groups = ImmutableSet.of();
        private Set<Profile> players = ImmutableSet.of();
        private int maxPlayers = 0;

        public Server(String id) {
            this.id = id;
        }

        private void loadData(ServerMessage msg) {
            this.lastPing = msg.time;
            this.groups = ImmutableSet.copyOf(msg.groups);

            ImmutableSet.Builder<Profile> players = ImmutableSet.builder();
            for (Map.Entry<UUID, String> p : msg.players.entrySet()) {
                players.add(Profile.create(p.getKey(), p.getValue()));
            }
            this.players = players.build();
            this.maxPlayers = msg.maxPlayers;
        }

        @Nonnull
        @Override
        public String getId() {
            return this.id;
        }

        @Nonnull
        @Override
        public Set<String> getGroups() {
            return this.groups;
        }

        @Override
        public boolean isOnline() {
            long diff = System.currentTimeMillis() - this.lastPing;
            return diff < TimeUnit.SECONDS.toMillis(5);
        }

        @Override
        public long getLastPing() {
            return this.lastPing;
        }

        @Override
        public Set<Profile> getOnlinePlayers() {
            return this.players;
        }

        @Override
        public int getMaxPlayers() {
            return this.maxPlayers;
        }
    }

    private static final class ServerMessage {
        private String id;
        private List<String> groups;
        private long time;
        private Map<UUID, String> players;
        private int maxPlayers;
    }
}
