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

package me.lucko.helper.serialize;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.lucko.helper.Helper;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable and serializable block location object
 */
public final class BlockPosition implements GsonSerializable {
    public static BlockPosition deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("x"));
        Preconditions.checkArgument(object.has("y"));
        Preconditions.checkArgument(object.has("z"));
        Preconditions.checkArgument(object.has("world"));

        int x = object.get("x").getAsInt();
        int y = object.get("y").getAsInt();
        int z = object.get("z").getAsInt();
        String world = object.get("world").getAsString();

        return of(x, y, z, world);
    }

    public static BlockPosition of(int x, int y, int z, String world) {
        Objects.requireNonNull(world, "world");
        return new BlockPosition(x, y, z, world);
    }

    public static BlockPosition of(int x, int y, int z, World world) {
        Objects.requireNonNull(world, "world");
        return of(x, y, z, world.getName());
    }

    public static BlockPosition of(Vector3i vector, String world) {
        Objects.requireNonNull(world, "world");
        return of(vector.getX(), vector.getY(), vector.getZ(), world);
    }

    public static BlockPosition of(Vector3i vector, World world) {
        Objects.requireNonNull(world, "world");
        return of(vector.getX(), vector.getY(), vector.getZ(), world);
    }

    public static BlockPosition of(Location location) {
        Objects.requireNonNull(location, "location");
        return of(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    public static BlockPosition of(Block block) {
        Objects.requireNonNull(block, "block");
        return of(block.getLocation());
    }

    private final int x;
    private final int y;
    private final int z;
    private final String world;

    @Nullable
    private Location bukkitLocation = null;

    private BlockPosition(int x, int y, int z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public String getWorld() {
        return this.world;
    }

    public synchronized Location toLocation() {
        if (this.bukkitLocation == null) {
            this.bukkitLocation = new Location(Helper.worldNullable(this.world), this.x, this.y, this.z);
        }

        return this.bukkitLocation.clone();
    }

    public Vector3i toVector() {
        return new Vector3i(this.x, this.y, this.z);
    }

    public Block toBlock() {
        return toLocation().getBlock();
    }

    public Position toPosition() {
        return Position.of(this.x, this.y, this.z, this.world);
    }

    public Position toPositionCenter() {
        return Position.of(this.x + 0.5d, this.y + 0.5d, this.z + 0.5d, this.world);
    }

    public ChunkPosition toChunk() {
        return ChunkPosition.of(this.x >> 4, this.z >> 4, this.world);
    }

    public boolean contains(Position position) {
        return equals(position.floor());
    }

    public BlockPosition getRelative(BlockFace face) {
        Objects.requireNonNull(face, "face");
        return BlockPosition.of(this.x + face.getModX(), this.y + face.getModY(), this.z + face.getModZ(), this.world);
    }

    public BlockPosition getRelative(BlockFace face, int distance) {
        Objects.requireNonNull(face, "face");
        return BlockPosition.of(this.x + (face.getModX() * distance), this.y + (face.getModY() * distance), this.z + (face.getModZ() * distance), this.world);
    }

    public BlockPosition add(Vector3i vector3i) {
        return add(vector3i.getX(), vector3i.getY(), vector3i.getZ());
    }

    public BlockPosition add(int x, int y, int z) {
        return BlockPosition.of(this.x + x, this.y + y, this.z + z, this.world);
    }

    public BlockPosition subtract(Vector3i vector3i) {
        return subtract(vector3i.getX(), vector3i.getY(), vector3i.getZ());
    }

    public BlockPosition subtract(int x, int y, int z) {
        return add(-x, -y, -z);
    }

    public BlockRegion regionWith(BlockPosition other) {
        Objects.requireNonNull(other, "other");
        return BlockRegion.of(this, other);
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        return JsonBuilder.object()
                .add("x", this.x)
                .add("y", this.y)
                .add("z", this.z)
                .add("world", this.world)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BlockPosition)) return false;
        final BlockPosition other = (BlockPosition) o;
        return this.getX() == other.getX() && this.getY() == other.getY() && this.getZ() == other.getZ() && this.getWorld().equals(other.getWorld());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        result = result * PRIME + this.getZ();
        result = result * PRIME + this.getWorld().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BlockPosition(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", world=" + this.getWorld() + ")";
    }

}