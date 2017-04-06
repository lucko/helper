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

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public class Cooldown implements LongSupplier {
    public static long now() {
        return System.currentTimeMillis();
    }

    public static long nowUnix() {
        return System.currentTimeMillis() / 1000L;
    }

    public static Cooldown ofTicks(long ticks) {
        return new Cooldown(ticks * 50L, TimeUnit.MILLISECONDS);
    }

    public static Cooldown of(long amount, TimeUnit unit) {
        return new Cooldown(amount, unit);
    }

    // when the last test occurred.
    private long lastCalled;

    // the cooldown duration in millis
    private long timeout;

    private Cooldown(long amount, TimeUnit unit) {
        timeout = unit.toMillis(amount);

        // allow #test to pass immediately.
        lastCalled = now() - timeout;
    }

    // returns true if the cooldown is not active, and resets the timer
    public boolean test() {
        if (!testSilently()) {
            return false;
        }

        reset();
        return true;
    }

    // returns true if the cooldown is not active
    public boolean testSilently() {
        return elapsed() > timeout;
    }

    public long elapsed() {
        return now() - lastCalled;
    }

    public void reset() {
        lastCalled = now();
    }

    public long remainingMillis() {
        long diff = elapsed();
        return diff > timeout ? 0L : timeout - diff;
    }

    public long remainingTime(TimeUnit unit) {
        return Math.max(0L, unit.convert(remainingMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public long getAsLong() {
        return remainingMillis();
    }
}
