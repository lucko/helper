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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Set;

/**
 * Represents a specific team on a {@link Scoreboard}.
 */
@NonnullByDefault
public interface ScoreboardTeam {

    /**
     * Gets the id of this team
     *
     * @return the id
     */
    String getId();

    /**
     * Gets if players should be automatically subscribed to this team.
     *
     * @return true if players should be automatically subscribed
     */
    boolean shouldAutoSubscribe();

    /**
     * Sets if players should be automatically subscribed to this team.
     *
     * @param autoSubscribe if players should be automatically subscribed
     */
    void setAutoSubscribe(boolean autoSubscribe);

    /**
     * Gets the current display name of this team
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Lazily sets the display name to a new value and updates the teams subscribers
     *
     * @param displayName the new display name
     */
    void setDisplayName(String displayName);

    /**
     * Gets the current prefix for this team
     *
     * @return the prefix
     */
    String getPrefix();

    /**
     * Lazily sets the prefix to a new value and updates the teams subscribers
     *
     * @param prefix the new prefix
     */
    void setPrefix(String prefix);

    /**
     * Gets the current suffix for this team
     *
     * @return the suffix
     */
    String getSuffix();

    /**
     * Lazily sets the suffix to a new value and updates the teams subscribers
     *
     * @param suffix the new suffix
     */
    void setSuffix(String suffix);

    /**
     * Gets if friendly fire is allowed
     *
     * @return true if friendly fire is allowed
     */
    boolean isAllowFriendlyFire();

    /**
     * Lazily sets the friendly fire setting to new value and updates the teams subscribers
     *
     * @param allowFriendlyFire the new setting
     */
    void setAllowFriendlyFire(boolean allowFriendlyFire);

    /**
     * Gets if members of this team can see invisible members on the same team
     *
     * @return true if members of this team can see invisible members on the same team
     */
    boolean isCanSeeFriendlyInvisibles();

    /**
     * Lazily sets the friendly invisibility setting to new value and updates the teams subscribers
     *
     * @param canSeeFriendlyInvisibles the new setting
     */
    void setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles);

    /**
     * Gets the current nametag visibility setting
     *
     * @return the nametag visibility
     */
    NameTagVisibility getNameTagVisibility();

    /**
     * Lazily sets the nametag visibility setting to new value and updates the teams subscribers
     *
     * @param nameTagVisibility the new setting
     */
    void setNameTagVisibility(NameTagVisibility nameTagVisibility);

    /**
     * Gets the current collision rule setting
     *
     * @return the collision rule
     */
    CollisionRule getCollisionRule();

    /**
     * Lazily sets the collision rule setting to new value and updates the teams subscribers
     *
     * @param collisionRule the new setting
     */
    void setCollisionRule(CollisionRule collisionRule);

    /**
     * Gets the team color. - {@link ChatColor#RESET} by default.
     *
     * @return the team color
     */
    ChatColor getColor();

    /**
     * Sets the teams color.
     *
     * @param color the new color
     */
    void setColor(ChatColor color);

    /**
     * Adds a player to this team
     *
     * @param player the player to add
     * @return true if the player was added successfully
     */
    boolean addPlayer(String player);

    /**
     * Removes a player from this team
     *
     * @param player the player to remove
     * @return true if the player was removed successfully
     */
    boolean removePlayer(String player);

    /**
     * Returns true if the given player is a member of this team
     *
     * @param player the player to check for
     * @return true if the player is a member
     */
    boolean hasPlayer(String player);

    /**
     * Adds a player to this team
     *
     * @param player the player to add
     * @return true if the player was added successfully
     */
    default boolean addPlayer(Player player) {
        return addPlayer(Objects.requireNonNull(player, "player").getName());
    }

    /**
     * Removes a player from this team
     *
     * @param player the player to remove
     * @return true if the player was removed successfully
     */
    default boolean removePlayer(Player player) {
        return removePlayer(Objects.requireNonNull(player, "player").getName());
    }

    /**
     * Returns true if the given player is a member of this team
     *
     * @param player the player to check for
     * @return true if the player is a member
     */
    default boolean hasPlayer(Player player) {
        return hasPlayer(Objects.requireNonNull(player, "player").getName());
    }

    /**
     * Gets an immutable copy of the teams members
     *
     * @return the team members
     */
    Set<String> getPlayers();

    /**
     * Subscribes a player to this team
     *
     * @param player the player to subscribe
     */
    void subscribe(Player player);

    /**
     * Unsubscribes a player from this team
     *
     * @param player the player to unsubscribe
     */
    void unsubscribe(Player player);

    /**
     * Unsubscribes a player from this team
     *
     * @param player the player to unsubscribe
     * @param fast if true, the removal packet will not be sent (for use when the player is leaving)
     */
    void unsubscribe(Player player, boolean fast);

    /**
     * Unsubscribes all players from this team
     */
    void unsubscribeAll();

    enum CollisionRule {
        ALWAYS("always"),
        NEVER("never"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam");

        private final String protocolName;

        CollisionRule(String protocolName) {
            this.protocolName = protocolName;
        }

        public String getProtocolName() {
            return this.protocolName;
        }
    }

    enum NameTagVisibility {
        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private final String protocolName;

        NameTagVisibility(String protocolName) {
            this.protocolName = protocolName;
        }

        public String getProtocolName() {
            return this.protocolName;
        }
    }

}
