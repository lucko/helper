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

package me.lucko.helper.metadata;

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.metadata.type.BlockMetadataRegistry;
import me.lucko.helper.metadata.type.EntityMetadataRegistry;
import me.lucko.helper.metadata.type.PlayerMetadataRegistry;
import me.lucko.helper.metadata.type.WorldMetadataRegistry;
import me.lucko.helper.serialize.BlockPosition;
import me.lucko.helper.utils.Players;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * The Metadata registries provided by helper.
 *
 * These instances can be accessed through {@link Metadata}.
 */
final class StandardMetadataRegistries {

    public static final PlayerMetadataRegistry PLAYER = new PlayerRegistry();
    public static final EntityMetadataRegistry ENTITY = new EntityRegistry();
    public static final BlockMetadataRegistry BLOCK = new BlockRegistry();
    public static final WorldMetadataRegistry WORLD = new WorldRegistry();

    private static final MetadataRegistry<?>[] VALUES = new MetadataRegistry[]{PLAYER, ENTITY, BLOCK, WORLD};

    public static MetadataRegistry<?>[] values() {
        return VALUES;
    }

    private static final class PlayerRegistry extends AbstractMetadataRegistry<UUID> implements PlayerMetadataRegistry {

        @Nonnull
        @Override
        public MetadataMap provide(@Nonnull Player player) {
            Objects.requireNonNull(player, "player");
            return provide(player.getUniqueId());
        }

        @Nonnull
        @Override
        public Optional<MetadataMap> get(@Nonnull Player player) {
            Objects.requireNonNull(player, "player");
            return get(player.getUniqueId());
        }

        @Nonnull
        @Override
        public <K> Map<Player, K> getAllWithKey(@Nonnull MetadataKey<K> key) {
            Objects.requireNonNull(key, "key");
            ImmutableMap.Builder<Player, K> ret = ImmutableMap.builder();
            this.cache.asMap().forEach((uuid, map) -> map.get(key).ifPresent(t -> {
                Player player = Players.getNullable(uuid);
                if (player != null) {
                    ret.put(player, t);
                }
            }));
            return ret.build();
        }
    }

    private static final class EntityRegistry extends AbstractMetadataRegistry<UUID> implements EntityMetadataRegistry {

        @Nonnull
        @Override
        public MetadataMap provide(@Nonnull Entity entity) {
            Objects.requireNonNull(entity, "entity");
            return provide(entity.getUniqueId());
        }

        @Nonnull
        @Override
        public Optional<MetadataMap> get(@Nonnull Entity entity) {
            Objects.requireNonNull(entity, "entity");
            return get(entity.getUniqueId());
        }

        @Nonnull
        @Override
        public <K> Map<Entity, K> getAllWithKey(@Nonnull MetadataKey<K> key) {
            Objects.requireNonNull(key, "key");
            ImmutableMap.Builder<Entity, K> ret = ImmutableMap.builder();
            this.cache.asMap().forEach((uuid, map) -> map.get(key).ifPresent(t -> {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity != null) {
                    ret.put(entity, t);
                }
            }));
            return ret.build();
        }
    }

    private static final class BlockRegistry extends AbstractMetadataRegistry<BlockPosition> implements BlockMetadataRegistry {

        @Nonnull
        @Override
        public MetadataMap provide(@Nonnull Block block) {
            Objects.requireNonNull(block, "block");
            return provide(BlockPosition.of(block));
        }

        @Nonnull
        @Override
        public Optional<MetadataMap> get(@Nonnull Block block) {
            Objects.requireNonNull(block, "block");
            return get(BlockPosition.of(block));
        }

        @Nonnull
        @Override
        public <K> Map<BlockPosition, K> getAllWithKey(@Nonnull MetadataKey<K> key) {
            Objects.requireNonNull(key, "key");
            ImmutableMap.Builder<BlockPosition, K> ret = ImmutableMap.builder();
            this.cache.asMap().forEach((pos, map) -> map.get(key).ifPresent(t -> ret.put(pos, t)));
            return ret.build();
        }
    }

    private static final class WorldRegistry extends AbstractMetadataRegistry<UUID> implements WorldMetadataRegistry {

        @Nonnull
        @Override
        public MetadataMap provide(@Nonnull World world) {
            Objects.requireNonNull(world, "world");
            return provide(world.getUID());
        }

        @Nonnull
        @Override
        public Optional<MetadataMap> get(@Nonnull World world) {
            Objects.requireNonNull(world, "world");
            return get(world.getUID());
        }

        @Nonnull
        @Override
        public <K> Map<World, K> getAllWithKey(@Nonnull MetadataKey<K> key) {
            Objects.requireNonNull(key, "key");
            ImmutableMap.Builder<World, K> ret = ImmutableMap.builder();
            this.cache.asMap().forEach((uuid, map) -> map.get(key).ifPresent(t -> {
                World world = Bukkit.getWorld(uuid);
                if (world != null) {
                    ret.put(world, t);
                }
            }));
            return ret.build();
        }
    }

    private StandardMetadataRegistries() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
