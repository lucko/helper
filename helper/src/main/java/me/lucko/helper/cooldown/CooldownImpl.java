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
import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

@NonnullByDefault
class CooldownImpl implements Cooldown {

    // when the last test occurred.
    private long lastTested;

    // the cooldown duration in millis
    private final long timeout;

    CooldownImpl(long amount, TimeUnit unit) {
        this.timeout = unit.toMillis(amount);
        this.lastTested = 0;
    }

    @Override
    public OptionalLong getLastTested() {
        return this.lastTested == 0 ? OptionalLong.empty() : OptionalLong.of(this.lastTested);
    }

    @Override
    public void setLastTested(long time) {
        if (time <= 0) {
            this.lastTested = 0;
        } else {
            this.lastTested = time;
        }
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public CooldownImpl copy() {
        return new CooldownImpl(this.timeout, TimeUnit.MILLISECONDS);
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