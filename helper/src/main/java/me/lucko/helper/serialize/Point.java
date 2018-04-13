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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.lucko.helper.Helper;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;

import org.bukkit.Location;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An immutable and serializable position + direction object
 */
public final class Point implements GsonSerializable {
    public static Point deserialize(JsonElement element) {
        Position position = Position.deserialize(element);
        Direction direction = Direction.deserialize(element);

        return of(position, direction);
    }

    public static Point of(Position position, Direction direction) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(direction, "direction");
        return new Point(position, direction);
    }

    public static Point of(Location location) {
        Objects.requireNonNull(location, "location");
        return of(Position.of(location), Direction.from(location));
    }

    private final Position position;
    private final Direction direction;

    @Nullable
    private Location bukkitLocation = null;

    private Point(Position position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    public Position getPosition() {
        return this.position;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public synchronized Location toLocation() {
        if (this.bukkitLocation == null) {
            this.bukkitLocation = new Location(Helper.worldNullable(this.position.getWorld()), this.position.getX(), this.position.getY(), this.position.getZ(), this.direction.getYaw(), this.direction.getPitch());
        }

        return this.bukkitLocation.clone();
    }

    public VectorPoint toVectorPoint() {
        return VectorPoint.of(this);
    }

    public Point add(double x, double y, double z) {
        return this.position.add(x, y, z).withDirection(this.direction);
    }

    public Point subtract(double x, double y, double z) {
        return this.position.subtract(x, y, z).withDirection(this.direction);
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        return JsonBuilder.object()
                .addAll(this.position.serialize())
                .addAll(this.direction.serialize())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Point)) return false;
        final Point other = (Point) o;
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
        return "Point(position=" + this.getPosition() + ", direction=" + this.getDirection() + ")";
    }
}
