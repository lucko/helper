/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me>
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

package me.lucko.helper.scoreboard;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Preconditions;

import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A thread-safe scoreboard using ProtocolLib
 */
public class PacketScoreboard {
    private final ProtocolManager protocolManager;

    private final Map<String, PacketScoreboardTeam> teams = Collections.synchronizedMap(new HashMap<>());
    private final Map<Player, Map<String, PacketScoreboardTeam>> playerTeams = Collections.synchronizedMap(new HashMap<>());

    private final Map<String, PacketScoreboardObjective> objectives = Collections.synchronizedMap(new HashMap<>());
    private final Map<Player, Map<String, PacketScoreboardObjective>> playerObjectives = Collections.synchronizedMap(new HashMap<>());

    public PacketScoreboard(Plugin plugin) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        if (plugin instanceof ExtendedJavaPlugin) {
            ExtendedJavaPlugin casted = (ExtendedJavaPlugin) plugin;
            Events.subscribe(PlayerJoinEvent.class).handler(this::handlePlayerJoin).register(casted);
            Events.subscribe(PlayerQuitEvent.class).handler(this::handlePlayerQuit).register(casted);
        } else {
            Events.subscribe(PlayerJoinEvent.class).handler(this::handlePlayerJoin);
            Events.subscribe(PlayerQuitEvent.class).handler(this::handlePlayerQuit);
        }
    }

    private void handlePlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        teams.values().forEach(t -> t.subscribe(player));
        objectives.values().forEach(o -> o.subscribe(player));
    }

    private void handlePlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        teams.values().forEach(t -> {
            t.unsubscribe(player, true);
            t.removePlayer(player);
        });
        objectives.values().forEach(o -> o.unsubscribe(player, true));

        Map<String, PacketScoreboardObjective> playerObjectives = this.playerObjectives.remove(player);
        if (playerObjectives != null) {
            playerObjectives.values().forEach(o -> o.unsubscribe(player, true));
        }

        Map<String, PacketScoreboardTeam> playerTeams = this.playerTeams.remove(player);
        if (playerTeams != null) {
            playerTeams.values().forEach(t -> {
                t.unsubscribe(player, true);
                t.removePlayer(player);
            });
        }
    }

    public PacketScoreboardTeam createTeam(String id, String title) {
        Preconditions.checkState(!teams.containsKey(id), "id already exists");

        PacketScoreboardTeam team = new PacketScoreboardTeam(this, id, title);
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            team.subscribe(player);
        }

        teams.put(id, team);
        return team;
    }

    PacketScoreboardTeam createTeam(String title) {
        return createTeam(Long.toHexString(System.nanoTime()), title);
    }

    public PacketScoreboardTeam getTeam(String id) {
        return teams.get(id);
    }

    public boolean removeTeam(String id) {
        PacketScoreboardTeam team = teams.remove(id);
        if (team == null) {
            return false;
        }

        team.unsubscribeAll();
        return true;
    }

    public PacketScoreboardObjective createObjective(String id, String title, DisplaySlot displaySlot) {
        Preconditions.checkState(!objectives.containsKey(id), "id already exists");

        PacketScoreboardObjective objective = new PacketScoreboardObjective(this, id, title, displaySlot);
        for (Player player : Bukkit.getOnlinePlayers()) {
            objective.subscribe(player);
        }

        objectives.put(id, objective);
        return objective;
    }

    public PacketScoreboardObjective createObjective(String title, DisplaySlot displaySlot) {
        return createObjective(Long.toHexString(System.nanoTime()), title, displaySlot);
    }

    public PacketScoreboardObjective getObjective(String id) {
        return objectives.get(id);
    }

    public boolean removeObjective(String id) {
        PacketScoreboardObjective objective = objectives.remove(id);
        if (objective == null) {
            return false;
        }

        objective.unsubscribeAll();
        return true;
    }

    public PacketScoreboardObjective createPlayerObjective(Player player, String id, String title, DisplaySlot displaySlot) {
        Map<String, PacketScoreboardObjective> objectives = playerObjectives.computeIfAbsent(player, p -> new HashMap<>());
        Preconditions.checkState(!objectives.containsKey(id), "id already exists");

        PacketScoreboardObjective objective = new PacketScoreboardObjective(this, id, title, displaySlot);
        objective.subscribe(player);
        objectives.put(id, objective);

        return objective;
    }

    public PacketScoreboardObjective createPlayerObjective(Player player, String title, DisplaySlot displaySlot) {
        return createPlayerObjective(player, Long.toHexString(System.nanoTime()), title, displaySlot);
    }

    public PacketScoreboardObjective getPlayerObjective(Player player, String id) {
        Map<String, PacketScoreboardObjective> map = playerObjectives.get(player);
        if (map == null) {
            return null;
        }

        return map.get(id);
    }

    public boolean removePlayerObjective(Player player, String id) {
        Map<String, PacketScoreboardObjective> map = playerObjectives.get(player);
        if (map == null) {
            return false;
        }

        PacketScoreboardObjective objective = map.remove(id);
        if (objective == null) {
            return false;
        }

        objective.unsubscribeAll();
        return true;
    }

    public PacketScoreboardTeam createPlayerTeam(Player player, String id, String title) {
        Map<String, PacketScoreboardTeam> teams = playerTeams.computeIfAbsent(player, p -> new HashMap<>());
        Preconditions.checkState(!teams.containsKey(id), "id already exists");

        PacketScoreboardTeam team = new PacketScoreboardTeam(this, id, title);
        team.subscribe(player);
        teams.put(id, team);

        return team;
    }

    public PacketScoreboardTeam createPlayerTeam(Player player, String title) {
        return createPlayerTeam(player, Long.toHexString(System.nanoTime()), title);
    }

    public PacketScoreboardTeam getPlayerTeam(Player player, String id) {
        Map<String, PacketScoreboardTeam> map = playerTeams.get(player);
        if (map == null) {
            return null;
        }

        return map.get(id);
    }

    public boolean removePlayerTeam(Player player, String id) {
        Map<String, PacketScoreboardTeam> map = playerTeams.get(player);
        if (map == null) {
            return false;
        }

        PacketScoreboardTeam team = map.remove(id);
        if (team == null) {
            return false;
        }

        team.unsubscribeAll();
        return true;
    }

    void sendPacket(PacketContainer packet, Player player) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void broadcastPacket(Iterable<Player> players, PacketContainer packet) {
        for (Player player : players) {
            sendPacket(packet, player);
        }
    }

}
