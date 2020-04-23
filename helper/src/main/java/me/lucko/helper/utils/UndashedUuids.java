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

import java.util.UUID;

/**
 * Utilities for converting {@link UUID} string representations without dashes.
 */
public final class UndashedUuids {

    /**
     * Returns a {@link UUID#toString()} string without dashes.
     *
     * @param uuid the uuid
     * @return the string form
     */
    public static String toString(UUID uuid) {
        // copied from UUID impl
        return (digits(uuid.getMostSignificantBits() >> 32, 8) +
                digits(uuid.getMostSignificantBits() >> 16, 4) +
                digits(uuid.getMostSignificantBits(), 4) +
                digits(uuid.getLeastSignificantBits() >> 48, 4) +
                digits(uuid.getLeastSignificantBits(), 12));
    }

    private static String digits(long val, int digits) {
        long hi = 1L << (digits * 4);
        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }


    /**
     * Parses a UUID from an undashed string.
     *
     * @param string the string
     * @return the uuid
     */
    public static UUID fromString(String string) throws IllegalArgumentException {
        if (string.length() != 32) {
            throw new IllegalArgumentException("Invalid length " + string.length() + ": " + string);
        }

        try {
            return new UUID(
                    Long.parseUnsignedLong(string.substring(0, 16), 16),
                    Long.parseUnsignedLong(string.substring(16), 16)
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid uuid string: " + string, e);
        }

    }

    private UndashedUuids() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}