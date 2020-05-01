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

package me.lucko.helper.utils.entityspawner;

import me.lucko.helper.reflect.ServerReflection;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Method;
import java.util.function.Consumer;

enum EntitySpawnerLegacy implements EntitySpawner {
    INSTANCE;

    private static final Method CREATE_ENTITY_METHOD;
    private static final Method ADD_ENTITY_METHOD;
    private static final Method GET_BUKKIT_ENTITY_METHOD;

    static {
        try {
            Class<?> craftWorldClass = ServerReflection.obcClass("CraftWorld");
            Class<?> entityClass = ServerReflection.nmsClass("Entity");
            CREATE_ENTITY_METHOD = craftWorldClass.getDeclaredMethod("createEntity", Location.class, Class.class);
            ADD_ENTITY_METHOD = craftWorldClass.getDeclaredMethod("addEntity", entityClass, CreatureSpawnEvent.SpawnReason.class);
            GET_BUKKIT_ENTITY_METHOD = entityClass.getDeclaredMethod("getBukkitEntity");
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public <T extends Entity> T spawn(Location location, Class<T> entityClass, Consumer<? super T> beforeAdd) {
        World world = location.getWorld();

        try {
            Object entity = CREATE_ENTITY_METHOD.invoke(world, location, entityClass);
            T bukkitEntity = entityClass.cast(GET_BUKKIT_ENTITY_METHOD.invoke(entity));
            beforeAdd.accept(bukkitEntity);
            ADD_ENTITY_METHOD.invoke(world, entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
            return bukkitEntity;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

}
