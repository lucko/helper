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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

/**
 * Creates and decodes {@link BooleanSettingMap}s for a given realm of settings.
 *
 * <p>It is safe to introduce additional settings, so long as the
 * ordinal values of existing ones are not affected.</p>
 *
 * @param <S> the setting type
 */
public final class BooleanSettingMapFactory<S extends BooleanSetting> {

    /**
     * Creates a new {@link BooleanSettingMapFactory} for the given {@link BooleanSetting} enum.
     *
     * <p>Factories should ideally be cached (stored in a static field) in the
     * setting enum and reused.</p>
     *
     * @param settingsEnum the setting class
     * @param <S> the class type
     * @return a new factory
     */
    public static <S extends Enum<S> & BooleanSetting> BooleanSettingMapFactory<S> create(Class<S> settingsEnum) {
        Objects.requireNonNull(settingsEnum, "settingsEnum");
        return create(settingsEnum.getEnumConstants());
    }

    /**
     * Creates a new {@link BooleanSettingMapFactory} for the given {@link BooleanSetting}s.
     *
     * <p>Factories should ideally be cached (stored in a static field) in the
     * setting class.</p>
     *
     * @param settings the settings
     * @param <S> the class type
     * @return a new factory
     */
    public static <S extends BooleanSetting> BooleanSettingMapFactory<S> create(S[] settings) {
        Objects.requireNonNull(settings, "settings");

        BitSet defaultBits = new BitSet();
        for (int i = 0; i < settings.length; i++) {
            S setting = settings[i];

            // ensure ordinal has been correctly implemented
            if (setting.ordinal() != i) {
                throw new IllegalArgumentException("The ordinal of setting " + setting + " does not equal its array index. ordinal=" + setting.ordinal() + ", index=" + i);
            }

            if (setting.defaultState()) {
                defaultBits.set(i);
            }
        }
        return new BooleanSettingMapFactory<>(settings, defaultBits);
    }

    private final S[] settings;
    private final BitSet defaultBits;

    private BooleanSettingMapFactory(S[] settings, BitSet defaultBits) {
        this.settings = settings;
        this.defaultBits = defaultBits;
    }

    /**
     * Gets the {@link BooleanSetting} instances.
     *
     * @return the setting instances
     */
    public S[] getSettings() {
        return Arrays.copyOf(this.settings, this.settings.length);
    }

    /**
     * Returns if the given set of bits differs from the defaults.
     *
     * @param bits the bits to compare
     * @return true if different
     */
    boolean isDifferentFromDefault(BitSet bits) {
        return !this.defaultBits.equals(bits);
    }

    /**
     * Creates a new {@link BooleanSettingMap}, with the default states set for each of the settings.
     *
     * @return the new map
     */
    public BooleanSettingMap<S> newMap() {
        return new BooleanSettingMap<>(this, BitSet.valueOf(this.defaultBits.toLongArray()));
    }

    /**
     * Decodes the given byte array to a {@link BooleanSettingMap}.
     *
     * <p>Operates on the reverse of {@link BooleanSettingMap#encode()}.</p>
     *
     * @param buf the byte array
     * @return the decoded map
     */
    public BooleanSettingMap<S> decode(byte[] buf) {
        BitSet bits = BitSet.valueOf(buf);
        if (bits.length() > this.settings.length) {
            bits.clear(this.settings.length, bits.length());
        }
        return new BooleanSettingMap<>(this, bits);
    }

    /**
     * Decodes the given string to a {@link BooleanSettingMap}.
     *
     * <p>Operates on the reverse of {@link BooleanSettingMap#encodeToString()}.</p>
     *
     * @param encodedString the string
     * @return the decoded map
     */
    public BooleanSettingMap<S> decode(String encodedString) {
        return decode(SettingMapFactory.ENCODING.decode(encodedString));
    }
}
