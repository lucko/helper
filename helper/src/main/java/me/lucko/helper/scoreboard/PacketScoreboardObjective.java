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
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import me.lucko.helper.utils.Color;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Wrapper for PacketPlayOutScoreboardObjective, PacketPlayOutScoreboardScore and PacketPlayOutScoreboardDisplayObjective
 *
 * <p>http://wiki.vg/Protocol#Scoreboard_Objective</p>
 * <p>http://wiki.vg/Protocol#Update_Score</p>
 * <p>http://wiki.vg/Protocol#Display_Scoreboard</p>
 */
@NonnullByDefault
public class PacketScoreboardObjective implements ScoreboardObjective {
    // the objective value in the ScoreboardObjective packet is limited to 32 chars
    private static final int MAX_NAME_LENGTH = 32;

    /**
     * Trims a objective name to the max length of {@link #MAX_NAME_LENGTH}
     *
     * @param name the name to trim
     * @return a trimmed version of name
     */
    private static String trimName(String name) {
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

    private static final int MODE_CREATE = 0;
    private static final int MODE_REMOVE = 1;
    private static final int MODE_UPDATE = 2;

    // the parent scoreboard
    private final PacketScoreboard scoreboard;
    // the id of this objective
    private final String id;

    // the current scores being shown
    private final Map<String, Integer> scores = Collections.synchronizedMap(new HashMap<>());
    // a set of the players subscribed to & receiving updates for this objective
    private final Set<Player> subscribed = Collections.synchronizedSet(new HashSet<>());

    // the current display name
    private String displayName;
    // the current display slot
    private DisplaySlot displaySlot;

    /**
     * Creates a new scoreboard objective
     *
     * @param scoreboard the parent scoreboard
     * @param id the id of this objective
     * @param displayName the initial display name
     * @param displaySlot the initial display slot
     */
    public PacketScoreboardObjective(PacketScoreboard scoreboard, String id, String displayName, DisplaySlot displaySlot) {
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");

        this.scoreboard = Preconditions.checkNotNull(scoreboard, "scoreboard");
        this.id = Preconditions.checkNotNull(id, "id");
        this.displayName = Color.colorize(Preconditions.checkNotNull(displayName, "displayName"));
        this.displaySlot = Preconditions.checkNotNull(displaySlot, "displaySlot");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        Preconditions.checkNotNull(displayName, "displayName");
        displayName = Color.colorize(displayName);
        if (this.displayName.equals(displayName)) {
            return;
        }

        this.displayName = displayName;
        scoreboard.broadcastPacket(subscribed, newObjectivePacket(MODE_UPDATE));
    }

    @Override
    public DisplaySlot getDisplaySlot() {
        return displaySlot;
    }

    @Override
    public void setDisplaySlot(DisplaySlot displaySlot) {
        Preconditions.checkNotNull(displaySlot, "displaySlot");
        if (this.displaySlot.equals(displaySlot)) {
            return;
        }

        this.displaySlot = displaySlot;
        scoreboard.broadcastPacket(subscribed, newDisplaySlotPacket(displaySlot));
    }

    @Override
    public Map<String, Integer> getScores() {
        return ImmutableMap.copyOf(scores);
    }

    @Override
    public boolean hasScore(String name) {
        Preconditions.checkNotNull(name, "name");
        return scores.containsKey(Color.colorize(trimName(name)));
    }

    @Nullable
    @Override
    public Integer getScore(String name) {
        Preconditions.checkNotNull(name, "name");
        return scores.get(Color.colorize(trimName(name)));
    }

    @Override
    public void setScore(String name, int value) {
        Preconditions.checkNotNull(name, "name");
        name = trimName(name);

        Integer oldValue = scores.put(name, value);
        if (oldValue != null && oldValue == value) {
            return;
        }

        scoreboard.broadcastPacket(subscribed, newScorePacket(name, value, false));
    }

    @Override
    public boolean removeScore(String name) {
        Preconditions.checkNotNull(name, "name");
        name = trimName(name);

        if (scores.remove(name) == null) {
            return false;
        }

        scoreboard.broadcastPacket(subscribed, newScorePacket(name, 0, true));
        return true;
    }

    @Override
    public void clearScores() {
        scores.clear();

        scoreboard.broadcastPacket(subscribed, newObjectivePacket(MODE_REMOVE));
        for (Player player : subscribed) {
            subscribe(player);
        }
    }

    @Override
    public void applyScores(Map<String, Integer> scores) {
        Preconditions.checkNotNull(scores, "scores");

        Set<String> toRemove = new HashSet<>(getScores().keySet());
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            toRemove.remove(Color.colorize(trimName(score.getKey())));
        }
        for (String name : toRemove) {
            removeScore(name);
        }
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            setScore(Color.colorize(score.getKey()), score.getValue());
        }
    }

    @Override
    public void applyLines(String... lines) {
        applyLines(Arrays.asList(lines));
    }

    @Override
    public void applyLines(Collection<String> lines) {
        Preconditions.checkNotNull(lines, "lines");
        Map<String, Integer> scores = new HashMap<>();
        int i = lines.size();
        for (String line : lines) {
            scores.put(line, i--);
        }
        applyScores(scores);
    }

    @Override
    public void subscribe(Player player) {
        Preconditions.checkNotNull(player, "player");
        scoreboard.sendPacket(newObjectivePacket(MODE_CREATE), player);
        scoreboard.sendPacket(newDisplaySlotPacket(getDisplaySlot()), player);
        for (Map.Entry<String, Integer> score : getScores().entrySet()) {
            scoreboard.sendPacket(newScorePacket(score.getKey(), score.getValue(), false), player);
        }
        subscribed.add(player);
    }

    @Override
    public void unsubscribe(Player player) {
        unsubscribe(player, false);
    }

    @Override
    public void unsubscribe(Player player, boolean fast) {
        Preconditions.checkNotNull(player, "player");
        if (!subscribed.remove(player) || fast) {
            return;
        }

        scoreboard.sendPacket(newObjectivePacket(MODE_REMOVE), player);
    }

    @Override
    public void unsubscribeAll() {
        scoreboard.broadcastPacket(subscribed, newObjectivePacket(MODE_REMOVE));
        subscribed.clear();
    }

    private PacketContainer newObjectivePacket(int mode) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);

        // set name
        packet.getStrings().write(0, getId());

        // set display name
        packet.getStrings().write(1, getDisplayName());

        // set health display
        packet.getEnumModifier(HealthDisplay.class, 2).write(0, HealthDisplay.INTEGER);

        // set mode
        packet.getIntegers().write(0, mode);

        return packet;
    }

    private PacketContainer newScorePacket(String name, int value, boolean remove) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);

        // set name
        packet.getStrings().write(0, name);

        // set objective name
        packet.getStrings().write(1, getId());

        // set value
        packet.getIntegers().write(0, value);

        if (remove) {
            packet.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.REMOVE);
        } else {
            packet.getScoreboardActions().write(0, EnumWrappers.ScoreboardAction.CHANGE);
        }

        return packet;
    }

    private PacketContainer newDisplaySlotPacket(DisplaySlot displaySlot) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);

        // set position
        int slot;
        switch (displaySlot) {
            case PLAYER_LIST:
                slot = 0;
                break;
            case SIDEBAR:
                slot = 1;
                break;
            case BELOW_NAME:
                slot = 2;
                break;
            default:
                throw new IllegalStateException();
        }

        packet.getIntegers().write(0, slot);

        // set objective name
        packet.getStrings().write(0, getId());

        return packet;
    }

    private enum HealthDisplay {
        INTEGER, HEARTS
    }
}
