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
import com.comphenix.protocol.wrappers.EnumWrappers.ScoreboardAction;
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
 * Implements {@link ScoreboardObjective} using ProtocolLib.
 */
@NonnullByDefault
public class PacketScoreboardObjective implements ScoreboardObjective {

    // the "Entity name" in the Update Score packet is limited to 40 chars
    private static final int MAX_SCORE_LENGTH = 40;
    private static String trimScore(String name) {
        return name.length() > MAX_SCORE_LENGTH ? name.substring(0, MAX_SCORE_LENGTH) : name;
    }

    // the "Objective Value" in the ScoreboardObjective packet is limited to 32 chars
    private static final int MAX_NAME_LENGTH = 32;
    private static String trimName(String name) {
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

    // the parent scoreboard
    private final PacketScoreboard scoreboard;
    // the id of this objective
    private final String id;
    // if players should be automatically subscribed
    private boolean autoSubscribe;

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
     * @param autoSubscribe if players should be automatically subscribed
     */
    public PacketScoreboardObjective(PacketScoreboard scoreboard, String id, String displayName, DisplaySlot displaySlot, boolean autoSubscribe) {
        Preconditions.checkNotNull(id, "id");
        Preconditions.checkArgument(id.length() <= 16, "id cannot be longer than 16 characters");

        this.scoreboard = Preconditions.checkNotNull(scoreboard, "scoreboard");
        this.id = id;
        this.displayName = trimName(Color.colorize(Preconditions.checkNotNull(displayName, "displayName")));
        this.displaySlot = Preconditions.checkNotNull(displaySlot, "displaySlot");
        this.autoSubscribe = autoSubscribe;
    }

    /**
     * Creates a new scoreboard objective
     *
     * @param scoreboard the parent scoreboard
     * @param id the id of this objective
     * @param displayName the initial display name
     * @param displaySlot the initial display slot
     */
    public PacketScoreboardObjective(PacketScoreboard scoreboard, String id, String displayName, DisplaySlot displaySlot) {
        this(scoreboard, id, displayName, displaySlot, true);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean shouldAutoSubscribe() {
        return autoSubscribe;
    }

    @Override
    public void setAutoSubscribe(boolean autoSubscribe) {
        this.autoSubscribe = autoSubscribe;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        Preconditions.checkNotNull(displayName, "displayName");
        displayName = trimName(Color.colorize(displayName));
        if (this.displayName.equals(displayName)) {
            return;
        }

        this.displayName = displayName;
        scoreboard.broadcastPacket(subscribed, newObjectivePacket(UpdateType.UPDATE));
    }

    @Override
    public DisplaySlot getDisplaySlot() {
        return displaySlot;
    }

    @Override
    public void setDisplaySlot(DisplaySlot displaySlot) {
        Preconditions.checkNotNull(displaySlot, "displaySlot");
        if (this.displaySlot == displaySlot) {
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
        return scores.containsKey(trimScore(Color.colorize(name)));
    }

    @Nullable
    @Override
    public Integer getScore(String name) {
        Preconditions.checkNotNull(name, "name");
        return scores.get(trimScore(Color.colorize(name)));
    }

    @Override
    public void setScore(String name, int value) {
        Preconditions.checkNotNull(name, "name");
        name = trimScore(Color.colorize(name));

        Integer oldValue = scores.put(name, value);
        if (oldValue != null && oldValue == value) {
            return;
        }

        scoreboard.broadcastPacket(subscribed, newScorePacket(name, value, ScoreboardAction.CHANGE));
    }

    @Override
    public boolean removeScore(String name) {
        Preconditions.checkNotNull(name, "name");
        name = trimScore(Color.colorize(name));

        if (scores.remove(name) == null) {
            return false;
        }

        scoreboard.broadcastPacket(subscribed, newScorePacket(name, 0, ScoreboardAction.REMOVE));
        return true;
    }

    @Override
    public void clearScores() {
        scores.clear();

        scoreboard.broadcastPacket(subscribed, newObjectivePacket(UpdateType.REMOVE));
        for (Player player : subscribed) {
            subscribe(player);
        }
    }

    @Override
    public void applyScores(Map<String, Integer> scores) {
        Preconditions.checkNotNull(scores, "scores");

        Set<String> toRemove = new HashSet<>(getScores().keySet());
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            toRemove.remove(trimScore(Color.colorize(score.getKey())));
        }
        for (String name : toRemove) {
            removeScore(name);
        }
        for (Map.Entry<String, Integer> score : scores.entrySet()) {
            setScore(score.getKey(), score.getValue());
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
        scoreboard.sendPacket(newObjectivePacket(UpdateType.CREATE), player);
        scoreboard.sendPacket(newDisplaySlotPacket(getDisplaySlot()), player);
        for (Map.Entry<String, Integer> score : getScores().entrySet()) {
            scoreboard.sendPacket(newScorePacket(score.getKey(), score.getValue(), ScoreboardAction.CHANGE), player);
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

        scoreboard.sendPacket(newObjectivePacket(UpdateType.REMOVE), player);
    }

    @Override
    public void unsubscribeAll() {
        scoreboard.broadcastPacket(subscribed, newObjectivePacket(UpdateType.REMOVE));
        subscribed.clear();
    }

    private PacketContainer newObjectivePacket(UpdateType mode) {
        // http://wiki.vg/Protocol#Scoreboard_Objective
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);

        // set name - limited to String(16)
        packet.getStrings().write(0, getId());

        // set mode - 0 to create the scoreboard. 1 to remove the scoreboard. 2 to update the display text.
        packet.getIntegers().write(0, mode.getCode());

        // set display name - limited to String(16) - Only if mode is 0 or 2. The text to be displayed for the score
        packet.getStrings().write(1, getDisplayName());

        // set type - either "integer" or "hearts"
        packet.getEnumModifier(HealthDisplay.class, 2).write(0, HealthDisplay.INTEGER);

        return packet;
    }

    private PacketContainer newScorePacket(String name, int value, ScoreboardAction action) {
        // http://wiki.vg/Protocol#Update_Score
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_SCORE);

        // set "Entity name" - aka the name of the score - limited to 40.
        packet.getStrings().write(0, name);

        // set the action - 0 to create/update an item. 1 to remove an item.
        packet.getScoreboardActions().write(0, action);

        // set objective name - The name of the objective the score belongs to
        packet.getStrings().write(1, getId());

        // set value of the score- The score to be displayed next to the entry. Only sent when Action does not equal 1.
        packet.getIntegers().write(0, value);

        return packet;
    }

    private PacketContainer newDisplaySlotPacket(DisplaySlot displaySlot) {
        // http://wiki.vg/Protocol#Display_Scoreboard
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);

        // set position
        final int slot;
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
                throw new RuntimeException();
        }

        packet.getIntegers().write(0, slot);

        // set objective name - The unique name for the scoreboard to be displayed.
        packet.getStrings().write(0, getId());

        return packet;
    }

    private enum UpdateType {
        CREATE(0),
        REMOVE(1),
        UPDATE(2);

        private final int code;

        UpdateType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    private enum HealthDisplay {
        INTEGER, HEARTS
    }
}
