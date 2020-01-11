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

package me.lucko.helper.cooldown;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.lucko.helper.gson.GsonSerializable;
import me.lucko.helper.scheduler.Ticks;
import me.lucko.helper.time.Time;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * A simple cooldown abstraction
 */
public interface Cooldown extends GsonSerializable {
    static Cooldown deserialize(JsonElement element) {
        Preconditions.checkArgument(element.isJsonObject());
        JsonObject object = element.getAsJsonObject();

        Preconditions.checkArgument(object.has("lastTested"));
        Preconditions.checkArgument(object.has("timeout"));

        long lastTested = object.get("lastTested").getAsLong();
        long timeout = object.get("timeout").getAsLong();

        Cooldown c = of(timeout, TimeUnit.MILLISECONDS);
        c.setLastTested(lastTested);
        return c;
    }

    /**
     * Creates a cooldown lasting a number of game ticks
     *
     * @param ticks the number of ticks
     * @return a new cooldown
     */
    @Nonnull
    static Cooldown ofTicks(long ticks) {
        return new CooldownImpl(Ticks.to(ticks, TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a cooldown lasting a specified amount of time
     *
     * @param amount the amount of time
     * @param unit the unit of time
     * @return a new cooldown
     */
    @Nonnull
    static Cooldown of(long amount, @Nonnull TimeUnit unit) {
        return new CooldownImpl(amount, unit);
    }

    /**
     * Returns true if the cooldown is not active, and then resets the timer
     *
     * <p>If the cooldown is currently active, the timer is <strong>not</strong> reset.</p>
     *
     * @return true if the cooldown is not active
     */
    default boolean test() {
        if (!testSilently()) {
            return false;
        }

        reset();
        return true;
    }

    /**
     * Returns true if the cooldown is not active
     *
     * @return true if the cooldown is not active
     */
    default boolean testSilently() {
        return elapsed() > getTimeout();
    }

    /**
     * Returns the elapsed time in milliseconds since the cooldown was last reset, or since creation time
     *
     * @return the elapsed time
     */
    default long elapsed() {
        return Time.nowMillis() - getLastTested().orElse(0);
    }

    /**
     * Resets the cooldown
     */
    default void reset() {
        setLastTested(Time.nowMillis());
    }

    /**
     * Gets the time in milliseconds until the cooldown will become inactive.
     *
     * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
     *
     * @return the time in millis until the cooldown will expire
     */
    default long remainingMillis() {
        long diff = elapsed();
        return diff > getTimeout() ? 0L : getTimeout() - diff;
    }

    /**
     * Gets the time until the cooldown will become inactive.
     *
     * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
     *
     * @param unit the unit to return in
     * @return the time until the cooldown will expire
     */
    default long remainingTime(TimeUnit unit) {
        return Math.max(0L, unit.convert(remainingMillis(), TimeUnit.MILLISECONDS));
    }

    /**
     * Return the time in milliseconds when this cooldown was last {@link #test()}ed.
     *
     * @return the last call time
     */
    @Nonnull
    OptionalLong getLastTested();

    /**
     * Sets the time in milliseconds when this cooldown was last tested.
     *
     * <p>Note: this should only be used when re-constructing a cooldown
     * instance. Use {@link #test()} otherwise.</p>
     *
     * @param time the time
     */
    void setLastTested(long time);

    /**
     * Gets the timeout in milliseconds for this cooldown
     *
     * @return the timeout in milliseconds
     */
    long getTimeout();

    /**
     * Copies the properties of this cooldown to a new instance
     *
     * @return a cloned cooldown instance
     */
    @Nonnull
    Cooldown copy();

}
