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

import com.flowpowered.math.vector.Vector3d;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable and serializable vector + direction object
 */
public final class VectorPoint implements GsonSerializable {
    public static VectorPoint deserialize(JsonElement element) {
        Vector3d position = VectorSerializers.deserialize3d(element);
        Direction direction = Direction.deserialize(element);

        return of(position, direction);
    }

    public static VectorPoint of(Vector3d position, Direction direction) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(direction, "direction");
        return new VectorPoint(position, direction);
    }

    public static VectorPoint of(Location location) {
        Objects.requireNonNull(location, "location");
        return of(Position.of(location).toVector(), Direction.from(location));
    }

    public static VectorPoint of(Point point) {
        Objects.requireNonNull(point, "point");
        return of(point.getPosition().toVector(), point.getDirection());
    }

    private final Vector3d position;
    private final Direction direction;

    @Nullable
    private Location bukkitLocation = null;

    private VectorPoint(Vector3d position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    public Vector3d getPosition() {
        return this.position;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public synchronized Location toLocation(World world) {
        if (this.bukkitLocation == null || !this.bukkitLocation.getWorld().equals(world)) {
            this.bukkitLocation = new Location(world, this.position.getX(), this.position.getY(), this.position.getZ(), this.direction.getYaw(), this.direction.getPitch());
        }

        return this.bukkitLocation.clone();
    }

    public VectorPoint add(double x, double y, double z) {
        return of(this.position.add(x, y, z), this.direction);
    }

    public VectorPoint subtract(double x, double y, double z) {
        return of(this.position.sub(x, y, z), this.direction);
    }

    public Point withWorld(String world) {
        Objects.requireNonNull(world, "world");
        return Point.of(Position.of(this.position, world), this.direction);
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        return JsonBuilder.object()
                .addAll(VectorSerializers.serialize(this.position))
                .addAll(this.direction.serialize())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof VectorPoint)) return false;
        final VectorPoint other = (VectorPoint) o;
        return this.getPosition().equals(other.getPosition()) && this.getDirection().equals(other.getDirection());
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;

        result = result * PRIME + this.getPosition().hashCode();
        result = result * PRIME + this.getDirection().hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "VectorPoint(position=" + this.getPosition() + ", direction=" + this.getDirection() + ")";
    }
}
