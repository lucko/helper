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

package me.lucko.helper.scoreboard;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Preconditions;

import me.lucko.helper.Events;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.utils.Players;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * A thread-safe scoreboard using ProtocolLib
 */
@NonnullByDefault
public class PacketScoreboard implements Scoreboard {
    private final ProtocolManager protocolManager;

    // teams & objectives shared by all players.
    // these are automatically subscribed to when players join
    private final Map<String, PacketScoreboardTeam> teams = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, PacketScoreboardObjective> objectives = Collections.synchronizedMap(new HashMap<>());

    // per-player teams & objectives.
    private final Map<UUID, Map<String, PacketScoreboardTeam>> playerTeams = Collections.synchronizedMap(new HashMap<>());
    private final Map<UUID, Map<String, PacketScoreboardObjective>> playerObjectives = Collections.synchronizedMap(new HashMap<>());

    public PacketScoreboard() {
        this(null);
    }

    public PacketScoreboard(@Nullable ExtendedJavaPlugin plugin) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        if (plugin != null) {
            Events.subscribe(PlayerJoinEvent.class).handler(this::handlePlayerJoin).bindWith(plugin);
            Events.subscribe(PlayerQuitEvent.class).handler(this::handlePlayerQuit).bindWith(plugin);
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

        Map<String, PacketScoreboardObjective> playerObjectives = this.playerObjectives.remove(player.getUniqueId());
        if (playerObjectives != null) {
            playerObjectives.values().forEach(o -> o.unsubscribe(player, true));
        }

        Map<String, PacketScoreboardTeam> playerTeams = this.playerTeams.remove(player.getUniqueId());
        if (playerTeams != null) {
            playerTeams.values().forEach(t -> {
                t.unsubscribe(player, true);
                t.removePlayer(player);
            });
        }
    }

    /**
     * Creates a new scoreboard team
     *
     * @param id the id of the team
     * @param title the initial title for the team
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    public PacketScoreboardTeam createTeam(String id, String title) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Preconditions.checkState(!teams.containsKey(id), "id already exists");

        PacketScoreboardTeam team = new PacketScoreboardTeam(this, id, title);
        for (Player player : Players.all()) {
            team.subscribe(player);
        }

        teams.put(id, team);
        return team;
    }

    /**
     * Creates a new scoreboard team with an automatically generated id
     *
     * @param title the initial title for the team
     * @return the new team
     */
    public PacketScoreboardTeam createTeam(String title) {
        return createTeam(Long.toHexString(System.nanoTime()), title);
    }

    /**
     * Gets an existing scoreboard team if one with the id exists
     *
     * @param id the id of the team
     * @return the team, if present, otherwise null
     */
    public PacketScoreboardTeam getTeam(String id) {
        return teams.get(id);
    }

    /**
     * Removes a scoreboard team from this scoreboard
     *
     * @param id the id of the team
     * @return true if the team was removed successfully
     */
    public boolean removeTeam(String id) {
        PacketScoreboardTeam team = teams.remove(id);
        if (team == null) {
            return false;
        }

        team.unsubscribeAll();
        return true;
    }

    /**
     * Creates a new scoreboard objective
     *
     * @param id the id of the objective
     * @param title the initial title for the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     * @throws IllegalStateException if an objective with the same id already exists
     */
    public PacketScoreboardObjective createObjective(String id, String title, DisplaySlot displaySlot) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Preconditions.checkState(!objectives.containsKey(id), "id already exists");

        PacketScoreboardObjective objective = new PacketScoreboardObjective(this, id, title, displaySlot);
        for (Player player : Players.all()) {
            objective.subscribe(player);
        }

