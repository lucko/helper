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

package me.lucko.helper.metadata;

import com.google.common.reflect.TypeToken;

import me.lucko.helper.cooldown.Cooldown;
import me.lucko.helper.interfaces.TypeAware;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Objects;
import java.util.UUID;

/**
 * A MetadataKey can be mapped to values in a {@link MetadataMap}.
 *
 * <p>Unlike a normal map key, a MetadataKey also holds the type of the values mapped to it.</p>
 *
 * @param <T> the value type
 */
@NonnullByDefault
public interface MetadataKey<T> extends TypeAware<T> {

    /**
     * Creates a MetadataKey with the given id and type
     *
     * @param id the id of the key
     * @param type the type of the value mapped to this key
     * @param <T> the value type
     * @return a new metadata key
     */
    static <T> MetadataKey<T> create(String id, TypeToken<T> type) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        return new MetadataKeyImpl<>(id, type);
    }

    /**
     * Creates a MetadataKey with the given id and type
     *
     * @param id the id of the key
     * @param clazz the class type of the value mapped to this key
     * @param <T> the value type
     * @return a new metadata key
     */
    static <T> MetadataKey<T> create(String id, Class<T> clazz) {
        return create(id, TypeToken.of(clazz));
    }

    static MetadataKey<Empty> createEmptyKey(String id) {
        return create(id, Empty.class);
    }

    static MetadataKey<String> createStringKey(String id) {
        return create(id, String.class);
    }

    static MetadataKey<Boolean> createBooleanKey(String id) {
        return create(id, Boolean.class);
    }

    static MetadataKey<Integer> createIntegerKey(String id) {
        return create(id, Integer.class);
    }

    static MetadataKey<Long> createLongKey(String id) {
        return create(id, Long.class);
    }

    static MetadataKey<Double> createDoubleKey(String id) {
        return create(id, Double.class);
    }

    static MetadataKey<Float> createFloatKey(String id) {
        return create(id, Float.class);
    }

    static MetadataKey<Short> createShortKey(String id) {
        return create(id, Short.class);
    }

    static MetadataKey<Character> createCharacterKey(String id) {
        return create(id, Character.class);
    }

    static MetadataKey<Cooldown> createCooldownKey(String id) {
        return create(id, Cooldown.class);
    }

    static MetadataKey<UUID> createUuidKey(String id) {
        return create(id, UUID.class);
    }

    /**
     * Gets the id of this key. May be automatically lowercase'd
     *
     * @return the id of this key
     */
    String getId();

    /**
     * Get the type of the value mapped to this key
     *
     * @return the type of the value
     */
    @Override
    TypeToken<T> getType();

    /**
     * Attempts to cast the given object to the return type of the key
     *
     * @param object the object to be casted
     * @return a casted object
     * @throws ClassCastException if the object cannot be casted
     */
    T cast(Object object) throws ClassCastException;

}