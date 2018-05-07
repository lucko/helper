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

package me.lucko.helper.hologram.individual;

import me.lucko.helper.Services;
import me.lucko.helper.hologram.BaseHologram;
import me.lucko.helper.serialize.Position;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

public interface IndividualHologram extends BaseHologram {

    /**
     * Creates and returns a new individual hologram
     *
     * <p>Note: the hologram will not be spawned automatically.</p>
     *
     * @param position the position of the hologram
     * @param lines the initial lines to display
     * @return the new hologram.
     */
    @Nonnull
    static IndividualHologram create(@Nonnull Position position, @Nonnull List<HologramLine> lines) {
        return Services.load(IndividualHologramFactory.class).newHologram(position, lines);
    }

    /**
     * Updates the lines displayed by this hologram
     *
     * <p>This method does not refresh the actual hologram display. {@link #spawn()} must be called for these changes
     * to apply.</p>
     *
     * @param lines the new lines
     */
    void updateLines(@Nonnull List<HologramLine> lines);

    /**
     * Returns a copy of the available viewers of the hologram.
     *
     * @return a {@link Set} of players.
     */
    @Nonnull
    Set<Player> getViewers();

    /**
     * Adds a viewer to the hologram.
     *
     * @param player the player
     */
    void addViewer(@Nonnull Player player);

    /**
     * Removes a viewer from the hologram.
     *
     * @param player the player
     */
    void removeViewer(@Nonnull Player player);

    /**
     * Check if there are any viewers for the hologram.
     *
     * @return any viewers
     */
    default boolean hasViewers() {
        return this.getViewers().size() > 0;
    }
}
