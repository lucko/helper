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

package me.lucko.helper.time;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A collection of time related utilities.
 */
public final class Time {

    /**
     * Gets the current unix time in milliseconds.
     *
     * @return the current unix time
     */
    public static long nowMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Gets the current unix time in seconds.
     *
     * @return the current unix time
     */
    public static long nowSeconds() {
        return nowMillis() / 1000L;
    }

    /**
     * Gets the current unix time as an {@link Instant}.
     *
     * @return the current unix time
     */
    public static Instant now() {
        return Instant.ofEpochMilli(System.currentTimeMillis());
    }

    /**
     * Gets the difference between {@link #now() now} and another instant.
     *
     * @param other the other instant
     * @return the difference
     */
    public static Duration diffToNow(Instant other) {
        return Duration.between(now(), other).abs();
    }

    /**
     * Gets a {@link Duration} for a {@link TimeUnit} and amount.
     *
     * @param unit the unit
     * @param amount the amount
     * @return the duration
     */
    public static Duration duration(TimeUnit unit, long amount) {
        Objects.requireNonNull(unit, "unit");
        switch (unit) {
            case NANOSECONDS:
                return Duration.ofNanos(amount);
            case MICROSECONDS:
                return Duration.ofNanos(TimeUnit.MICROSECONDS.toNanos(amount));
            case MILLISECONDS:
                return Duration.ofMillis(amount);
            case SECONDS:
                return Duration.ofSeconds(amount);
            case MINUTES:
                return Duration.ofMinutes(amount);
            case HOURS:
                return Duration.ofHours(amount);
            case DAYS:
                return Duration.ofDays(amount);
            default:
                throw new AssertionError("unknown time unit: " + unit);

        }
    }

    private Time() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
