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

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * An immutable and serializable direction object
 */
public final class Direction implements GsonSerializable {
    public static final Direction ZERO = Direction.of(0.0f, 0.0f);

    public static Direction deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("yaw"));
        Preconditions.checkArgument(object.has("pitch"));

        float yaw = object.get("yaw").getAsFloat();
        float pitch = object.get("pitch").getAsFloat();

        return of(yaw, pitch);
    }

    public static Direction of(float yaw, float pitch) {
        return new Direction(yaw, pitch);
    }

    public static Direction from(Location location) {
        Objects.requireNonNull(location, "location");
        return of(location.getYaw(), location.getPitch());
    }

    private final float yaw;
    private final float pitch;

    private Direction(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    @Nonnull
    @Override
    public JsonObject serialize() {
        return JsonBuilder.object()
                .add("yaw", this.yaw)
                .add("pitch", this.pitch)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Direction)) return false;
        final Direction other = (Direction) o;
        return Float.compare(this.getYaw(), other.getYaw()) == 0 &&
                Float.compare(this.getPitch(), other.getPitch()) == 0;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;

        result = result * PRIME + Float.floatToIntBits(this.getYaw());
        result = result * PRIME + Float.floatToIntBits(this.getPitch());

        return result;
    }

    @Override
    public String toString() {
        return "Direction(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }

}
