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
import me.lucko.helper.Helper;
import me.lucko.helper.gson.JsonBuilder;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.reflect.MinecraftVersions;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import me.lucko.helper.text3.Text;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BukkitHologramFactory implements HologramFactory {

    @Nonnull
    @Override
    public Hologram newHologram(@Nonnull Position position, @Nonnull List<String> lines) {
        return new BukkitHologram(position, lines);
    }

    private static final class BukkitHologram implements Hologram {
        private static final Method SET_CAN_TICK;
        static {
            Method setCanTick = null;
            try {
                setCanTick = ArmorStand.class.getMethod("setCanTick", boolean.class);
            } catch (NoSuchMethodException ignored) {}

            SET_CAN_TICK = setCanTick;
        }

        private Position position;
        private final List<String> lines = new ArrayList<>();
        private final List<ArmorStand> spawnedEntities = new ArrayList<>();
        private boolean spawned = false;

        private CompositeTerminable listeners = null;
        private Consumer<Player> clickCallback = null;
        private final List<Pig> spawnedPassengers = new ArrayList<>();

        BukkitHologram(Position position, List<String> lines) {
            this.position = Objects.requireNonNull(position, "position");
            updateLines(lines);
        }

        private Position getNewLinePosition() {
            if (this.spawnedEntities.isEmpty()) {
                return this.position;
            } else {
                // get the last entry
                ArmorStand last = this.spawnedEntities.get(this.spawnedEntities.size() - 1);
                return Position.of(last.getLocation()).subtract(0.0, 0.25, 0.0);
            }
        }

        @Override
        public void spawn() {
            // resize to fit any new lines
            int linesSize = this.lines.size();
            int spawnedSize = this.spawnedEntities.size();

            // remove excess lines
            if (linesSize < spawnedSize) {
                int diff = spawnedSize - linesSize;
                for (int i = 0; i < diff; i++) {

                    // get and remove the last entry
                    ArmorStand as = this.spawnedEntities.remove(this.spawnedEntities.size() - 1);
                    as.remove();

                    if (this.listeners != null) {
                        Pig pig = this.spawnedPassengers.remove(this.spawnedPassengers.size() - 1);
                        pig.remove();
                    }
                }
            }

            // now enough armorstands are spawned, we can now update the text
            for (int i = 0; i < this.lines.size(); i++) {
                String line = this.lines.get(i);

                if (i >= this.spawnedEntities.size()) {
                    // add a new line
                    Location loc = getNewLinePosition().toLocation();

                    // ensure the hologram's chunk is loaded.
                    Chunk chunk = loc.getChunk();
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }

                    // remove any armorstands already at this location. (leftover from a server restart)
                    loc.getWorld().getNearbyEntities(loc, 1.0, 1.0, 1.0).forEach(e -> {
                        if (e.getType() == EntityType.ARMOR_STAND && locationsEqual(e.getLocation(), loc)) {
                            e.remove();
                        }
                    });

                    ArmorStand as = loc.getWorld().spawn(loc, ArmorStand.class);
                    as.setSmall(true);
                    as.setMarker(true);
                    as.setArms(false);
                    as.setBasePlate(false);
                    as.setGravity(false);
                    as.setVisible(false);
                    as.setCustomName(line);
                    as.setCustomNameVisible(true);

                    if (MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_9)) {
                        as.setAI(false);
                        as.setCollidable(false);
                        as.setInvulnerable(true);
                    }

                    if (SET_CAN_TICK != null) {
                        try {
                            SET_CAN_TICK.invoke(as, false);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }

                    if (this.listeners != null) {
                        Pig pig = (Pig) as.getWorld().spawnEntity(as.getLocation(), EntityType.PIG);
                        pig.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                        pig.setCustomNameVisible(false);
                        pig.setSilent(true);
                        pig.setGravity(false);

                        pig.setMetadata("nodespawn", new FixedMetadataValue(Helper.hostPlugin(), true));

                        if (MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_9)) {
                            pig.setAI(false);
                            pig.setCollidable(false);
                            pig.setInvulnerable(true);
                        }

                        as.addPassenger(pig);

                        this.spawnedPassengers.add(pig);
                    }

                    this.spawnedEntities.add(as);
                } else {
                    // update existing line if necessary
                    ArmorStand as = this.spawnedEntities.get(i);

                    if (as.getCustomName() != null && as.getCustomName().equals(line)) {
                        continue;
                    }

                    as.setCustomName(line);
                }
            }

            if (this.listeners == null && this.clickCallback != null) {
                setClickCallback(this.clickCallback);
            }

            this.spawned = true;
        }

        @Override
        public void despawn() {
            this.spawnedEntities.forEach(Entity::remove);
            this.spawnedEntities.clear();
            this.spawned = false;

            if (this.listeners != null) {
                this.listeners.closeAndReportException();
            }
            this.listeners = null;
        }

        @Override
        public boolean isSpawned() {
            if (!this.spawned) {
                return false;
            }

            for (ArmorStand stand : this.spawnedEntities) {
                if (!stand.isValid()) {
                    return false;
                }
            }

            return true;
        }

        @Nonnull
        @Override
        public Collection<ArmorStand> getArmorStands() {
            return this.spawnedEntities;
        }

        @Nullable
        @Override
        public ArmorStand getArmorStand(int line) {
            if (line >= this.spawnedEntities.size()) {
                return null;
            }
            return this.spawnedEntities.get(line);
        }

        @Override
        public void updatePosition(@Nonnull Position position) {
            Objects.requireNonNull(position, "position");
            if (this.position.equals(position)) {
                return;
            }

            this.position = position;
            despawn();
            spawn();
        }

        @Override
        public void updateLines(@Nonnull List<String> lines) {
            Objects.requireNonNull(lines, "lines");
            Preconditions.checkArgument(!lines.isEmpty(), "lines cannot be empty");
            for (String line : lines) {
                Preconditions.checkArgument(line != null, "null line");
            }

            List<String> ret = lines.stream().map(Text::colorize).collect(Collectors.toList());
            if (this.lines.equals(ret)) {
                return;
            }

            this.lines.clear();
            this.lines.addAll(ret);
        }

        @Override
        public void setClickCallback(@Nullable Consumer<Player> clickCallback) {
            // unregister any existing listeners
            if (clickCallback == null) {
                if (this.listeners != null) {
                    this.listeners.closeAndReportException();
                }
                this.clickCallback = null;
                this.listeners = null;
                return;
            }

            this.clickCallback = clickCallback;

            if (this.listeners == null) {
                this.listeners = CompositeTerminable.create();

                this.listeners.bind(() -> {
                    this.spawnedPassengers.forEach(Entity::remove);
                    this.spawnedPassengers.clear();
                });

                this.spawnedPassengers.forEach(Entity::remove);
                this.spawnedPassengers.clear();

                for (ArmorStand as : this.spawnedEntities) {
                    Pig pig = (Pig) as.getWorld().spawnEntity(as.getLocation(), EntityType.PIG);
                    pig.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
                    pig.setCustomNameVisible(false);
                    pig.setSilent(true);
                    pig.setGravity(false);

                    pig.setMetadata("nodespawn", new FixedMetadataValue(Helper.hostPlugin(), true));

                    if (MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_9)) {
                        pig.setAI(false);
                        pig.setCollidable(false);
                        pig.setInvulnerable(true);
                    }

                    as.addPassenger(pig);
                }

                Events.subscribe(PigZapEvent.class)
                        .handler(e -> {
                            for (Pig spawned : this.spawnedPassengers) {
                                if (spawned.equals(e.getEntity())) {
                                    e.setCancelled(true);
                                    return;
                                }
                            }
                        }).bindWith(this.listeners);

                Events.subscribe(PlayerInteractEntityEvent.class)
                        .filter(e -> e.getRightClicked() instanceof Pig)
                        .handler(e -> {
                            Player p = e.getPlayer();
                            Pig pig = (Pig) e.getRightClicked();

                            for (Pig spawned : this.spawnedPassengers) {
                                if (spawned.equals(pig)) {
                                    e.setCancelled(true);
                                    this.clickCallback.accept(p);
                                    return;
                                }
                            }
                        })
                        .bindWith(this.listeners);

                Events.subscribe(EntityDamageByEntityEvent.class)
                        .filter(e -> e.getEntity() instanceof Pig)
                        .filter(e -> e.getDamager() instanceof Player)
                        .handler(e -> {
                            Player p = (Player) e.getDamager();
                            Pig pig = (Pig) e.getEntity();

                            for (Pig spawned : this.spawnedPassengers) {
                                if (spawned.equals(pig)) {
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
        public void close() {
            despawn();
        }

        @Override
        public boolean isClosed() {
            return !this.spawned;
        }

        @Nonnull
        @Override
        public JsonObject serialize() {
            return JsonBuilder.object()
                    .add("position", this.position)
                    .add("lines", JsonBuilder.array().addStrings(this.lines).build())
                    .build();
        }

        private static boolean locationsEqual(Location l1, Location l2) {
            return Double.doubleToLongBits(l1.getX()) == Double.doubleToLongBits(l2.getX()) &&
                    Double.doubleToLongBits(l1.getY()) == Double.doubleToLongBits(l2.getY()) &&
                    Double.doubleToLongBits(l1.getZ()) == Double.doubleToLongBits(l2.getZ());
        }
    }
}
