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

import java.time.Instant;

import javax.annotation.Nonnull;

public final class TimeUtil {

    public static long now() {
        return Instant.now().toEpochMilli();
    }

    public static long nowUnix() {
        return Instant.now().getEpochSecond();
    }

    @Nonnull
    public static String toShortForm(long seconds) {
        if (seconds == 0) {
            return "0s";
        }

        long minute = seconds / 60;
        seconds = seconds % 60;
        long hour = minute / 60;
        minute = minute % 60;
        long day = hour / 24;
        hour = hour % 24;

        StringBuilder time = new StringBuilder();
        if (day != 0) {
            time.append(day).append("d ");
        }
        if (hour != 0) {
            time.append(hour).append("h ");
        }
        if (minute != 0) {
            time.append(minute).append("m ");
        }
        if (seconds != 0) {
            time.append(seconds).append("s");
        }

        return time.toString().trim();
    }

    @Nonnull
    public static String toLongForm(long seconds) {
        if (seconds == 0) {
            return "0 seconds";
        }

        long minute = seconds / 60;
        seconds = seconds % 60;
        long hour = minute / 60;
        minute = minute % 60;
        long day = hour / 24;
        hour = hour % 24;

        StringBuilder time = new StringBuilder();
        if (day != 0) {
            time.append(day);
        }
        if (day == 1) {
            time.append(" day ");
        } else if (day > 1) {
            time.append(" days ");
        }
        if (hour != 0) {
            time.append(hour);
        }
        if (hour == 1) {
            time.append(" hour ");
        } else if (hour > 1) {
            time.append(" hours ");
        }
        if (minute != 0) {
            time.append(minute);
        }
        if (minute == 1) {
            time.append(" minute ");
        } else if (minute > 1) {
            time.append(" minutes ");
        }
        if (seconds!= 0) {
            time.append(seconds);
        }
        if (seconds == 1) {
            time.append(" second");
        } else if (seconds > 1) {
            time.append(" seconds");
        }

        return time.toString().trim();
    }

    private TimeUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
