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

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import me.lucko.helper.utils.Color;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Packet wrapper for PacketPlayOutScoreboardTeam
 *
 * http://wiki.vg/Protocol#Teams
 */
public class PacketScoreboardTeam {
    private static final int MODE_CREATE = 0;
    private static final int MODE_REMOVE = 1;
    private static final int MODE_UPDATE = 2;
    private static final int MODE_ADD_PLAYERS = 3;
    private static final int MODE_REMOVE_PLAYERS = 4;

    // the name value in the Teams packet is limited to 16 chars
    private static final int MAX_PREFIX_LENGTH = 16;
    private static final int MAX_SUFFIX_LENGTH = MAX_PREFIX_LENGTH;

    // the parent scoreboard
    private final PacketScoreboard scoreboard;
    // the id of this team
    private final String id;

    // the members of this team
    private final Set<Player> players = Collections.synchronizedSet(new HashSet<>());
    // a set of the players subscribed to & receiving updates for this team
    private final Set<Player> subscribed = Collections.synchronizedSet(new HashSet<>());

    // the current display name
    private String displayName;
    // the current prefix
    private String prefix = "";
    // the current suffix
    private String suffix = "";
    // if friendly fire is allowed
    private boolean allowFriendlyFire = true;
    // if members of this team can see invisible members on the same team
    private boolean canSeeFriendlyInvisibles = true;
    // the current nametag visibility
    private NameTagVisibility nameTagVisibility = NameTagVisibility.ALWAYS;
    // the current collision rule
    private CollisionRule collisionRule = CollisionRule.ALWAYS;

    /**
     * Creates a new scoreboard team
     *
     * @param scoreboard the parent scoreboard
     * @param id the id of this team
     * @param displayName the initial display name
     */
    public PacketScoreboardTeam(PacketScoreboard scoreboard, String id, String displayName) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");

