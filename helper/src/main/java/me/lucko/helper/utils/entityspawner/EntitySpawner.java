package me.lucko.helper.utils.entityspawner;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

/**
 * Utility for spawning entities into a world.
 */
public interface EntitySpawner {

    /**
     * The instance.
     */
    EntitySpawner INSTANCE = EntitySpawnerProvider.get();

    /**
     * Spawns an entity at the given location.
     *
     * @param location the location to spawn at
     * @param entityClass the class of the entity
     * @param beforeAdd a callback executed after the entity has been created but before it is added to the world
     * @param <T> the entity type
     * @return the spawned entity
     */
    <T extends Entity> T spawn(Location location, Class<T> entityClass, Consumer<T> beforeAdd);

}

class EntitySpawnerProvider {
    static EntitySpawner get() {
        try {
            // test for modern method
            Class<?> consumerClass = Class.forName("org.bukkit.util.Consumer");
            World.class.getDeclaredMethod("spawn", Location.class, Class.class, consumerClass);

            return EntitySpawnerModern.INSTANCE;
        } catch (ReflectiveOperationException e) {
            return EntitySpawnerLegacy.INSTANCE;
        }
    }
}
