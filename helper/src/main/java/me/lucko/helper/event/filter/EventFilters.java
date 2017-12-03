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

package me.lucko.helper.event.filter;

import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;

import org.bukkit.event.Cancellable;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

/**
 * Defines standard event predicates for use in functional event handlers.
 */
@SuppressWarnings("unchecked")
public final class EventFilters {

    private static final Predicate<? extends Cancellable> IGNORE_CANCELLED = e -> !e.isCancelled();
    private static final Predicate<? extends Cancellable> IGNORE_UNCANCELLED = Cancellable::isCancelled;
    private static final Predicate<? extends PlayerLoginEvent> IGNORE_DISALLOWED_LOGIN = e -> e.getResult() == PlayerLoginEvent.Result.ALLOWED;
    private static final Predicate<? extends AsyncPlayerPreLoginEvent> IGNORE_DISALLOWED_PRE_LOGIN = e -> e.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED;

    private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_BLOCK = e ->
            e.getFrom().getBlockX() != e.getTo().getBlockX() ||
            e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
            e.getFrom().getBlockY() != e.getTo().getBlockY() ||
            !e.getFrom().getWorld().equals(e.getTo().getWorld());

    private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_BLOCK_AND_Y = e ->
            e.getFrom().getBlockX() != e.getTo().getBlockX() ||
            e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
            !e.getFrom().getWorld().equals(e.getTo().getWorld());

    private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_CHUNK = e ->
            (e.getFrom().getBlockX() >> 4) != (e.getTo().getBlockX() >> 4) ||
            (e.getFrom().getBlockZ() >> 4) != (e.getTo().getBlockZ() >> 4) ||
            !e.getFrom().getWorld().equals(e.getTo().getWorld());

    /**
     * Returns a predicate which only returns true if the event isn't cancelled
     *
     * @param <T> the event type
     * @return a predicate which only returns true if the event isn't cancelled
     */
    @Nonnull
    public static <T extends Cancellable> Predicate<T> ignoreCancelled() {
        return (Predicate<T>) IGNORE_CANCELLED;
    }

    /**
     * Returns a predicate which only returns true if the event is cancelled
     *
     * @param <T> the event type
     * @return a predicate which only returns true if the event is cancelled
     */
    @Nonnull
    public static <T extends Cancellable> Predicate<T> ignoreNotCancelled() {
        return (Predicate<T>) IGNORE_UNCANCELLED;
    }

    /**
     * Returns a predicate which only returns true if the login is allowed
     *
     * @param <T> the event type
     * @return a predicate which only returns true if the login is allowed
     */
    @Nonnull
    public static <T extends PlayerLoginEvent> Predicate<T> ignoreDisallowedLogin() {
        return (Predicate<T>) IGNORE_DISALLOWED_LOGIN;
    }

    /**
     * Returns a predicate which only returns true if the login is allowed
     *
     * @param <T> the event type
     * @return a predicate which only returns true if the login is allowed
     */
    @Nonnull
    public static <T extends AsyncPlayerPreLoginEvent> Predicate<T> ignoreDisallowedPreLogin() {
        return (Predicate<T>) IGNORE_DISALLOWED_PRE_LOGIN;
    }

    /**
     * Returns a predicate which only returns true if the player has moved over a block
     *
     * @param <T> the event type
     * @return a predicate which only returns true if the player has moved over a block
     */
    @Nonnull
    public static <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlock() {
        return (Predicate<T>) IGNORE_SAME_BLOCK;
    }

    /**
     * Returns a predicate which only returns true if the player has moved over a block, not including movement
     * directly up and down. (so jumping wouldn't return true)
     *
     * @param <T> the event type
     * @return a predicate which only returns true if the player has moved across a block border
     */
    @Nonnull
    public static <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlockAndY() {
        return (Predicate<T>) IGNORE_SAME_BLOCK_AND_Y;
    }

    /**
     * Returns a predicate which only returns true if the player has moved over a chunk border
     *
     * @param <T> the event type
     * @return a predicate which only returns true if the player has moved over a chunk border
     */
    @Nonnull
    public static <T extends PlayerMoveEvent> Predicate<T> ignoreSameChunk() {
        return (Predicate<T>) IGNORE_SAME_CHUNK;
    }

    /**
     * Returns a predicate which only returns true if the entity has a given metadata key
     *
     * @param key the metadata key
     * @param <T> the event type
     * @return a predicate which only returns true if the entity has a given metadata key
     */
    @Nonnull
    public static <T extends EntityEvent> Predicate<T> entityHasMetadata(MetadataKey<?> key) {
        return e -> Metadata.provideForEntity(e.getEntity()).has(key);
    }

    /**
     * Returns a predicate which only returns true if the player has a given metadata key
     *
     * @param key the metadata key
     * @param <T> the event type
     * @return a predicate which only returns true if the player has a given metadata key
     */
    @Nonnull
    public static <T extends PlayerEvent> Predicate<T> playerHasMetadata(MetadataKey<?> key) {
        return e -> Metadata.provideForPlayer(e.getPlayer()).has(key);
    }

    /**
     * Returns a predicate which only returns true if the player has the given permission
     *
     * @param permission the permission
     * @param <T> the event type
     * @return a predicate which only returns true if the player has the given permission
     */
    @Nonnull
    public static <T extends PlayerEvent> Predicate<T> playerHasPermission(String permission) {
        return e -> e.getPlayer().hasPermission(permission);
    }

    private EventFilters() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
