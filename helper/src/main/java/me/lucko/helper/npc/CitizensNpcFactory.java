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

package me.lucko.helper.npc;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.metadata.ExpiringValue;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.metadata.MetadataMap;
import me.lucko.helper.terminable.composite.CompositeTerminable;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.trait.LookClose;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of {@link NpcFactory} using Citizens.
 */
public class CitizensNpcFactory implements NpcFactory {
    private static final MetadataKey<Boolean> RECENT_NPC_CLICK_KEY = MetadataKey.createBooleanKey("helper-recent-npc-click");

    private NPCRegistry npcRegistry;
    private final CompositeTerminable registry = CompositeTerminable.create();

    public CitizensNpcFactory() {

        // create an enable hook for citizens
        Events.subscribe(PluginEnableEvent.class)
                .filter(e -> e.getPlugin().getName().equals("Citizens"))
                .expireAfter(1) // only call once
                .handler(e -> init());
    }

    private void init() {
        // create npc registry
        this.npcRegistry = CitizensAPI.createNamedNPCRegistry("helper", new MemoryNPCDataStore());

        // ensure our trait is registered
        registerTrait();

        // handle click events
        Events.merge(NPCClickEvent.class, NPCRightClickEvent.class, NPCLeftClickEvent.class)
                .handler(e -> handleClick(e.getNPC(), e.getClicker())).bindWith(this.registry);

        // don't let players move npcs
        Events.subscribe(PlayerFishEvent.class)
                .filter(e -> e.getCaught() != null)
                .filter(e -> isHelperNpc(e.getCaught()))
                .handler(e -> e.setCancelled(true))
                .bindWith(this.registry);

        /* Events.subscribe(ProjectileCollideEvent.class)
                .filter(e -> e.getCollidedWith() != null)
                .filter(e -> isHelperNpc(e.getCollidedWith()))
                .handler(e -> e.setCancelled(true))
                .bindWith(this.registry); */

        Events.subscribe(EntityDamageByEntityEvent.class)
                .filter(e -> isHelperNpc(e.getEntity()))
                .handler(e -> e.setCancelled(true))
                .bindWith(this.registry);

        // update npcs every 10 ticks
        Schedulers.sync().runRepeating(this::tickNpcs, 10L, 10L).bindWith(this.registry);
    }

    private boolean isHelperNpc(Entity entity) {
        NPC npc = this.npcRegistry.getNPC(entity);
        return npc != null && npc.hasTrait(ClickableTrait.class);
    }

    private void handleClick(NPC npc, Player clicker) {
        if (npc.hasTrait(ClickableTrait.class)) {
            if (processMetadata(clicker)) {
                return;
            }
            npc.getTrait(ClickableTrait.class).onClick(clicker);
        }
    }

    // returns true if the action should be blocked
    private boolean processMetadata(Player p) {
        return !Metadata.provideForPlayer(p).putIfAbsent(RECENT_NPC_CLICK_KEY, ExpiringValue.of(true, 100, TimeUnit.MILLISECONDS));
    }

