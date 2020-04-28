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

    public <T extends Entity> T spawn(Location location, Class<T> entityClass, Consumer<T> beforeAdd) {
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
