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
public final class Position {
    public static Position deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("x"));
        Preconditions.checkArgument(object.has("y"));
        Preconditions.checkArgument(object.has("z"));
        Preconditions.checkArgument(object.has("world"));

        double x = object.get("x").getAsDouble();
        double y = object.get("y").getAsDouble();
        double z = object.get("z").getAsDouble();
        String world = object.get("world").getAsString();

        return of(x, y, z, world);
    }

    public static Position of(double x, double y, double z, String world) {
        return new Position(x, y, z, world);
    }

    public static Position of(Location location) {
        return of(location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
    }

    public static Position of(Block block) {
        return of(block.getLocation());
    }

    private final double x;
    private final double y;
    private final double z;
    private final String world;

    private Location bukkitLocation = null;

    private Position(double x, double y, double z, String world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public String getWorld() {
        return this.world;
    }

    public synchronized Location toLocation() {
        if (bukkitLocation == null) {
            bukkitLocation = new Location(Bukkit.getWorld(world), x, y, z);
        }

        return bukkitLocation.clone();
    }

    public BlockPosition floor() {
        return BlockPosition.of(bukkitFloor(x), bukkitFloor(y), bukkitFloor(z), world);
    }

    public Position getRelative(BlockFace face) {
        return Position.of(x + face.getModX(), y + face.getModY(), z + face.getModZ(), world);
    }

    public Position getRelative(BlockFace face, double distance) {
        return Position.of(x + (face.getModX() * distance), y + (face.getModY() * distance), z + (face.getModZ() * distance), world);
    }

    public Position add(double x, double y, double z) {
        return Position.of(this.x + x, this.y + y, this.z + z, world);
    }

    public Position subtract(double x, double y, double z) {
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
        if (!(o instanceof Position)) return false;
        final Position other = (Position) o;
        return Double.compare(this.getX(), other.getX()) == 0 &&
                Double.compare(this.getY(), other.getY()) == 0 &&
                Double.compare(this.getZ(), other.getZ()) == 0 &&
                (this.getWorld() == null ? other.getWorld() == null : this.getWorld().equals(other.getWorld()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;

        final long x = Double.doubleToLongBits(this.getX());
        final long y = Double.doubleToLongBits(this.getY());
        final long z = Double.doubleToLongBits(this.getZ());

        result = result * PRIME + (int) (x >>> 32 ^ x);
        result = result * PRIME + (int) (y >>> 32 ^ y);
        result = result * PRIME + (int) (z >>> 32 ^ z);
        result = result * PRIME + (this.getWorld() == null ? 43 : this.getWorld().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Position(x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + ", world=" + this.getWorld() + ")";
    }

    private static int bukkitFloor(double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

}
