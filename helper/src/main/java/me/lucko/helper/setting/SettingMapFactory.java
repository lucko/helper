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

import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.Arrays;
import java.util.Objects;

/**
 * Creates and decodes {@link SettingMap}s for a given realm of settings and states.
 *
 * <p>It is safe to introduce additional settings and states, so long as the
 * ordinal values of existing ones are not affected.</p>
 *
 * @param <S> the setting type
 */
public final class SettingMapFactory<S extends Setting<V>, V extends Setting.State> {

    /** The instance used for encoding/decoding setting maps */
    static final BaseEncoding ENCODING = BaseEncoding.base64().omitPadding();

    /**
     * Creates a new {@link SettingMapFactory} for the given {@link Setting} and {@link Setting.State} enums.
     *
     * <p>Factories should ideally be cached (stored in a static field) in the
     * setting enum and reused.</p>
     *
     * @param settingsEnum the setting enum
     * @param statesEnum the states enum
     * @param <S> the class type
     * @return a new factory
     */
    public static <S extends Enum<S> & Setting<V>, V extends Enum<V> & Setting.State> SettingMapFactory<S, V> create(Class<S> settingsEnum, Class<V> statesEnum) {
        Objects.requireNonNull(settingsEnum, "settingsEnum");
        Objects.requireNonNull(statesEnum, "statesEnum");
        return create(settingsEnum.getEnumConstants(), statesEnum.getEnumConstants());
    }

    /**
     * Creates a new {@link SettingMapFactory} for the given {@link Setting}s and {@link Setting.State}s.
     *
     * <p>Factories should ideally be cached (stored in a static field) in the
     * setting class.</p>
     *
     * @param settings the settings
     * @param <S> the class type
     * @return a new factory
     */
    public static <S extends Setting<V>, V extends Setting.State> SettingMapFactory<S, V> create(S[] settings, V[] states) {
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(states, "states");

        // ensure the number of settings can fit into a byte
        // we encode the number of "changes" from default as a byte.
        if (settings.length > 255) {
            throw new IllegalArgumentException("number of settings cannot be greater than 255");
        }

        // ensure the ordinal of any of the states can fit into a byte
        // means indexes run from 0-255
        if (states.length > 256) {
            throw new IllegalArgumentException("number of states cannot be greater than 256");
        }

        // ensure ordinal has been correctly implemented for the states
        for (int i = 0; i < states.length; i++) {
            V state = states[i];
            if (state.ordinal() != i) {
                throw new IllegalArgumentException("The ordinal of state " + state + " does not equal its array index. ordinal=" + state.ordinal() + ", index=" + i);
            }
        }

        byte[] defaultStates = new byte[settings.length];
        for (int i = 0; i < settings.length; i++) {
            S setting = settings[i];

            // ensure ordinal has been correctly implemented for the settings
            if (setting.ordinal() != i) {
                throw new IllegalArgumentException("The ordinal of setting " + setting + " does not equal its array index. ordinal=" + setting.ordinal() + ", index=" + i);
            }

            defaultStates[i] = (byte) setting.defaultState().ordinal();
        }

        return new SettingMapFactory<>(settings, states, defaultStates);
    }

    final S[] settings;
    final V[] states;

    // the index corresponds to the setting ordinal, the value corresponds to the state ordinal
    private final byte[] defaultStates;

    private SettingMapFactory(S[] settings, V[] states, byte[] defaultStates) {
        this.settings = settings;
        this.states = states;
        this.defaultStates = defaultStates;
    }

    /**
     * Gets the {@link Setting} instances.
     *
     * @return the setting instances
     */
    public S[] getSettings() {
        return Arrays.copyOf(this.settings, this.settings.length);
    }

    /**
     * Gets the {@link Setting.State} instances.
     *
     * @return the state instances
     */
    public V[] getStates() {
        return Arrays.copyOf(this.states, this.states.length);
    }

    /**
     * Returns if the given set of states differs from the defaults.
     *
     * @param states the states to compare
     * @return true if different
     */
    boolean isDifferentFromDefault(byte[] states) {
        return !Arrays.equals(this.defaultStates, states);
    }

    byte[] encode(byte[] states) {
        // calculate the number of differences
        int n = 0;
        for (int i = 0; i < states.length; i++) {
            byte state = states[i];
            byte defaultState = this.defaultStates[i];

            if (state == defaultState) {
                continue;
            }

            n++;
        }

        if (n == 0) {
            return new byte[0];
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        // write the number of differences to "expect"
        out.writeByte((byte) n);

        for (int i = 0; i < states.length; i++) {
            byte state = states[i];
            byte defaultState = this.defaultStates[i];

            if (state == defaultState) {
                continue;
            }

            byte settingOrdinal = (byte) i;
            out.writeByte(settingOrdinal);
            out.writeByte(state);
        }

        return out.toByteArray();
    }

    /**
     * Creates a new {@link SettingMap}, with the default states set for each of the settings.
     *
     * @return the new map
     */
    public SettingMap<S, V> newMap() {
        return new SettingMap<>(this, Arrays.copyOf(this.defaultStates, this.defaultStates.length));
    }

    /**
     * Decodes the given byte array to a {@link SettingMap}.
     *
     * <p>Operates on the reverse of {@link SettingMap#encode()}.</p>
     *
     * @param buf the byte array
     * @return the decoded map
     */
    public SettingMap<S, V> decode(byte[] buf) {
        if (buf.length == 0) {
            return newMap();
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(buf);
        int n = Byte.toUnsignedInt(in.readByte());

        byte[] states = Arrays.copyOf(this.defaultStates, this.defaultStates.length);

        for (int i = 0; i < n; i++) {
            int settingOrdinal = Byte.toUnsignedInt(in.readByte());
            byte stateByte = in.readByte();

            states[settingOrdinal] = stateByte;
        }

        return new SettingMap<>(this, states);
    }

    /**
     * Decodes the given string to a {@link SettingMap}.
     *
     * <p>Operates on the reverse of {@link SettingMap#encodeToString()}.</p>
     *
     * @param encodedString the string
     * @return the decoded map
     */
    public SettingMap<S, V> decode(String encodedString) {
        return decode(ENCODING.decode(encodedString));
    }
}
