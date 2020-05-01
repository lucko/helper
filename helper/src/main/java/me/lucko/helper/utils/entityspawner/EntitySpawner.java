package me.lucko.helper.utils.entityspawner;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

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
    <T extends Entity> T spawn(Location location, Class<T> entityClass, Consumer<? super T> beforeAdd);

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
