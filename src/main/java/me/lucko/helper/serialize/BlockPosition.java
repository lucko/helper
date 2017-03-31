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

package me.lucko.helper.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * An immutable and serializable location object
 */
public final class BlockPosition {
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
        return new BlockPosition(x, y, z, world, null);
    }

    public static BlockPosition of(Location location) {
        return of(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName()).setBukkitLocation(location);
    }

    public static BlockPosition of(Block block) {
        return of(block.getLocation());
    }

    private final int x;
    private final int y;
    private final int z;
    private final String world;

    private Location bukkitLocation;

    private BlockPosition(int x, int y, int z, String world, Location bukkitLocation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.bukkitLocation = bukkitLocation;
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
        if (bukkitLocation == null) {
            bukkitLocation = new Location(Bukkit.getWorld(world), x, y, z);
        }

        return bukkitLocation;
    }

    public Block toBlock() {
        return toLocation().getBlock();
    }

    private BlockPosition setBukkitLocation(Location bukkitLocation) {
        this.bukkitLocation = bukkitLocation;
        return this;
    }

    public BlockPosition getRelative(BlockFace face) {
        return BlockPosition.of(x + face.getModX(), y + face.getModY(), z + face.getModZ(), world);
    }

    public BlockPosition getRelative(BlockFace face, int distance) {
        return BlockPosition.of(x + (face.getModX() * distance), y + (face.getModY() * distance), z + (face.getModZ() * distance), world);
    }

    public BlockPosition add(int x, int y, int z) {
        return BlockPosition.of(this.x + x, this.y + y, this.z + z, world);
    }

    public BlockPosition subtract(int x, int y, int z) {
        return add(-x, -y, -z);
    }

    public JsonObject serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("x", x);
        object.addProperty("y", y);
        object.addProperty("z", z);
        object.addProperty("world", world);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BlockPosition)) return false;
        final BlockPosition other = (BlockPosition) o;
        return this.getX() == other.getX() &&
                this.getY() == other.getY() &&
                this.getZ() == other.getZ() &&
                (this.getWorld() == null ? other.getWorld() == null : this.getWorld().equals(other.getWorld()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.getX();
        result = result * PRIME + this.getY();
        result = result * PRIME + this.getZ();
        result = result * PRIME + (this.getWorld() == null ? 43 : this.getWorld().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "BlockPosition(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", world=" + this.getWorld() + ")";
    }

}