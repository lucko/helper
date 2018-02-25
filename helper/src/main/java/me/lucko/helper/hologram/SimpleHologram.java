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
import com.google.gson.JsonObject;

import me.lucko.helper.Events;
import me.lucko.helper.gson.JsonBuilder;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.terminable.registry.TerminableRegistry;
import me.lucko.helper.utils.Color;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class SimpleHologram implements Hologram {

    private Position position;
    private final List<String> lines = new ArrayList<>();
    private final List<ArmorStand> spawnedEntities = new ArrayList<>();
    private boolean spawned = false;

    private TerminableRegistry listeners = null;
    private Consumer<Player> clickCallback = null;

    SimpleHologram(Position position, List<String> lines) {
        this.position = Preconditions.checkNotNull(position, "position");
        updateLines(lines);
    }

    private Position getNewLinePosition() {
        if (spawnedEntities.isEmpty()) {
            return position;
        } else {
            // get the last entry
            ArmorStand last = spawnedEntities.get(spawnedEntities.size() - 1);
            return Position.of(last.getLocation()).subtract(0.0d, 0.25d, 0.0d);
        }
    }

    @Override
    public void spawn() {
        // resize to fit any new lines
        int linesSize = lines.size();
        int spawnedSize = spawnedEntities.size();

        // remove excess lines
        if (linesSize < spawnedSize) {
            int diff = spawnedSize - linesSize;
            for (int i = 0; i < diff; i++) {

                // get and remove the last entry
                ArmorStand as = spawnedEntities.remove(spawnedEntities.size() - 1);
                as.remove();
            }
        }

        // now enough armorstands are spawned, we can now update the text
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (i >= spawnedEntities.size()) {
                // add a new line
                Location loc = getNewLinePosition().toLocation();

                // remove any armorstands already at this location. (leftover from a server restart)
                loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0).forEach(e -> {
                    if (e.getType() == EntityType.ARMOR_STAND && locationsEqual(e.getLocation(), loc)) {
                        e.remove();
                    }
                });

                ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class, stand -> {
                    stand.setSmall(true);
                    stand.setMarker(true);
                    stand.setArms(false);
                    stand.setBasePlate(false);
                    stand.setGravity(false);
                    stand.setVisible(false);
                    stand.setAI(false);
                    stand.setCollidable(false);
                    stand.setInvulnerable(true);
                    stand.setCustomName(line);
                    stand.setCustomNameVisible(true);
                });

                spawnedEntities.add(as);
            } else {
                // update existing line if necessary
                ArmorStand as = spawnedEntities.get(i);

                if (as.getCustomName() != null && as.getCustomName().equals(line)) {
                    continue;
                }

                as.setCustomName(line);
            }
        }

        if (this.listeners == null && this.clickCallback != null) {
            setClickCallback(this.clickCallback);
        }

        spawned = true;
    }

    @Override
    public void despawn() {
        spawnedEntities.forEach(Entity::remove);
        spawnedEntities.clear();
        spawned = false;

        if (listeners != null) {
            listeners.terminate();
        }
        listeners = null;
    }

    @Override
    public boolean isSpawned() {
        if (!spawned) {
            return false;
        }

        for (ArmorStand stand : spawnedEntities) {
            if (!stand.isValid()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void updatePosition(@Nonnull Position position) {
        Preconditions.checkNotNull(position, "position");
        if (this.position.equals(position)) {
            return;
        }

        this.position = position;
        despawn();
        spawn();
    }

    @Override
    public void updateLines(@Nonnull List<String> lines) {
        Preconditions.checkNotNull(lines, "lines");
        Preconditions.checkArgument(!lines.isEmpty(), "lines cannot be empty");
        for (String line : lines) {
            Preconditions.checkArgument(line != null, "null line");
        }

        List<String> ret = lines.stream().map(Color::colorize).collect(Collectors.toList());
        if (this.lines.equals(ret)) {
            return;
        }

        this.lines.clear();
        this.lines.addAll(ret);
    }

    public void setClickCallback(@Nullable Consumer<Player> clickCallback) {
        // unregister any existing listeners
        if (clickCallback == null) {
            if (this.listeners != null) {
                this.listeners.terminate();
            }
            this.clickCallback = null;
            this.listeners = null;
            return;
        }

        this.clickCallback = clickCallback;

        if (this.listeners == null) {
            this.listeners = TerminableRegistry.create();
            Events.subscribe(PlayerInteractAtEntityEvent.class)
                    .filter(e -> e.getRightClicked() instanceof ArmorStand)
                    .handler(e -> {
                        Player p = e.getPlayer();
                        ArmorStand as = (ArmorStand) e.getRightClicked();

                        for (ArmorStand spawned : this.spawnedEntities) {
                            if (spawned.equals(as)) {
                                e.setCancelled(true);
                                this.clickCallback.accept(p);
                                return;
                            }
                        }
                    })
                    .bindWith(this.listeners);

            Events.subscribe(EntityDamageByEntityEvent.class)
                    .filter(e -> e.getEntity() instanceof ArmorStand)
                    .filter(e -> e.getDamager() instanceof Player)
                    .handler(e -> {
                        Player p = (Player) e.getDamager();
                        ArmorStand as = (ArmorStand) e.getEntity();

                        for (ArmorStand spawned : this.spawnedEntities) {
                            if (spawned.equals(as)) {
                                e.setCancelled(true);
                                this.clickCallback.accept(p);
                                return;
                            }
                        }
                    })
                    .bindWith(this.listeners);
        }
    }

    @Override
    public boolean terminate() {
        despawn();
        return true;
    }

    @Override
    public boolean hasTerminated() {
        return !spawned;
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        return JsonBuilder.object()
                .add("position", position)
                .add("lines", JsonBuilder.array().addStrings(lines).build())
                .build();
    }

    private static boolean locationsEqual(Location l1, Location l2) {
        return Double.doubleToLongBits(l1.getX()) == Double.doubleToLongBits(l2.getX()) &&
                Double.doubleToLongBits(l1.getY()) == Double.doubleToLongBits(l2.getY()) &&
                Double.doubleToLongBits(l1.getZ()) == Double.doubleToLongBits(l2.getZ());
    }
}
