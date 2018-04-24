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

package me.lucko.helper.npc;

import me.lucko.helper.metadata.MetadataMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a NPC (non-player character)
 */
public interface Npc {

    /**
     * Applies a click callback listener to this NPC.
     *
     * @param clickCallback the click callback
     */
    void setClickCallback(@Nullable Consumer<Player> clickCallback);

    /**
     * Gets the NPCs attached metadata map.
     *
     * @return the metadata map
     */
    @Nonnull
    MetadataMap getMeta();

    /**
     * Sets the NPCs skin to the skin of the given player.
     *
     * @param skinPlayer the player
     * @deprecated in favour of {@link #setSkin(String, String)}
     */
    @Deprecated
    void setSkin(@Nonnull String skinPlayer);

    /**
     * Sets the NPCs skin to the given textures
     *
     * @param textures the textures
     * @param signature the signature of the textires
     */
    void setSkin(@Nonnull String textures, @Nonnull String signature);

    /**
     * Sets the name of this NPC
     *
     * @param name the name
     */
    void setName(@Nonnull String name);

    /**
     * Sets if this NPCs nametag should be shown
     *
     * @param show is the nametag should be shown
     */
    void setShowNametag(boolean show);

    /**
     * Gets the location where this NPC was initially spawned at
     *
     * @return the initial spawn location of the NPC
     */
    @Nonnull
    Location getInitialSpawn();

}
