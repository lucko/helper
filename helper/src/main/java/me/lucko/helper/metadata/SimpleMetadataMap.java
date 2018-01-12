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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

final class SimpleMetadataMap implements MetadataMap {
    private final Map<MetadataKey<?>, Object> map = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public <T> void put(@Nonnull MetadataKey<T> key, @Nonnull T value) {
        internalPut(key, value);
    }

    @Override
    public <T> void put(@Nonnull MetadataKey<T> key, @Nonnull TransientValue<T> value) {
        internalPut(key, value);
    }

    private void internalPut(MetadataKey<?> key, Object value) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(value, "value");

        lock.lock();
        try {
            MetadataKey<?> existing = null;
            for (MetadataKey<?> k : map.keySet()) {
                if (k.equals(key)) {
                    existing = k;
                    break;
                }
            }

            if (existing != null && !existing.getType().equals(key.getType())) {
                throw new ClassCastException("Cannot cast key with id " + key.getId() + " with type " + key.getType().getRawType() + " to existing stored type " + existing.getType().getRawType());
            }

            map.put(key, value);

        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> void forcePut(@Nonnull MetadataKey<T> key, @Nonnull T value) {
        internalForcePut(key, value);
    }

    @Override
    public <T> void forcePut(@Nonnull MetadataKey<T> key, @Nonnull TransientValue<T> value) {
        internalForcePut(key, value);
    }

    private void internalForcePut(MetadataKey<?> key, Object value) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(value, "value");

        lock.lock();
        try {
            map.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> boolean putIfAbsent(@Nonnull MetadataKey<T> key, @Nonnull T value) {
        return internalPutIfAbsent(key, value);
    }

    @Override
    public <T> boolean putIfAbsent(@Nonnull MetadataKey<T> key, @Nonnull TransientValue<T> value) {
        return internalPutIfAbsent(key, value);
    }

    private boolean internalPutIfAbsent(MetadataKey<?> key, Object value) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(value, "value");

        lock.lock();
        try {
            cleanup();
            return map.putIfAbsent(key, value) == null;
        } finally {
            lock.unlock();
        }
    }

    @Nonnull
    @Override
    public <T> Optional<T> get(@Nonnull MetadataKey<T> key) {
        Preconditions.checkNotNull(key, "key");

        lock.lock();
        try {
            Map.Entry<MetadataKey<?>, Object> existing = null;

            // try to locate an existing entry, and expire any values at the same time.
            Iterator<Map.Entry<MetadataKey<?>, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MetadataKey<?>, Object> kv = it.next();

                if (kv.getValue() instanceof TransientValue<?>) {
                    TransientValue<?> transientValue = ((TransientValue) kv.getValue());

                    Object unboxed = transientValue.getOrNull();

                    // if it has expired
                    if (unboxed == null) {
                        it.remove();
                        continue;
                    }

                    // copy out the unboxed value
                    if (kv.getKey().equals(key)) {
                        existing = Maps.immutableEntry(kv.getKey(), unboxed);
                        break;
                    }

                } else {
                    if (kv.getKey().equals(key)) {
                        existing = kv;
                        break;
                    }
                }
            }

            if (existing == null) {
                return Optional.empty();
            }

            if (!existing.getKey().getType().equals(key.getType())) {
                throw new ClassCastException("Cannot cast key with id " + key.getId() + " with type " + key.getType().getRawType() + " to existing stored type " + existing.getKey().getType().getRawType());
            }

            return Optional.of(key.cast(existing.getValue()));
        } finally {
            lock.unlock();
        }
    }

    @Nonnull
    @Override
    public <T> boolean ifPresent(@Nonnull MetadataKey<T> key, @Nonnull Consumer<? super T> action) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(action, "action");
        Optional<T> opt = get(key);
        if (!opt.isPresent()) {
            return false;
        }

