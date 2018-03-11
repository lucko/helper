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

import com.google.gson.JsonElement;

import me.lucko.helper.gson.JsonBuilder;
import me.lucko.helper.utils.TimeUtil;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import javax.annotation.Nonnull;

@NonnullByDefault
class CooldownImpl implements LongSupplier, Cooldown {

    // when the last test occurred.
    private long lastTested;

    // the cooldown duration in millis
    private final long timeout;

    CooldownImpl(long amount, TimeUnit unit) {
        this.timeout = unit.toMillis(amount);
        this.lastTested = 0;
    }

    /**
     * Returns true if the cooldown is not active, and then resets the timer
     *
     * <p>If the cooldown is currently active, the timer is <strong>not</strong> reset.</p>
     *
     * @return true if the cooldown is not active
     */
    @Override
    public boolean test() {
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
    @Override
    public boolean testSilently() {
        return elapsed() > this.timeout;
    }

    /**
     * Returns the elapsed time in milliseconds since the cooldown was last reset, or since creation time
     *
     * @return the elapsed time
     */
    @Override
    public long elapsed() {
        return TimeUtil.now() - this.lastTested;
    }

    /**
     * Resets the cooldown
     */
    @Override
    public void reset() {
        this.lastTested = TimeUtil.now();
    }

    /**
     * Gets the time in milliseconds until the cooldown will become inactive.
     *
     * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
     *
     * @return the time in millis until the cooldown will expire
     */
    @Override
    public long remainingMillis() {
        long diff = elapsed();
        return diff > this.timeout ? 0L : this.timeout - diff;
    }

    /**
     * Gets the time until the cooldown will become inactive.
     *
     * <p>If the cooldown is not active, this method returns <code>0</code>.</p>
     *
     * @param unit the unit to return in
     * @return the time until the cooldown will expire
     */
    @Override
    public long remainingTime(TimeUnit unit) {
        return Math.max(0L, unit.convert(remainingMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public long getAsLong() {
        return remainingMillis();
    }

    /**
     * Gets the timeout in milliseconds for this cooldown
     *
     * @return the timeout in milliseconds
     */
    @Override
    public long getTimeout() {
        return this.timeout;
    }

    /**
     * Copies the properties of this cooldown to a new instance
     *
     * @return a cloned cooldown instance
     */
    @Override
    public CooldownImpl copy() {
        return new CooldownImpl(this.timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Return the time in milliseconds when this cooldown was last {@link #test()}ed.
     *
     * @return the last call time
     */
    @Override
    public OptionalLong getLastTested() {
        return this.lastTested == 0 ? OptionalLong.empty() : OptionalLong.of(this.lastTested);
    }

    /**
     * Sets the time in milliseconds when this cooldown was last tested.
     *
     * <p>Note: this should only be used when re-constructing a cooldown
     * instance. Use {@link #test()} otherwise.</p>
     *
     * @param time the time
     */
    @Override
    public void setLastTested(long time) {
        if (time <= 0) {
            this.lastTested = 0;
        } else {
            this.lastTested = time;
        }
    }

    @Nonnull
    @Override
    public JsonElement serialize() {
        return JsonBuilder.object()
                .add("lastTested", lastTested)
                .add("timeout", timeout)
                .build();
    }
}