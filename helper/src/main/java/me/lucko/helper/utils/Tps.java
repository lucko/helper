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

package me.lucko.helper.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.gson.JsonBuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * Encapsulates the servers "ticks per second" (TPS) reading.
 */
public final class Tps implements GsonSerializable {

    private static final Supplier<double[]> SUPPLIER = getSupplier();
    private static Supplier<double[]> getSupplier() {
        try {
            Method spigotMethod = Bukkit.getServer().getClass().getMethod("spigot");
            Method getTPSMethod = Class.forName("org.bukkit.Server$Spigot").getMethod("getTPS");
            Object spigot = spigotMethod.invoke(Bukkit.getServer());
            return () -> {
                try {
                    return (double[]) getTPSMethod.invoke(spigot);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (Exception e) {
            // ignore
        }

        try {
            Method getTPSMethod = Bukkit.class.getMethod("getTPS");
            return () -> {
                try {
                    return (double[]) getTPSMethod.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (Exception e) {
            // ignore
        }

        return null;
    }

    public static boolean isReadSupported() {
        return SUPPLIER != null;
    }

    public static Tps read() {
        if (!isReadSupported()) {
            throw new UnsupportedOperationException("Unable to supply server tps");
        }
        return new Tps(SUPPLIER.get());
    }

    private final double avg1;
    private final double avg5;
    private final double avg15;
    private final double[] asArray;

    public Tps(double avg1, double avg5, double avg15) {
        this.avg1 = avg1;
        this.avg5 = avg5;
        this.avg15 = avg15;
        this.asArray = new double[]{avg1, avg5, avg15};
    }

    public Tps(double[] values) {
        this.avg1 = values[0];
        this.avg5 = values[1];
        this.avg15 = values[2];
        this.asArray = values;
    }

    public double avg1() {
        return this.avg1;
    }

    public double avg5() {
        return this.avg5;
    }

    public double avg15() {
        return this.avg15;
    }

    public double[] asArray() {
        return this.asArray;
    }

    public String toFormattedString() {
        return String.join(", ", format(this.avg1), format(this.avg5), format(this.avg15));
    }

    public static String format(double tps) {
        StringBuilder sb = new StringBuilder();
        if (tps > 18.0) {
            sb.append(ChatColor.GREEN);
        } else if (tps > 16.0) {
            sb.append(ChatColor.YELLOW);
        } else {
            sb.append(ChatColor.RED);
        }

        sb.append(Math.min(Math.round(tps * 100.0) / 100.0, 20.0));

        if (tps > 20.0) {
            sb.append('*');
        }

        return sb.toString();
    }

    @Nonnull
    @Override
    public JsonElement serialize() {
        return JsonBuilder.object()
                .add("avg1", this.avg1)
                .add("avg5", this.avg5)
                .add("avg15", this.avg15)
                .build();
    }

    public static Tps deserialize(JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        return new Tps(
                jsonObject.get("avg1").getAsDouble(),
                jsonObject.get("avg5").getAsDouble(),
                jsonObject.get("avg15").getAsDouble()
        );
    }
}
