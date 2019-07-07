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

package me.lucko.helper.messaging.util;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

/**
 * Receives a message from a channel, sent at a fixed interval.
 *
 * @param <T> the message type
 */
public final class ChannelReceiver<T> {

    private T value;

    private long timestamp = 0;
    private final long expiryMillis;

    public ChannelReceiver(long expiryDuration, @Nonnull TimeUnit unit) {
        this.expiryMillis = unit.toMillis(expiryDuration);
    }

    public void set(T value) {
        this.value = value;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Gets the last known value.
     *
     * @return the last known value
     */
    public Optional<T> getLastKnownValue() {
        return Optional.ofNullable(this.value);
    }

    /**
     * Gets the value.
     *
     * <p>Returns empty if the expiry on the last known value has been exceeded.</p>
     *
     * @return the value
     */
    public Optional<T> getValue() {
        long now = System.currentTimeMillis();
        long diff = now - this.timestamp;
        if (diff > this.expiryMillis) {
            return Optional.empty();
        }
        return getLastKnownValue();
    }

    /**
     * Gets the timestamp when the last value was received.
     *
     * @return the last received timestamp
     */
    public OptionalLong getLastReceivedTimestamp() {
        return this.timestamp == 0 ? OptionalLong.empty() : OptionalLong.of(this.timestamp);
    }

    @Override
    public String toString() {
        return "ChannelReceiver{value=" + this.value + ", timestamp=" + this.timestamp + ", expiryMillis=" + this.expiryMillis + '}';
    }
}
