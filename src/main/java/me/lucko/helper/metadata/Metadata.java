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

import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Holds {@link MetadataMap}s bound to players.
 *
 * Maps are removed when players quit the server.
 */
public final class Metadata {

    private static LoadingCache<UUID, MetadataMap> players = null;

    // lazily load
    private static synchronized void setup() {
        if (players != null) return;

        players = CacheBuilder.newBuilder()
                .build(new CacheLoader<UUID, MetadataMap>() {
                    @Override
                    public MetadataMap load(UUID uuid) {
                        return MetadataMap.create();
                    }
                });

        Events.subscribe(PlayerQuitEvent.class, EventPriority.MONITOR)
                .handler(e -> {
                    MetadataMap map = players.asMap().remove(e.getPlayer().getUniqueId());
                    if (map != null) {
                        map.clear();
                    }
                });
    }

    public static MetadataMap getMap(Player player) {
        setup();
        return players.getUnchecked(player.getUniqueId());
    }

    public static MetadataMap getMap(UUID uuid) {
        setup();
        return players.getUnchecked(uuid);
    }

    private Metadata() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