        action.accept(opt.get());
        return true;
    }

    @Override
    public <T> T getOrNull(@Nonnull MetadataKey<T> key) {
        Preconditions.checkNotNull(key, "key");
        return get(key).orElse(null);
    }

    @Nonnull
    @Override
    public <T> T getOrDefault(@Nonnull MetadataKey<T> key, T def) {
        Preconditions.checkNotNull(key, "key");
        return get(key).orElse(def);
    }

    @Nonnull
    @Override
    public <T> T getOrPut(@Nonnull MetadataKey<T> key, @Nonnull Supplier<? extends T> def) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(def, "def");

        lock.lock();
        try {
            Map.Entry<MetadataKey<?>, Object> existing = null;

            // try to locate an existing entry, and expire any values at the same time.
            Iterator<Map.Entry<MetadataKey<?>, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MetadataKey<?>, Object> kv = it.next();

                if (kv.getValue() instanceof TransientValue<?>) {
                    TransientValue<?> transientValue = ((TransientValue) kv.getValue());

                    Object unboxed = transientValue.getOrNull();

                    // if it has expired
                    if (unboxed == null) {
                        it.remove();
                        continue;
                    }

                    // copy out the unboxed value
                    if (kv.getKey().equals(key)) {
                        existing = Maps.immutableEntry(kv.getKey(), unboxed);
                        break;
                    }

                } else {
                    if (kv.getKey().equals(key)) {
                        existing = kv;
                        break;
                    }
                }
            }

            if (existing == null) {
                T t = def.get();
                Preconditions.checkNotNull(t, "supplied def");

                map.put(key, t);
                return t;
            }

            if (!existing.getKey().getType().equals(key.getType())) {
                throw new ClassCastException("Cannot cast key with id " + key.getId() + " with type " + key.getType().getRawType() + " to existing stored type " + existing.getKey().getType().getRawType());
            }

            return key.cast(existing.getValue());
        } finally {
            lock.unlock();
        }
    }

    @Nonnull
    @Override
    public <T> T getOrPutExpiring(@Nonnull MetadataKey<T> key, @Nonnull Supplier<? extends TransientValue<T>> def) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(def, "def");

        lock.lock();
        try {
            Map.Entry<MetadataKey<?>, Object> existing = null;

            // try to locate an existing entry, and expire any values at the same time.
            Iterator<Map.Entry<MetadataKey<?>, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MetadataKey<?>, Object> kv = it.next();

                if (kv.getValue() instanceof TransientValue<?>) {
                    TransientValue<?> transientValue = ((TransientValue) kv.getValue());

                    Object unboxed = transientValue.getOrNull();

                    // if it has expired
                    if (unboxed == null) {
                        it.remove();
                        continue;
                    }

                    // copy out the unboxed value
                    if (kv.getKey().equals(key)) {
                        existing = Maps.immutableEntry(kv.getKey(), unboxed);
                        break;
                    }

                } else {
                    if (kv.getKey().equals(key)) {
                        existing = kv;
                        break;
                    }
                }
            }

            if (existing == null) {
                TransientValue<T> t = def.get();
                Preconditions.checkNotNull(t, "supplied def");

                T value = t.getOrNull();
                if (value == null) {
                    throw new IllegalArgumentException("Transient value already expired: " + t);
                }

                map.put(key, t);
                return value;
            }

            if (!existing.getKey().getType().equals(key.getType())) {
                throw new ClassCastException("Cannot cast key with id " + key.getId() + " with type " + key.getType().getRawType() + " to existing stored type " + existing.getKey().getType().getRawType());
            }

            return key.cast(existing.getValue());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean has(@Nonnull MetadataKey<?> key) {
        Preconditions.checkNotNull(key, "key");

        lock.lock();
        try {
            Map.Entry<MetadataKey<?>, Object> existing = null;

            // try to locate an existing entry, and expire any values at the same time.
            Iterator<Map.Entry<MetadataKey<?>, Object>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<MetadataKey<?>, Object> kv = it.next();

                if (kv.getValue() instanceof TransientValue<?>) {
                    TransientValue<?> transientValue = ((TransientValue) kv.getValue());

                    if (transientValue.shouldExpire()) {
                        it.remove();
                        continue;
                    }
                }

                if (kv.getKey().equals(key)) {
                    existing = kv;
                    break;
                }
            }

            return existing != null && existing.getKey().getType().equals(key.getType());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean remove(@Nonnull MetadataKey<?> key) {
        Preconditions.checkNotNull(key, "key");

        lock.lock();
        try {
            return map.remove(key) != null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            map.clear();
        } finally {
            lock.unlock();
        }
    }

    @Nonnull
    @Override
    public ImmutableMap<MetadataKey<?>, Object> asMap() {
        lock.lock();
        try {
            return ImmutableMap.copyOf(map);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        lock.lock();
        try {
            cleanup();
            return map.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void cleanup() {
        lock.lock();
        try {
            map.values().removeIf(o -> o instanceof TransientValue<?> && ((TransientValue) o).shouldExpire());
        } finally {
            lock.unlock();
        }
    }

}
