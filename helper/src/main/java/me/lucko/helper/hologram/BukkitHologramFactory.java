public class BukkitHologramFactory implements HologramFactory {

    @Nonnull
    @Override
    public Hologram newHologram(@Nonnull Position position, @Nonnull List<String> lines) {
        return new BukkitHologram(position, lines);
    }

    @Nonnull
    @Override
    public IndividualHologram newIndividualHologram(@Nonnull Position position, @Nonnull List<String> lines) {
        return new BukkitIndividualHologram(position, lines);
    }

    private static class BukkitHologram implements Hologram {

        private Position position;
        private final List<String> lines = Lists.newArrayList();
        private final List<ArmorStand> spawnedEntities = Lists.newArrayList();
        private boolean spawned = false;

        private CompositeTerminable listeners = null;
        private Consumer<Player> clickCallback = null;

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
                return Position.of(last.getLocation()).subtract(0.0d, 0.25d, 0.0d);
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
                }
            }

            // now enough armorstands are spawned, we can now update the text
            for (int i = 0; i < this.lines.size(); i++) {
                String line = this.lines.get(i);

                if (i >= this.spawnedEntities.size()) {
                    // add a new line
                    Location loc = getNewLinePosition().toLocation();

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

                    if (MinecraftVersion.getRuntimeVersion().isAfter(MinecraftVersions.v1_8)) {
                        as.setAI(false);
                        as.setCollidable(false);
                        as.setInvulnerable(true);
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
                Events.subscribe(PlayerInteractAtEntityEvent.class)
                        .filter(e -> e.getRightClicked() instanceof ArmorStand)
                        .handler(e -> {
                            Player p = e.getPlayer();
                            ArmorStand as = (ArmorStand) e.getRightClicked();

                            if (this.spawnedEntities.stream().anyMatch(as::equals)) {
                                e.setCancelled(true);
                                this.clickCallback.accept(p);
                            }
                        })
                        .bindWith(this.listeners);

                Events.subscribe(EntityDamageByEntityEvent.class)
                        .filter(e -> e.getEntity() instanceof ArmorStand)
                        .filter(e -> e.getDamager() instanceof Player)
                        .handler(e -> {
                            Player p = (Player) e.getDamager();
                            ArmorStand as = (ArmorStand) e.getEntity();

                            this.spawnedEntities.stream().filter(as::equals).findFirst().ifPresent(armorStand -> {
                                e.setCancelled(true);
                                this.clickCallback.accept(p);
                            });
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

        private <T> Stream<Map.Entry<Integer, T>> streamWithIndex(Stream<? extends T> stream) {
            return Streams.iterate(new Iterator<Map.Entry<Integer, T>>() {
                private final Iterator<? extends T> streamIterator = stream.iterator();
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return streamIterator.hasNext();
                }

                @Override
                public Map.Entry<Integer, T> next() {
                    return new AbstractMap.SimpleImmutableEntry<>(index++, streamIterator.next());
                }
            }, false);
        }
    }
}