    private void tickNpcs() {
        for (NPC npc : this.npcRegistry) {
            if (!npc.isSpawned() || !npc.hasTrait(ClickableTrait.class)) continue;

            Npc helperNpc = npc.getTrait(ClickableTrait.class).npc;

            // ensure npcs stay in the same position
            Location loc = npc.getEntity().getLocation();
            if (loc.getBlockX() != helperNpc.getInitialSpawn().getBlockX() || loc.getBlockZ() != helperNpc.getInitialSpawn().getBlockZ()) {
                npc.teleport(helperNpc.getInitialSpawn().clone(), PlayerTeleportEvent.TeleportCause.PLUGIN);
            }

            // don't let players stand near npcs
            for (Entity entity : npc.getStoredLocation().getWorld().getNearbyEntities(npc.getStoredLocation(), 1.0, 1.0, 1.0)) {
                if (!(entity instanceof Player) || this.npcRegistry.isNPC(entity)) continue;

                final Player p = (Player) entity;

                if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }

                if (npc.getEntity().getLocation().distance(p.getLocation()) < 3.5) {
                    p.setVelocity(p.getLocation().getDirection().multiply(-0.5).setY(0.4));
                }
            }
        }
    }

    @Nonnull
    @Override
    public CitizensNpc spawnNpc(@Nonnull Location location, @Nonnull String nametag, @Nonnull String skinPlayer) {
        return spawnNpc(location.clone(), nametag, npc -> npc.setSkin(skinPlayer));
    }

    @Nonnull
    @Override
    public CitizensNpc spawnNpc(@Nonnull Location location, @Nonnull String nametag, @Nonnull String skinTextures, @Nonnull String skinSignature) {
        return spawnNpc(location.clone(), nametag, npc -> npc.setSkin(skinTextures, skinSignature));
    }

    private CitizensNpc spawnNpc(Location location, String nametag, Consumer<Npc> skin) {
        Objects.requireNonNull(this.npcRegistry, "npcRegistry");

        // create a new npc
        NPC npc = this.npcRegistry.createNPC(EntityType.PLAYER, nametag);

        // add the trait
        ClickableTrait trait = new ClickableTrait();
        npc.addTrait(trait);

        // create a new helperNpc instance
        CitizensNpc helperNpc = new NpcImpl(npc, trait, location.clone());
        trait.npc = helperNpc;

        // apply the skin and spawn it
        skin.accept(helperNpc);
        npc.spawn(location);

        return helperNpc;
    }

    @Override
    public void close() {
        if (this.npcRegistry != null) {
            this.npcRegistry.deregisterAll();
        }
        this.registry.closeAndReportException();
    }

    private static void registerTrait() {
        if (CitizensAPI.getTraitFactory().getTrait(ClickableTrait.class) == null) {
            try {
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ClickableTrait.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static final class ClickableTrait extends Trait {
        private final MetadataMap meta = MetadataMap.create();
        private Consumer<Player> clickCallback = null;
        private Npc npc = null;

        public ClickableTrait() {
            super("helper_clickable");
        }

        @Override
        public void onSpawn() {
            super.onSpawn();
            ensureLook(getNPC().getTrait(LookClose.class));
        }

        private void ensureLook(LookClose lookTraitInstance) {
            if (lookTraitInstance.toggle()) {
                return;
            }
            lookTraitInstance.toggle();
        }

        private void onClick(Player player) {
            if (this.clickCallback != null) {
                this.clickCallback.accept(player);
            }
        }
    }

    private static final class NpcImpl implements CitizensNpc {
        private final NPC npc;
        private final ClickableTrait trait;
        private final Location initialSpawn;

        private NpcImpl(NPC npc, ClickableTrait trait, Location initialSpawn) {
            this.npc = npc;
            this.trait = trait;
            this.initialSpawn = initialSpawn;
        }

        @Override
        public void setClickCallback(@Nullable Consumer<Player> clickCallback) {
            this.trait.clickCallback = clickCallback;
        }

        @Nonnull
        @Override
        public MetadataMap getMeta() {
            return this.trait.meta;
        }

        @Override
        @Deprecated
        public void setSkin(@Nonnull String skinPlayer) {
            try {
                this.npc.data().set(NPC.PLAYER_SKIN_UUID_METADATA, skinPlayer);
                this.npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, true);

                if (this.npc instanceof SkinnableEntity) {
                    ((SkinnableEntity) this.npc).getSkinTracker().notifySkinChange(true);
                }
                if (this.npc.getEntity() instanceof SkinnableEntity) {
                    ((SkinnableEntity) this.npc.getEntity()).getSkinTracker().notifySkinChange(true);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setSkin(@Nonnull String textures, @Nonnull String signature) {
            try {
                this.npc.data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, textures);
                this.npc.data().set(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, signature);
                this.npc.data().set("cached-skin-uuid-name", "null");
                this.npc.data().set("player-skin-name", "null");
                this.npc.data().set("cached-skin-uuid", UUID.randomUUID().toString());
                this.npc.data().set(NPC.PLAYER_SKIN_USE_LATEST, false);

                if (this.npc instanceof SkinnableEntity) {
                    ((SkinnableEntity) this.npc).getSkinTracker().notifySkinChange(true);
                }
                if (this.npc.getEntity() instanceof SkinnableEntity) {
                    ((SkinnableEntity) this.npc.getEntity()).getSkinTracker().notifySkinChange(true);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public void setName(@Nonnull String name) {
            this.npc.setName(name);
        }

        @Override
        public void setShowNametag(boolean show) {
            this.npc.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, show);
        }

        @Nonnull
        @Override
        public Location getInitialSpawn() {
            return this.initialSpawn;
        }

        @Nonnull
        @Override
        public NPC getNpc() {
            return this.npc;
        }
    }
}