        this.scoreboard = Preconditions.checkNotNull(scoreboard, "scoreboard");
        this.id = Preconditions.checkNotNull(id, "id");
        this.displayName = Color.colorize(Preconditions.checkNotNull(displayName, "displayName"));
    }

    /**
     * Gets the id of this team
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the current display name of this team
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Lazily sets the display name to a new value and updates the teams subscribers
     *
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        Preconditions.checkNotNull(displayName, "displayName");
        displayName = Color.colorize(displayName);
        if (this.displayName.equals(displayName)) {
            return;
        }

        this.displayName = displayName;
        scoreboard.broadcastPacket(subscribed, newUpdatePacket());
    }

    /**
     * Gets the current prefix for this team
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Lazily sets the prefix to a new value and updates the teams subscribers
     *
     * @param prefix the new prefix
     */
    public void setPrefix(String prefix) {
        Preconditions.checkNotNull(prefix, "prefix");
        prefix = Color.colorize(prefix);
        if (prefix.length() > MAX_PREFIX_LENGTH) {
            prefix = prefix.substring(0, MAX_PREFIX_LENGTH);
        }
        if (this.prefix.equals(prefix)) {
            return;
        }

        this.prefix = prefix;
        scoreboard.broadcastPacket(subscribed, newUpdatePacket());
    }

    /**
     * Gets the current suffix for this team
     *
     * @return the suffix
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Lazily sets the suffix to a new value and updates the teams subscribers
     *
     * @param suffix the new suffix
     */
    public void setSuffix(String suffix) {
        Preconditions.checkNotNull(suffix, "suffix");
        suffix = Color.colorize(suffix);
        if (suffix.length() > MAX_SUFFIX_LENGTH) {
            suffix = suffix.substring(0, MAX_SUFFIX_LENGTH);
        }
        if (this.suffix.equals(suffix)) {
            return;
        }

        this.suffix = suffix;
        scoreboard.broadcastPacket(subscribed, newUpdatePacket());
    }

    /**
     * Gets if friendly fire is allowed
     *
     * @return true if friendly fire is allowed
     */
    public boolean isAllowFriendlyFire() {
        return allowFriendlyFire;
    }

    /**
     * Lazily sets the friendly fire setting to new value and updates the teams subscribers
     *
     * @param allowFriendlyFire the new setting
     */
    public void setAllowFriendlyFire(boolean allowFriendlyFire) {
        if (this.allowFriendlyFire == allowFriendlyFire) {
            return;
        }

        this.allowFriendlyFire = allowFriendlyFire;
        scoreboard.broadcastPacket(subscribed, newUpdatePacket());
    }

    /**
     * Gets if members of this team can see invisible members on the same team
     *
     * @return true if members of this team can see invisible members on the same team
     */
    public boolean isCanSeeFriendlyInvisibles() {
        return canSeeFriendlyInvisibles;
    }

    /**
     * Lazily sets the friendly invisibility setting to new value and updates the teams subscribers
     *
     * @param canSeeFriendlyInvisibles the new setting
     */
    public void setCanSeeFriendlyInvisibles(boolean canSeeFriendlyInvisibles) {
        if (this.canSeeFriendlyInvisibles == canSeeFriendlyInvisibles) {
            return;
        }

        this.canSeeFriendlyInvisibles = canSeeFriendlyInvisibles;
        scoreboard.broadcastPacket(subscribed, newUpdatePacket());
    }

    /**
     * Gets the current nametag visibility setting
     *
     * @return the nametag visibility
     */
    public NameTagVisibility getNameTagVisibility() {
        return nameTagVisibility;
    }

    /**
     * Lazily sets the nametag visibility setting to new value and updates the teams subscribers
     *
     * @param nameTagVisibility the new setting
     */
    public void setNameTagVisibility(NameTagVisibility nameTagVisibility) {
        Preconditions.checkNotNull(nameTagVisibility, "nameTagVisibility");
        if (this.nameTagVisibility.equals(nameTagVisibility)) {
            return;
        }

        this.nameTagVisibility = nameTagVisibility;
        scoreboard.broadcastPacket(subscribed, newUpdatePacket());
    }

    /**
     * Gets the current collision rule setting
     *
     * @return the collision rule
     */
    public CollisionRule getCollisionRule() {
        return collisionRule;
    }

    /**
     * Lazily sets the collision rule setting to new value and updates the teams subscribers
     *
     * @param collisionRule the new setting
     */
    public void setCollisionRule(CollisionRule collisionRule) {
        Preconditions.checkNotNull(collisionRule, "collisionRule");
        if (this.collisionRule.equals(collisionRule)) {
            return;
        }

        this.collisionRule = collisionRule;
        scoreboard.broadcastPacket(subscribed, newUpdatePacket());
    }

    /**
     * Adds a player to this team
     *
     * @param player the player to add
     * @return true if the player was added successfully
     */
    public boolean addPlayer(Player player) {
        Preconditions.checkNotNull(player, "player");
        if (!players.add(player)) {
            return false;
        }

        scoreboard.broadcastPacket(subscribed, newPlayerPacket(player, false));
        return true;
    }

    /**
     * Removes a player from this team
     *
     * @param player the player to remove
     * @return true if the player was removed successfully
     */
    public boolean removePlayer(Player player) {
        Preconditions.checkNotNull(player, "player");
        if (!players.remove(player)) {
            return false;
        }

        scoreboard.broadcastPacket(subscribed, newPlayerPacket(player, true));
        return true;
    }

    /**
     * Returns true if the given player is a member of this team
     *
     * @param player the player to check for
     * @return true if the player is a member
     */
    public boolean hasPlayer(Player player) {
        Preconditions.checkNotNull(player, "player");
        return players.contains(player);
    }

    /**
     * Gets an immutable copy of the teams members
     *
     * @return the team members
     */
    public Set<Player> getPlayers() {
        return ImmutableSet.copyOf(players);
    }

    /**
     * Subscribes a player to this team
     *
     * @param player the player to subscribe
     */
    public void subscribe(Player player) {
        scoreboard.sendPacket(newCreatePacket(), player);
        subscribed.add(player);
    }

    /**
     * Unsubscribes a player from this team
     *
     * @param player the player to unsubscribe
     */
    public void unsubscribe(Player player) {
        unsubscribe(player, false);
    }

    /**
     * Unsubscribes a player from this team
     *
     * @param player the player to unsubscribe
     * @param fast if true, the removal packet will not be sent (for use when the player is leaving)
     */
    public void unsubscribe(Player player, boolean fast) {
        if (!subscribed.remove(player) || fast) {
            return;
        }

        scoreboard.sendPacket(newRemovePacket(), player);
    }

    /**
     * Unsubscribes all players from this team
     */
    public void unsubscribeAll() {
        scoreboard.broadcastPacket(subscribed, newRemovePacket());
        subscribed.clear();
    }

    private PacketContainer newCreatePacket() {
        PacketContainer packet = newUpdatePacket();
        
        // get players
        List<String> players = new ArrayList<>();
        for (Player player : getPlayers()) {
            players.add(player.getName());
        }

        // set players
        packet.getSpecificModifier(Collection.class).write(0, players);

        // set mode
        packet.getIntegers().write(1, MODE_CREATE);
        
        return packet;
    }

    private PacketContainer newRemovePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);

        // set name
        packet.getStrings().write(0, getId());

        // set mode
        packet.getIntegers().write(1, MODE_REMOVE);

        return packet;
    }

    private PacketContainer newUpdatePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);

        // set name
        packet.getStrings().write(0, getId());

        // set display name
        packet.getStrings().write(1, getDisplayName());

        // set prefix
        packet.getStrings().write(2, getPrefix());

        // set suffix
        packet.getStrings().write(3, getSuffix());

        // set nametag visibility
        packet.getStrings().write(4, getNameTagVisibility().getProtocolName());

        // set collision rule
        packet.getStrings().write(5, getCollisionRule().getProtocolName());

        // set color
        packet.getIntegers().write(0, -1); // ChatColor RESET

        // set mode
        packet.getIntegers().write(1, MODE_UPDATE);

        // pack option data
        int data = 0;
        if (isAllowFriendlyFire()) {
            data |= 1;
        }
        if (isCanSeeFriendlyInvisibles()) {
            data |= 2;
        }

        // set pack data
        packet.getIntegers().write(2, data);
        
        return packet;
    }

    private PacketContainer newPlayerPacket(Player player, boolean remove) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);

        // set name
        packet.getStrings().write(0, getId());

        // set players
        packet.getSpecificModifier(Collection.class).write(0, Collections.singletonList(player.getName()));

        // set mode
        if (remove) {
            packet.getIntegers().write(1, MODE_REMOVE_PLAYERS);
        } else {
            packet.getIntegers().write(1, MODE_ADD_PLAYERS);
        }
        
        return packet;
    }

    public enum CollisionRule {
        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("pushOtherTeams"),
        HIDE_FOR_OWN_TEAM("pushOwnTeam");

        private final String protocolName;

        CollisionRule(String protocolName) {
            this.protocolName = protocolName;
        }

        public String getProtocolName() {
            return protocolName;
        }
    }

    public enum NameTagVisibility {
        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private final String protocolName;

        NameTagVisibility(String protocolName) {
            this.protocolName = protocolName;
        }

        public String getProtocolName() {
            return protocolName;
        }
    }

}
