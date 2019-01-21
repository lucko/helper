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

package me.lucko.helper.setting;

import java.util.BitSet;

/**
 * Represents a map of states of a given realm of settings.
 *
 * @param <S> the setting type
 */
public final class BooleanSettingMap<S extends BooleanSetting> {
    private final BooleanSettingMapFactory<S> factory;
    private final BitSet bits;

    BooleanSettingMap(BooleanSettingMapFactory<S> factory, BitSet bits) {
        this.factory = factory;
        this.bits = bits;
    }

    /**
     * Gets the state of a given setting.
     *
     * @param setting the setting
     * @return the state
     */
    public boolean get(S setting) {
        return this.bits.get(setting.ordinal());
    }

    /**
     * Sets the state of a given setting.
     *
     * @param setting the setting
     * @param state the state to set
     * @return the previous state of the setting
     */
    public boolean set(S setting, boolean state) {
        if (state == get(setting)) {
            return state;
        }

        if (state) {
            this.bits.set(setting.ordinal());
        } else {
            this.bits.clear(setting.ordinal());
        }
        return !state;
    }

    /**
     * Toggles the state of a setting.
     *
     * @param setting the setting
     * @return the new state
     */
    public boolean toggle(S setting) {
        this.bits.flip(setting.ordinal());
        return get(setting);
    }

    /**
     * Returns if this map has any settings with states differing from the defaults.
     *
     * @return if this map differs from the defaults
     */
    public boolean isDifferentFromDefault() {
        return this.factory.isDifferentFromDefault(this.bits);
    }

    /**
     * Encodes the state of each of the settings in this map to a byte array.
     *
     * @return the bytes
     */
    public byte[] encode() {
        return this.bits.toByteArray();
    }

    /**
     * Encodes the state of each of the settings in this map to a string.
     *
     * @return the string
     */
    public String encodeToString() {
        return SettingMapFactory.ENCODING.encode(encode());
    }

    /**
     * Returns a readable string representation of the map.
     *
     * <p>Consists of a list of the ordinals of the setting set to a true state.</p>
     *
     * @return a readable string representation of the map
     */
    @Override
    public String toString() {
        return this.bits.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanSettingMap<?> that = (BooleanSettingMap<?>) o;
        return this.factory.equals(that.factory) &&
                this.bits.equals(that.bits);
    }

    @Override
    public int hashCode() {
        return this.bits.hashCode();
    }
}
