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

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.terminable.Terminable;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A simple hologram utility.
 *
 * Should only be used from the server thread.
 */
public interface Hologram extends Terminable, GsonSerializable {

    /**
     * Creates and returns a new hologram
     *
     * <p>Note: the hologram will not be spawned automatically.</p>
     *
     * @param position the position of the hologram
     * @param lines the initial lines to display
     * @return the new hologram.
     */
    @Nonnull
    static Hologram create(@Nonnull Position position, @Nonnull List<String> lines) {
        return new SimpleHologram(position, lines);
    }

    static Hologram deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("position"));
        Preconditions.checkArgument(object.has("lines"));

        Position position = Position.deserialize(object.get("position"));
        JsonArray lineArray = object.get("lines").getAsJsonArray();
        List<String> lines = new ArrayList<>();
        for (JsonElement e : lineArray) {
            lines.add(e.getAsString());
        }

        return create(position, lines);
    }

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
     * Updates the position of the hologram and respawns it
     *
     * @param position the new position
     */
    void updatePosition(@Nonnull Position position);

    /**
     * Updates the lines displayed by this hologram
     *
     * <p>This method does not refresh the actual hologram display. {@link #spawn()} must be called for these changes
     * to apply.</p>
     *
     * @param lines the new lines
     */
    void updateLines(@Nonnull List<String> lines);

    /**
     * Sets a click callback for this hologram
     *
     * @param clickCallback the click callback, or null to unregister any existing callback
     */
    void setClickCallback(@Nullable Consumer<Player> clickCallback);

}
