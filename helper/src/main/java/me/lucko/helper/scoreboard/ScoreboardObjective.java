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

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a specific objective on a {@link Scoreboard}.
 */
public interface ScoreboardObjective {

    /**
     * Gets the id of this objective
     *
     * @return the id
     */
    String getId();

    /**
     * Gets the current display name of this objective
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Lazily sets the display name to a new value and updates the objectives subscribers
     *
     * @param displayName the new display name
     */
    void setDisplayName(String displayName);

    /**
     * Gets the current display slot of this objective
     *
     * @return the display slot
     */
    DisplaySlot getDisplaySlot();

    /**
     * Lazily sets the display slot to a new value and updates the objectives subscribers
     *
     * @param displaySlot the new display slot
     */
    void setDisplaySlot(DisplaySlot displaySlot);

    /**
     * Gets an immutable copy of the current objective scores
     *
     * @return the current scores
     */
    Map<String, Integer> getScores();

    /**
     * Returns true if this objective has a given score
     *
     * @param name the name of the score to check for
     * @return true if the objective has the score
     */
    boolean hasScore(String name);

    /**
     * Gets the value mapped to a given score, if present
     *
     * @param name the name of the score
     * @return the value, or null if a mapping could not be found
     */
    Integer getScore(String name);

    /**
     * Sets a new score value
     *
     * @param name the name of the score
     * @param value the value to set the score to
     */
    void setScore(String name, int value);

    /**
     * Removes a score
     *
     * @param name the name of the score
     * @return true if the score was removed
     */
    boolean removeScore(String name);

    /**
     * Clears the scores from this objective
     */
    void clearScores();

    /**
     * Applies a score mapping to this objective
     *
     * @param scores the scores to apply
     */
    void applyScores(Map<String, Integer> scores);

    /**
     * Automatically applies a set of score lines to this objective.
     *
     * @param lines the lines to apply
     */
    void applyLines(String... lines);

    /**
     * Automatically applies a set of score lines to this objective.
     *
     * @param lines the lines to apply
     */
    void applyLines(Collection<String> lines);

    /**
     * Subscribes a player to this objective
     *
     * @param player the player to subscribe
     */
    void subscribe(Player player);

    /**
     * Unsubscribes a player from this objective
     *
     * @param player the player to unsubscribe
     */
    void unsubscribe(Player player);

    /**
     * Unsubscribes a player from this objective
     *
     * @param player the player to unsubscribe
     * @param fast if true, the removal packet will not be sent (for use when the player is leaving)
     */
    void unsubscribe(Player player, boolean fast);

    /**
     * Unsubscribes all players from this objective
     */
    void unsubscribeAll();
}
