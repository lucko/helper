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

package me.lucko.helper.hologram.individual;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;

import me.lucko.helper.Events;
import me.lucko.helper.event.SingleSubscription;
import me.lucko.helper.gson.JsonBuilder;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.serialize.Position;
import me.lucko.helper.text.Text;
import me.lucko.helper.utils.Players;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PacketIndividualHologramFactory implements IndividualHologramFactory {

    @Nonnull
    @Override
    public IndividualHologram newHologram(@Nonnull Position position, @Nonnull List<String> lines) {
        return new BukkitIndividualHologram(position, lines);
    }

    private static class BukkitIndividualHologram implements IndividualHologram {

        private Set<String> viewers = Sets.newHashSet();

        private Position position;
        private final List<String> lines = Lists.newArrayList();
        private final List<EntityArmorStand> spawnedEntities = Lists.newArrayList();
        private boolean spawned = false;

        private PacketListener listener;
        private Consumer<Player> clickCallback = null;

        private final SingleSubscription<PlayerJoinEvent> joinSubscription;

        BukkitIndividualHologram(Position position, List<String> lines) {
            this.position = Objects.requireNonNull(position, "position");
            updateLines(lines);

            this.joinSubscription = Events.subscribe(PlayerJoinEvent.class)
                    .filter(event -> this.viewers.contains(event.getPlayer().getName()))
                    .handler(event -> {
                        Player player = event.getPlayer();

                        Arrays.stream(this.getSpawnPackets()).forEach(packetContainer -> sendPacket(player, packetContainer));
                        Arrays.stream(this.getMetaPackets()).forEach(packetContainer -> sendPacket(player, packetContainer));
                    });
        }

        private Position getNewLinePosition(int index) {
            if (this.spawnedEntities.isEmpty()) {
                return this.position;
            } else {
                // get the last entry
                return Position.of(this.position.toLocation().clone().subtract(0, index * .25, 0));
            }
        }

        @Override
        public Set<String> getViewers() {
            return Sets.newHashSet(this.viewers);
        }

        @Override
        public void addViewer(String name) {
            Objects.requireNonNull(name, "name");
            Preconditions.checkArgument(!name.isEmpty(), "name is empty");
            this.viewers.add(name);

            if (!this.isSpawned()) {
                return;
            }

            Players.get(name).ifPresent(player -> {
                Arrays.stream(this.getSpawnPackets()).forEach(packetContainer -> this.sendPacket(player, packetContainer));
                Arrays.stream(this.getMetaPackets()).forEach(packetContainer -> this.sendPacket(player, packetContainer));

            });

        }

        @Override
        public void removeViewer(String name) {
            Objects.requireNonNull(name, "name");
            Preconditions.checkArgument(!name.isEmpty(), "name is empty");
            this.viewers.remove(name);

            Players.get(name).ifPresent(player -> Arrays.stream(this.getDespawnPackets()).forEach(packetContainer -> this.sendPacket(player, packetContainer)));

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
                    EntityArmorStand entityArmorStand = this.spawnedEntities.remove(this.spawnedEntities.size() - 1);

                    PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                    packet.getIntegerArrays().write(0, new int[]{entityArmorStand.getId()});

                    this.viewers.stream().map(Bukkit::getPlayer).forEach(player -> this.sendPacket(player, packet));
                }
            }

            this.lines.forEach(line -> {
                EntityArmorStand armorStand = new EntityArmorStand(((CraftWorld) Bukkit.getWorld(this.position.getWorld())).getHandle());
                armorStand.setCustomName(line);

                this.spawnedEntities.add(armorStand);
            });

            this.viewers.stream().map(Bukkit::getPlayer).forEach(player -> {

                Arrays.stream(this.getSpawnPackets()).forEach(packetContainer -> sendPacket(player, packetContainer));
                Arrays.stream(this.getMetaPackets()).forEach(packetContainer -> sendPacket(player, packetContainer));

            });
            if (this.listener == null && this.clickCallback != null) {
                setClickCallback(this.clickCallback);
            }
            this.spawned = true;
        }

        @Override
        public void despawn() {
            if (!this.spawned) {
                return;
            }
            this.viewers.stream().map(Bukkit::getPlayer).forEach(player -> Arrays.stream(this.getDespawnPackets()).forEach(packetContainer -> sendPacket(player, packetContainer)));
            this.spawned = false;
            this.spawnedEntities.clear();

            if (this.listener != null) {
                ProtocolLibrary.getProtocolManager().removePacketListener(this.listener);
            }
        }

        @Override
        public boolean isSpawned() {
            return this.spawned;
        }

        @Override
        public void updatePosition(@Nonnull Position position) {
            Objects.requireNonNull(position, "position");
            if (this.position.equals(position)) {
                return;
            }
            this.position = position;

            this.despawn();
            this.spawn();
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

            if (this.viewers.isEmpty()) {
                return;
            }
            this.viewers.stream().map(Bukkit::getPlayer).forEach(player -> Arrays.stream(this.getMetaPackets()).forEach(packetContainer -> sendPacket(player, packetContainer)));
        }

        @Override
        public void setClickCallback(@Nullable Consumer<Player> clickCallback) {
            // unregister any existing listeners
            if (clickCallback == null) {
                if (this.listener != null) {
                    return;
                }
                this.clickCallback = null;
                this.listener = null;
                return;
            }

            this.clickCallback = clickCallback;

            if (this.listener != null) {
                ProtocolLibrary.getProtocolManager().removePacketListener(this.listener);
            }

            this.listener = new PacketAdapter(LoaderUtils.getPlugin(), PacketType.Play.Client.USE_ENTITY) {
                @Override
                public void onPacketReceiving(PacketEvent event) {
                    PacketContainer packetContainer = event.getPacket();

                    int id = packetContainer.getIntegers().read(0);

                    if (spawnedEntities.stream().noneMatch(entityArmorStand -> entityArmorStand.getId() == id)) {
                        return;
                    }
                    EnumWrappers.EntityUseAction entityUseAction = packetContainer.getEntityUseActions().read(0);
                    if (entityUseAction == EnumWrappers.EntityUseAction.ATTACK) {
                        event.setCancelled(true);
                        return;
                    }
                    if (entityUseAction == EnumWrappers.EntityUseAction.INTERACT) {
                        event.setCancelled(true);
                        return;
                    }
                    if (packetContainer.getHands().read(0) == EnumWrappers.Hand.OFF_HAND) {
                        event.setCancelled(true);
                        return;
                    }
                    clickCallback.accept(event.getPlayer());
                }
            };
            ProtocolLibrary.getProtocolManager().addPacketListener(this.listener);
        }

        @Nonnull
        @Override
        public JsonElement serialize() {
            return JsonBuilder.object()
                    .add("position", this.position)
                    .add("lines", JsonBuilder.array().addStrings(this.lines).build())
                    .build();
        }

        @Override
        public boolean isClosed() {
            return !this.spawned;
        }

        @Override
        public void close() {
            this.despawn();
            this.viewers.clear();
            this.joinSubscription.close();
        }

        private PacketContainer[] getMetaPackets() {
            return this.streamWithIndex(this.spawnedEntities.stream()).map(entityArmorStandEntry -> {

                int index = entityArmorStandEntry.getKey();
                EntityArmorStand entityArmorStand = entityArmorStandEntry.getValue();

                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);

                packet.getIntegers().write(0, entityArmorStand.getId());

                WrappedDataWatcher wrappedWatchableObjects = new WrappedDataWatcher();

                wrappedWatchableObjects.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
                wrappedWatchableObjects.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(String.class)), this.lines.get(index));
                wrappedWatchableObjects.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true);
                wrappedWatchableObjects.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true);
                wrappedWatchableObjects.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(10, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x04 | 0x08));

                packet.getWatchableCollectionModifier().write(0, wrappedWatchableObjects.getWatchableObjects());

                return packet;

            }).toArray(PacketContainer[]::new);
        }

        private PacketContainer[] getSpawnPackets() {
            return this.streamWithIndex(this.spawnedEntities.stream()).map(entityArmorStandEntry -> {

                int index = entityArmorStandEntry.getKey();
                EntityArmorStand entityArmorStand = entityArmorStandEntry.getValue();

                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

                Position position = this.getNewLinePosition(index);
                // entity ID
                packet.getIntegers().write(0, entityArmorStand.getId());
                //uuid
                packet.getUUIDs().write(0, UUID.randomUUID());
                //type
                packet.getIntegers().write(1, (int) EntityType.ARMOR_STAND.getTypeId());
                //positions
                packet.getDoubles().write(0, position.getX());
                packet.getDoubles().write(1, position.getY());
                packet.getDoubles().write(2, position.getZ());

                return packet;

            }).toArray(PacketContainer[]::new);
        }

        private PacketContainer[] getDespawnPackets() {
            return this.spawnedEntities.stream().map(EntityArmorStand::getId).map(id -> {

                PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                packet.getIntegerArrays().write(0, new int[]{id});

                return packet;

            }).toArray(PacketContainer[]::new);
        }

        private void sendPacket(Player player, PacketContainer packetContainer) {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException exception) {
                exception.printStackTrace();
            }
        }
    }
}
