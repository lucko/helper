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

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2f;
import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector2l;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector3i;
import com.flowpowered.math.vector.Vector3l;
import com.flowpowered.math.vector.Vector4d;
import com.flowpowered.math.vector.Vector4f;
import com.flowpowered.math.vector.Vector4i;
import com.flowpowered.math.vector.Vector4l;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.lucko.helper.gson.JsonBuilder;

/**
 * Utility for serializing and deserializing flowpowered Vector instances
 */
public final class VectorSerializers {

    public static JsonObject serialize(Vector2d vector2d) {
        return JsonBuilder.object()
                .add("x", vector2d.getX())
                .add("y", vector2d.getY())
                .build();
    }

    public static Vector2d deserialize2d(JsonElement element) {
        return new Vector2d(
                element.getAsJsonObject().get("x").getAsDouble(),
                element.getAsJsonObject().get("y").getAsDouble()
        );
    }

    public static JsonObject serialize(Vector2f vector2f) {
        return JsonBuilder.object()
                .add("x", vector2f.getX())
                .add("y", vector2f.getY())
                .build();
    }

    public static Vector2f deserialize2f(JsonElement element) {
        return new Vector2f(
                element.getAsJsonObject().get("x").getAsFloat(),
                element.getAsJsonObject().get("y").getAsFloat()
        );
    }

    public static JsonObject serialize(Vector2i vector2i) {
        return JsonBuilder.object()
                .add("x", vector2i.getX())
                .add("y", vector2i.getY())
                .build();
    }

    public static Vector2i deserialize2i(JsonElement element) {
        return new Vector2i(
                element.getAsJsonObject().get("x").getAsInt(),
                element.getAsJsonObject().get("y").getAsInt()
        );
    }

    public static JsonObject serialize(Vector2l vector2l) {
        return JsonBuilder.object()
                .add("x", vector2l.getX())
                .add("y", vector2l.getY())
                .build();
    }

    public static Vector2l deserialize2l(JsonElement element) {
        return new Vector2l(
                element.getAsJsonObject().get("x").getAsLong(),
                element.getAsJsonObject().get("y").getAsLong()
        );
    }

    public static JsonObject serialize(Vector3d vector3d) {
        return JsonBuilder.object()
                .add("x", vector3d.getX())
                .add("y", vector3d.getY())
                .add("z", vector3d.getZ())
                .build();
    }

    public static Vector3d deserialize3d(JsonElement element) {
        return new Vector3d(
                element.getAsJsonObject().get("x").getAsDouble(),
                element.getAsJsonObject().get("y").getAsDouble(),
                element.getAsJsonObject().get("z").getAsDouble()
        );
    }

    public static JsonObject serialize(Vector3f vector3f) {
        return JsonBuilder.object()
                .add("x", vector3f.getX())
                .add("y", vector3f.getY())
                .add("z", vector3f.getZ())
                .build();
    }

    public static Vector3f deserialize3f(JsonElement element) {
        return new Vector3f(
                element.getAsJsonObject().get("x").getAsFloat(),
                element.getAsJsonObject().get("y").getAsFloat(),
                element.getAsJsonObject().get("z").getAsFloat()
        );
    }

    public static JsonObject serialize(Vector3i vector3i) {
        return JsonBuilder.object()
                .add("x", vector3i.getX())
                .add("y", vector3i.getY())
                .add("z", vector3i.getZ())
                .build();
    }

    public static Vector3i deserialize3i(JsonElement element) {
        return new Vector3i(
                element.getAsJsonObject().get("x").getAsInt(),
                element.getAsJsonObject().get("y").getAsInt(),
                element.getAsJsonObject().get("z").getAsInt()
        );
    }

    public static JsonObject serialize(Vector3l vector3l) {
        return JsonBuilder.object()
                .add("x", vector3l.getX())
                .add("y", vector3l.getY())
                .add("z", vector3l.getZ())
                .build();
    }

    public static Vector3l deserialize3l(JsonElement element) {
        return new Vector3l(
                element.getAsJsonObject().get("x").getAsLong(),
                element.getAsJsonObject().get("y").getAsLong(),
                element.getAsJsonObject().get("z").getAsLong()
        );
    }

    public static JsonObject serialize(Vector4d vector4d) {
        return JsonBuilder.object()
                .add("x", vector4d.getX())
                .add("y", vector4d.getY())
                .add("z", vector4d.getZ())
                .add("w", vector4d.getW())
                .build();
    }

    public static Vector4d deserialize4d(JsonElement element) {
        return new Vector4d(
                element.getAsJsonObject().get("x").getAsDouble(),
                element.getAsJsonObject().get("y").getAsDouble(),
                element.getAsJsonObject().get("z").getAsDouble(),
                element.getAsJsonObject().get("w").getAsDouble()
        );
    }

    public static JsonObject serialize(Vector4f vector4f) {
        return JsonBuilder.object()
                .add("x", vector4f.getX())
                .add("y", vector4f.getY())
                .add("z", vector4f.getZ())
                .add("w", vector4f.getW())
                .build();
    }

    public static Vector4f deserialize4f(JsonElement element) {
        return new Vector4f(
                element.getAsJsonObject().get("x").getAsFloat(),
                element.getAsJsonObject().get("y").getAsFloat(),
                element.getAsJsonObject().get("z").getAsFloat(),
                element.getAsJsonObject().get("w").getAsFloat()
        );
    }

    public static JsonObject serialize(Vector4i vector4i) {
        return JsonBuilder.object()
                .add("x", vector4i.getX())
                .add("y", vector4i.getY())
                .add("z", vector4i.getZ())
                .add("w", vector4i.getW())
                .build();
    }

    public static Vector4i deserialize4i(JsonElement element) {
        return new Vector4i(
                element.getAsJsonObject().get("x").getAsInt(),
                element.getAsJsonObject().get("y").getAsInt(),
                element.getAsJsonObject().get("z").getAsInt(),
                element.getAsJsonObject().get("w").getAsInt()
        );
    }

    public static JsonObject serialize(Vector4l vector4l) {
        return JsonBuilder.object()
                .add("x", vector4l.getX())
                .add("y", vector4l.getY())
                .add("z", vector4l.getZ())
                .add("w", vector4l.getW())
                .build();
    }

    public static Vector4l deserialize4l(JsonElement element) {
        return new Vector4l(
                element.getAsJsonObject().get("x").getAsLong(),
                element.getAsJsonObject().get("y").getAsLong(),
                element.getAsJsonObject().get("z").getAsLong(),
                element.getAsJsonObject().get("w").getAsLong()
        );
    }

    private VectorSerializers() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
