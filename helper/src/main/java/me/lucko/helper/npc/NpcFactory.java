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

import me.lucko.helper.terminable.Terminable;

import org.bukkit.Location;

import javax.annotation.Nonnull;

/**
 * Represents an object which can create {@link Npc}s.
 */
public interface NpcFactory extends Terminable {

    /**
     * Spawns a NPC at the given location
     *
     * @param location the location to spawn the npc at
     * @param nametag the nametag to give the npc
     * @param skinPlayer the username of the player whose skin the NPC should have
     * @return the created npc
     * @deprecated in favour of {@link #spawnNpc(Location, String, String, String)}
     */
    @Nonnull
    @Deprecated
    Npc spawnNpc(@Nonnull Location location, @Nonnull String nametag, @Nonnull String skinPlayer);

    /**
     * Spawns a NPC at the given location
     *
     * @param location the location to spawn the npc at
     * @param nametag the nametag to give the npc
     * @param skinTextures the skin textures the NPC should have
     * @param skinSignature the signature of the provided textures
     * @return the created npc
     */
    @Nonnull
    Npc spawnNpc(@Nonnull Location location, @Nonnull String nametag, @Nonnull String skinTextures, @Nonnull String skinSignature);

    @Override
    void close();
}
