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

package me.lucko.helper.utils;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.stream.Stream;

public final class Players {

    public static Stream<? extends Player> stream() {
        return Bukkit.getOnlinePlayers().stream();
    }

    public static void forEach(Consumer<Player> consumer) {
        Bukkit.getOnlinePlayers().forEach(consumer);
    }

    public static Stream<? extends Player> streamInRange(Location center, double radius) {
        return center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e));
    }

    public static void forEachInRange(Location center, double radius, Consumer<Player> consumer) {
        center.getWorld().getNearbyEntities(center, radius, radius, radius).stream()
                .filter(e -> e instanceof Player)
                .map(e -> ((Player) e))
                .forEach(consumer);
    }

    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
    }

    public static void playSound(Player player, Location location, Sound sound) {
        player.playSound(location, sound, 1.0f, 1.0f);
    }

    public static void playSound(Location location, Sound sound) {
        location.getWorld().playSound(location, sound, 1.0f, 1.0f);
    }

    @SuppressWarnings("deprecation")
    public static void sendBlockChange(Player player, Location loc, Material type, int data) {
        player.sendBlockChange(loc, type, (byte) data);
    }

    public static void sendBlockChange(Player player, Block block, Material type, int data) {
        sendBlockChange(player, block.getLocation(), type, data);
    }

    public static void sendBlockChange(Player player, Location loc, Material type) {
        sendBlockChange(player, loc, type, 0);
    }

    public static void sendBlockChange(Player player, Block block, Material type) {
        sendBlockChange(player, block, type, 0);
    }

    public static void spawnParticle(Player player, Location location, Particle particle) {
        player.spawnParticle(particle, location, 1);
    }

    public static void spawnParticle(Location location, Particle particle) {
        location.getWorld().spawnParticle(particle, location, 1);
    }

    public static void spawnParticle(Player player, Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        player.spawnParticle(particle, location, amount);
    }

    public static void spawnParticle(Location location, Particle particle, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        location.getWorld().spawnParticle(particle, location, amount);
    }

    public static void spawnParticleOffset(Player player, Location location, Particle particle, double offset) {
        player.spawnParticle(particle, location, 1, offset, offset, offset);
    }

    public static void spawnParticleOffset(Location location, Particle particle, double offset) {
        location.getWorld().spawnParticle(particle, location, 1, offset, offset, offset);
    }

    public static void spawnParticleOffset(Player player, Location location, Particle particle, int amount, double offset) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        player.spawnParticle(particle, location, amount, offset, offset, offset);
    }

    public static void spawnParticleOffset(Location location, Particle particle, int amount, double offset) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        location.getWorld().spawnParticle(particle, location, amount, offset, offset, offset);
    }

    public static void spawnEffect(Player player, Location location, Effect effect) {
        player.playEffect(location, effect, null);
    }

    public static void spawnEffect(Location location, Effect effect) {
        location.getWorld().playEffect(location, effect, null);
    }

    public static void spawnEffect(Player player, Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        for (int i = 0; i < amount; i++) {
            player.playEffect(location, effect, null);
        }
    }

    public static void spawnEffect(Location location, Effect effect, int amount) {
        Preconditions.checkArgument(amount > 0, "amount > 0");
        for (int i = 0; i < amount; i++) {
            location.getWorld().playEffect(location, effect, null);
        }
    }

    public static void resetWalkSpeed(Player player) {
        player.setWalkSpeed(0.2f);
    }

    public static void resetFlySpeed(Player player) {
        player.setFlySpeed(0.1f);
    }

    private Players() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
