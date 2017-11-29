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

package me.lucko.helper.scheduler;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Utility for converting between Minecraft game ticks and standard durations.
 */
public final class Ticks {
    // the number of ticks which occur in a second - this is a server implementation detail
    public static final int TICKS_PER_SECOND = 20;
    // the number of milliseconds in a second - constant
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // the number of milliseconds in a tick - assuming the server runs at a perfect tick rate
    public static final int MILLISECONDS_PER_TICK = MILLISECONDS_PER_SECOND / TICKS_PER_SECOND;

    /**
     * Converts a duration in a certain unit of time to ticks.
     *
     * <p><code>Ticks.from(duration)</code> returns the number of ticks <b>from</b> the given duration.</p>
     *
     * @param duration the duration of time
     * @param unit the unit the duration is in
     * @return the number of ticks which represent the duration
     */
    public static long from(long duration, @Nonnull TimeUnit unit) {
        return unit.toMillis(duration) / MILLISECONDS_PER_TICK;
    }

    /**
     * Converts ticks to a duration in a certain unit of time.
     *
     * <p><code>Ticks.to(ticks)</code> converts the number of ticks <b>to</b> a duration.</p>
     *
     * @param ticks the number of ticks
     * @param unit the unit to return the duration in
     * @return a duration value in the given unit, representing the number of ticks
     */
    public static long to(long ticks, @Nonnull TimeUnit unit) {
        return unit.convert(ticks * MILLISECONDS_PER_TICK, TimeUnit.MILLISECONDS);
    }

    private Ticks() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
