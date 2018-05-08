/*
 * This file is part of helper, licensed under the MIScoreboardTeam License.
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
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUScoreboardTeam WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUScoreboardTeam NOScoreboardTeam LIMITED TScoreboardObjective THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NScoreboardObjective EVENScoreboardTeam SHALL THE
 *  AUTHORS OR COPYRIGHScoreboardTeam HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORScoreboardTeam OR OTHERWISE, ARISING FROM,
 *  OUScoreboardTeam OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package me.lucko.helper.scoreboard;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import javax.annotation.Nullable;

/**
 * Represents a Scoreboard on the server
 */
@NonnullByDefault
public interface Scoreboard {

    /**
     * Creates a new scoreboard team
     *
     * @param id the id of the team
     * @param title the initial title for the team
     * @param autoSubscribe if players should be automatically subscribed
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    ScoreboardTeam createTeam(String id, String title, boolean autoSubscribe);

    /**
     * Creates a new scoreboard team
     *
     * @param id the id of the team
     * @param title the initial title for the team
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    default ScoreboardTeam createTeam(String id, String title) {
        return createTeam(id, title, true);
    }

    /**
     * Creates a new scoreboard team with an automatically generated id
     *
     * @param title the initial title for the team
     * @param autoSubscribe if players should be automatically subscribed
     * @return the new team
     */
    default ScoreboardTeam createTeam(String title, boolean autoSubscribe) {
        return createTeam(Long.toHexString(System.nanoTime()), title, autoSubscribe);
    }

    /**
     * Creates a new scoreboard team with an automatically generated id
     *
     * @param title the initial title for the team
     * @return the new team
     */
    default ScoreboardTeam createTeam(String title) {
        return createTeam(title, true);
    }

    /**
     * Gets an existing scoreboard team if one with the id exists
     *
     * @param id the id of the team
     * @return the team, if present, otherwise null
     */
    @Nullable
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
     * @param autoSubscribe if players should be automatically subscribed
     * @return the new objective
     * @throws IllegalStateException if an objective with the same id already exists
     */
    ScoreboardObjective createObjective(String id, String title, DisplaySlot displaySlot, boolean autoSubscribe);

    /**
     * Creates a new scoreboard objective
     *
     * @param id the id of the objective
     * @param title the initial title for the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     * @throws IllegalStateException if an objective with the same id already exists
     */
    default ScoreboardObjective createObjective(String id, String title, DisplaySlot displaySlot) {
        return createObjective(id, title, displaySlot, true);
    }

    /**
     * Creates a new scoreboard objective with an automatically generated id
     *
     * @param title the initial title for the objective
     * @param displaySlot the display slot to use for this objective
     * @param autoSubscribe if players should be automatically subscribed
     * @return the new objective
     */
    default ScoreboardObjective createObjective(String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        return createObjective(Long.toHexString(System.nanoTime()), title, displaySlot, autoSubscribe);
    }

    /**
     * Creates a new scoreboard objective with an automatically generated id
     *
     * @param title the initial title for the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     */
    default ScoreboardObjective createObjective(String title, DisplaySlot displaySlot) {
        return createObjective(title, displaySlot, true);
    }

    /**
     * Gets an existing scoreboard objective if one with the id exists
     *
     * @param id the id of the objective
     * @return the objective, if present, otherwise null
     */
    @Nullable
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
     * @param autoSubscribe if players should be automatically subscribed
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    ScoreboardTeam createPlayerTeam(Player player, String id, String title, boolean autoSubscribe);

    /**
     * Creates a new per-player scoreboard team
     *
     * @param player the player to make the team for
     * @param id the id of the team
     * @param title the initial title of the team
     * @return the new team
     * @throws IllegalStateException if a team with the same id already exists
     */
    default ScoreboardTeam createPlayerTeam(Player player, String id, String title) {
        return createPlayerTeam(player, id, title, true);
    }

    /**
     * Creates a new per-player scoreboard team with an automatically generated id
     *
     * @param player the player to make the team for
     * @param title the initial title of the team
     * @param autoSubscribe if players should be automatically subscribed
     * @return the new team
     */
    default ScoreboardTeam createPlayerTeam(Player player, String title, boolean autoSubscribe) {
        return createPlayerTeam(player, Long.toHexString(System.nanoTime()), title, autoSubscribe);
    }

    /**
     * Creates a new per-player scoreboard team with an automatically generated id
     *
     * @param player the player to make the team for
     * @param title the initial title of the team
     * @return the new team
     */
    default ScoreboardTeam createPlayerTeam(Player player, String title) {
        return createPlayerTeam(player, title, true);
    }

    /**
     * Gets an existing per-player scoreboard team if one with the id exists
     *
     * @param player the player to get the team for
     * @param id the id of the team
     * @return the team, if present, otherwise null
     */
    @Nullable
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
     * @param autoSubscribe if players should be automatically subscribed
     * @return the new objective
     * @throws IllegalStateException if an objective with the same id already exists
     */
    ScoreboardObjective createPlayerObjective(Player player, String id, String title, DisplaySlot displaySlot, boolean autoSubscribe);

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
    default ScoreboardObjective createPlayerObjective(Player player, String id, String title, DisplaySlot displaySlot) {
        return createPlayerObjective(player, id, title, displaySlot, true);
    }

    /**
     * Creates a new per-player scoreboard objective with an automatically generated id
     *
     * @param player the player to make the objective for
     * @param title the initial title of the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     */
    default ScoreboardObjective createPlayerObjective(Player player, String title, DisplaySlot displaySlot, boolean autoSubscribe) {
        return createPlayerObjective(player, Long.toHexString(System.nanoTime()), title, displaySlot, autoSubscribe);
    }

    /**
     * Creates a new per-player scoreboard objective with an automatically generated id
     *
     * @param player the player to make the objective for
     * @param title the initial title of the objective
     * @param displaySlot the display slot to use for this objective
     * @return the new objective
     */
    default ScoreboardObjective createPlayerObjective(Player player, String title, DisplaySlot displaySlot) {
        return createPlayerObjective(player, title, displaySlot, true);
    }

    /**
     * Gets an existing per-player scoreboard objective if one with the id exists
     *
     * @param player the player to get the objective for
     * @param id the id of the objective
     * @return the objective, if present, otherwise null
     */
    @Nullable
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
