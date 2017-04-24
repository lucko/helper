/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me>
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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import me.lucko.helper.Events;
import me.lucko.helper.Scheduler;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds {@link MetadataMap}s bound to players, entities, blocks and worlds.
 */
public final class Metadata {

    private static LoadingCache<UUID, MetadataMap> players = null;
    private static LoadingCache<UUID, MetadataMap> entities = null;
    private static LoadingCache<Integer, MetadataMap> blocks = null;
    private static LoadingCache<UUID, MetadataMap> worlds = null;

    // lazily load
    private static synchronized void setup() {
        if (players != null) return;

        players = makeCache();
        entities = makeCache();
        blocks = makeCache();
        worlds = makeCache();

        // remove player metadata when they leave the server
        Events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR)
                .handler(e -> {
                    MetadataMap map = players.asMap().remove(e.getPlayer().getUniqueId());
                    if (map != null) {
                        map.clear();
                    }
                });

        // cache housekeeping task
        Scheduler.runTaskRepeatingAsync(() -> {
            players.asMap().values().removeIf(MetadataMap::isEmpty);
            entities.asMap().values().removeIf(MetadataMap::isEmpty);
            blocks.asMap().values().removeIf(MetadataMap::isEmpty);
            worlds.asMap().values().removeIf(MetadataMap::isEmpty);
        }, 1200L + ThreadLocalRandom.current().nextInt(20), 1200L);
    }

    private static <T> LoadingCache<T, MetadataMap> makeCache() {
        return CacheBuilder.newBuilder()
                .build(new CacheLoader<T, MetadataMap>() {
                    @Override
                    public MetadataMap load(T uuid) {
                        return MetadataMap.create();
                    }
                });
    }

    /**
     * Gets a MetadataMap for the given player.
     * A map will only be loaded when requested for.
     *
     * @param player the player
     * @return a metadata map
     */
    public static MetadataMap provideForPlayer(Player player) {
        setup();
        return players.getUnchecked(player.getUniqueId());
    }

    /**
     * Gets a MetadataMap for the given player.
     * A map will only be loaded when requested for.
     *
     * @param uuid the players uuid
     * @return a metadata map
     */
    public static MetadataMap provideForPlayer(UUID uuid) {
        setup();
        return players.getUnchecked(uuid);
    }

    /**
     * Gets a MetadataMap for the given entity.
     * A map will only be loaded when requested for.
     *
     * @param entity the entity
     * @return a metadata map
     */
    public static MetadataMap provideForEntity(Entity entity) {
        setup();

        if (entity instanceof Player) {
            return provideForPlayer(((Player) entity));
        }

        return entities.getUnchecked(entity.getUniqueId());
    }

    /**
     * Gets a MetadataMap for the given block.
     * A map will only be loaded when requested for.
     *
     * @param block the block
     * @return a metadata map
     */
    public static MetadataMap provideForBlock(Block block) {
        setup();
        return blocks.getUnchecked(block.hashCode());
    }

    private Metadata() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
