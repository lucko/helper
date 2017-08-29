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

import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nonnull;

/**
 * Represents a Scoreboard on the server
 */
@NonnullByDefault
public interface Scoreboard {

    /**
     * Gets the global scoreboard instance.
     *
     * @return the global scoreboard instance
     */
    @Nonnull
    static PacketScoreboard get() {
        return GlobalScoreboard.get();
    }

    /**
     * Creates a new scoreboard team
     *
     * @param id the id of the team
     * @param title the initial title for the team
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    ScoreboardTeam createTeam(String id, String title);

    /**
     * Creates a new scoreboard team with an automatically generated id
     *
     * @param title the initial title for the team
     * @return the new team
     */
    ScoreboardTeam createTeam(String title);

    /**
     * Gets an existing scoreboard team if one with the id exists
     *
     * @param id the id of the team
     * @return the team, if present, otherwise null
     */
    ScoreboardTeam getTeam(String id);

    /**
     * Removes a scoreboard team from this scoreboard
     *
     * @param id the id of the team
     * @return true if the team was removed successfully
     */
    boolean removeTeam(String id);

    /**
     * Creates a new scoreboard objective
     *
     * @param id the id of the objective
     * @param title the initial title for the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     * @throws IllegalStateException if an objective with the same id already exists
     */
    ScoreboardObjective createObjective(String id, String title, DisplaySlot displaySlot);

    /**
     * Creates a new scoreboard objective with an automatically generated id
     *
     * @param title the initial title for the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     */
    ScoreboardObjective createObjective(String title, DisplaySlot displaySlot);

    /**
     * Gets an existing scoreboard objective if one with the id exists
     *
     * @param id the id of the objective
     * @return the objective, if present, otherwise null
     */
    ScoreboardObjective getObjective(String id);

    /**
     * Removes a scoreboard objective from this scoreboard
     *
     * @param id the id of the objective
     * @return true if the objective was removed successfully
     */
    boolean removeObjective(String id);

    /**
     * Creates a new per-player scoreboard team
     *
     * @param player the player to make the team for
     * @param id the id of the team
     * @param title the initial title of the team
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    ScoreboardTeam createPlayerTeam(Player player, String id, String title);

    /**
     * Creates a new per-player scoreboard team with an automatically generated id
     *
     * @param player the player to make the team for
     * @param title the initial title of the team
     * @return the new team
     */
    ScoreboardTeam createPlayerTeam(Player player, String title);

    /**
     * Gets an existing per-player scoreboard team if one with the id exists
     *
     * @param player the player to get the team for
     * @param id the id of the team
     * @return the team, if present, otherwise null
     */
    ScoreboardTeam getPlayerTeam(Player player, String id);

    /**
     * Removes a per-player scoreboard team from this scoreboard
     *
     * @param player the player to remove the team for
     * @param id the id of the team
     * @return true if the team was removed successfully
     */
    boolean removePlayerTeam(Player player, String id);

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
    ScoreboardObjective createPlayerObjective(Player player, String id, String title, DisplaySlot displaySlot);

    /**
     * Creates a new per-player scoreboard objective with an automatically generated id
     *
     * @param player the player to make the objective for
     * @param title the initial title of the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     */
    ScoreboardObjective createPlayerObjective(Player player, String title, DisplaySlot displaySlot);

    /**
     * Gets an existing per-player scoreboard objective if one with the id exists
     *
     * @param player the player to get the objective for
     * @param id the id of the objective
     * @return the objective, if present, otherwise null
     */
    ScoreboardObjective getPlayerObjective(Player player, String id);

    /**
     * Removes a per-player scoreboard objective from this scoreboard
     *
     * @param player the player to remove the objective for
     * @param id the id of the objective
     * @return true if the objective was removed successfully
     */
    boolean removePlayerObjective(Player player, String id);

}