        objectives.put(id, objective);
        return objective;
    }

    /**
     * Creates a new scoreboard objective with an automatically generated id
     *
     * @param title the initial title for the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     */
    public PacketScoreboardObjective createObjective(String title, DisplaySlot displaySlot) {
        return createObjective(Long.toHexString(System.nanoTime()), title, displaySlot);
    }

    /**
     * Gets an existing scoreboard objective if one with the id exists
     *
     * @param id the id of the objective
     * @return the objective, if present, otherwise null
     */
    public PacketScoreboardObjective getObjective(String id) {
        return objectives.get(id);
    }

    /**
     * Removes a scoreboard objective from this scoreboard
     *
     * @param id the id of the objective
     * @return true if the objective was removed successfully
     */
    public boolean removeObjective(String id) {
        PacketScoreboardObjective objective = objectives.remove(id);
        if (objective == null) {
            return false;
        }

        objective.unsubscribeAll();
        return true;
    }

    /**
     * Creates a new per-player scoreboard team
     *
     * @param player the player to make the team for
     * @param id the id of the team
     * @param title the initial title of the team
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    public PacketScoreboardTeam createPlayerTeam(Player player, String id, String title) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Map<String, PacketScoreboardTeam> teams = playerTeams.computeIfAbsent(player.getUniqueId(), p -> new HashMap<>());
        Preconditions.checkState(!teams.containsKey(id), "id already exists");

        PacketScoreboardTeam team = new PacketScoreboardTeam(this, id, title);
        team.subscribe(player);
        teams.put(id, team);

        return team;
    }

    /**
     * Creates a new per-player scoreboard team with an automatically generated id
     *
     * @param player the player to make the team for
     * @param title the initial title of the team
     * @return the new team
     */
    public PacketScoreboardTeam createPlayerTeam(Player player, String title) {
        return createPlayerTeam(player, Long.toHexString(System.nanoTime()), title);
    }

    /**
     * Gets an existing per-player scoreboard team if one with the id exists
     *
     * @param player the player to get the team for
     * @param id the id of the team
     * @return the team, if present, otherwise null
     */
    public PacketScoreboardTeam getPlayerTeam(Player player, String id) {
        Map<String, PacketScoreboardTeam> map = playerTeams.get(player.getUniqueId());
        if (map == null) {
            return null;
        }

        return map.get(id);
    }

    /**
     * Removes a per-player scoreboard team from this scoreboard
     *
     * @param player the player to remove the team for
     * @param id the id of the team
     * @return true if the team was removed successfully
     */
    public boolean removePlayerTeam(Player player, String id) {
        Map<String, PacketScoreboardTeam> map = playerTeams.get(player.getUniqueId());
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

    /**
     * Creates a new per-player scoreboard objective
     *
     * @param player the player to make the objective for
     * @param id the id of the objective
     * @param title the initial title of the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     * @throws IllegalStateException if an objective with the same id already exists
     */
    public PacketScoreboardObjective createPlayerObjective(Player player, String id, String title, DisplaySlot displaySlot) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");
        Map<String, PacketScoreboardObjective> objectives = playerObjectives.computeIfAbsent(player.getUniqueId(), p -> new HashMap<>());
        Preconditions.checkState(!objectives.containsKey(id), "id already exists");

        PacketScoreboardObjective objective = new PacketScoreboardObjective(this, id, title, displaySlot);
        objective.subscribe(player);
        objectives.put(id, objective);

        return objective;
    }

    /**
     * Creates a new per-player scoreboard objective with an automatically generated id
     *
     * @param player the player to make the objective for
     * @param title the initial title of the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     */
    public PacketScoreboardObjective createPlayerObjective(Player player, String title, DisplaySlot displaySlot) {
        return createPlayerObjective(player, Long.toHexString(System.nanoTime()), title, displaySlot);
    }

    /**
     * Gets an existing per-player scoreboard objective if one with the id exists
     *
     * @param player the player to get the objective for
     * @param id the id of the objective
     * @return the objective, if present, otherwise null
     */
    public PacketScoreboardObjective getPlayerObjective(Player player, String id) {
        Map<String, PacketScoreboardObjective> map = playerObjectives.get(player.getUniqueId());
        if (map == null) {
            return null;
        }

        return map.get(id);
    }

    /**
     * Removes a per-player scoreboard objective from this scoreboard
     *
     * @param player the player to remove the objective for
     * @param id the id of the objective
     * @return true if the objective was removed successfully
     */
    public boolean removePlayerObjective(Player player, String id) {
        Map<String, PacketScoreboardObjective> map = playerObjectives.get(player.getUniqueId());
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

    /**
     * Sends a packet to a player, absorbing any exceptions thrown in the process
     *
     * @param packet the packet to send
     * @param player the player to send the packet to
     */
    void sendPacket(PacketContainer packet, Player player) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a packet to an iterable of players
     *
     * @param players the players to send the packet to
     * @param packet the packet to send
     */
    void broadcastPacket(Iterable<Player> players, PacketContainer packet) {
        for (Player player : players) {
            sendPacket(packet, player);
        }
    }

}
