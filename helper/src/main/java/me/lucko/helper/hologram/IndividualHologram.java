package me.lucko.helper.hologram;

import com.google.gson.JsonElement;
import com.sllibrary.bukkit.Services;
import com.sllibrary.bukkit.serialize.Position;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IndividualHologram extends Hologram {

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
    static IndividualHologram create(@Nonnull Position position, @Nonnull List<String> lines) {
        return Services.load(HologramFactory.class).newIndividualHologram(position, lines);
    }

    static IndividualHologram deserialize(JsonElement element) {
        return (IndividualHologram) Services.load(HologramFactory.class).deserialize(element);
    }

    /**
     * Returns a copy of the available viewers of the hologram.
     *
     * @return a {@link Set} of player names.
     */
    Set<String> getViewers();

    /**
     * Adds a viewer to the hologram.
     *
     * @param name
     */
    void addViewer(String name);

    /**
     * Removes a viewer from the hologram.
     *
     * @param name
     */
    void removeViewer(String name);

    /**
     * Check if there are any viewers for the hologram.
     *
     * @return any viewers
     */
    default boolean hasViewers() {
        return this.getViewers().size() > 0;
    }
}
