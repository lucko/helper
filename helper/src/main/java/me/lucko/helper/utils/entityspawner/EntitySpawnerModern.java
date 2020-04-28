package me.lucko.helper.utils.entityspawner;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

enum EntitySpawnerModern implements EntitySpawner {
    INSTANCE;

    public <T extends Entity> T spawn(Location location, Class<T> entityClass, Consumer<T> beforeAdd) {
        return location.getWorld().spawn(location, entityClass, beforeAdd::accept);
    }

}
