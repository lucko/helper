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

package me.lucko.helper.hologram;

import me.lucko.helper.serialize.Position;
import me.lucko.helper.terminable.Terminable;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base interface for holograms.
 */
public interface BaseHologram extends Terminable {

    /**
     * Spawns the hologram
     */
    void spawn();

    /**
     * Despawns the hologram
     */
    void despawn();

    /**
     * Check if the hologram is currently spawned
     *
     * @return true if spawned and active, or false otherwise
     */
    boolean isSpawned();

    /**
     * Gets the ArmorStands that hold the lines for this hologram
     *
     * @return the ArmorStands holding the lines
     */
    @Nonnull
    Collection<ArmorStand> getArmorStands();

    /**
     * Gets the ArmorStand holding the specified line
     *
     * @param line the line
     * @return the ArmorStand holding this line
     */
    @Nullable
    ArmorStand getArmorStand(int line);

    /**
     * Updates the position of the hologram and respawns it
     *
     * @param position the new position
     */
    void updatePosition(@Nonnull Position position);

    /**
     * Sets a click callback for this hologram
     *
     * @param clickCallback the click callback, or null to unregister any existing callback
     */
    void setClickCallback(@Nullable Consumer<Player> clickCallback);

}
