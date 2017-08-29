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

import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;

import org.bukkit.Location;

import javax.annotation.Nonnull;

/**
 * An immutable and serializable region object
 */
public final class Region implements GsonSerializable {
    public static Region deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("min"));
        Preconditions.checkArgument(object.has("max"));

        Position a = Position.deserialize(object.get("min"));
        Position b = Position.deserialize(object.get("max"));

        return of(a, b);
    }

    public static Region of(Position a, Position b) {
        Preconditions.checkNotNull(a, "a");
        Preconditions.checkNotNull(b, "b");

        if (!a.getWorld().equals(b.getWorld())) {
            throw new IllegalArgumentException("positions are in different worlds");
        }

        return new Region(a, b);
    }

    private final Position min;
    private final Position max;

    private final double width;
    private final double height;
    private final double depth;

    private Region(Position a, Position b) {
        min = Position.of(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()), a.getWorld());
        max = Position.of(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()), a.getWorld());

        width = max.getX() - min.getX();
        height = max.getY() - min.getX();
        depth = max.getZ() - min.getZ();
    }

    public boolean inRegion(Position pos) {
        Preconditions.checkNotNull(pos, "pos");
        return pos.getWorld().equals(min.getWorld()) && inRegion(pos.getX(), pos.getY(), pos.getZ());
    }

    public boolean inRegion(Location loc) {
        Preconditions.checkNotNull(loc, "loc");
        return loc.getWorld().getName().equals(min.getWorld()) && inRegion(loc.getX(), loc.getY(), loc.getZ());
    }

    public boolean inRegion(double x, double y, double z) {
        return x >= min.getX() && x <= max.getX()
                && y >= min.getY() && y <= max.getY()
                && z >= min.getZ() && z <= max.getZ();
    }

    public Position getMin() {
        return this.min;
    }

    public Position getMax() {
        return this.max;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public double getDepth() {
        return this.depth;
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        return JsonBuilder.object()
                .add("min", min)
                .add("max", max)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Region)) return false;
        final Region other = (Region) o;
        return (this.getMin() == null ? other.getMin() == null : this.getMin().equals(other.getMin())) &&
                (this.getMax() == null ? other.getMax() == null : this.getMax().equals(other.getMax()));
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.getMin() == null ? 43 : this.getMin().hashCode());
        result = result * PRIME + (this.getMax() == null ? 43 : this.getMax().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Region(min=" + this.getMin() + ", max=" + this.getMax() + ")";
    }

}
