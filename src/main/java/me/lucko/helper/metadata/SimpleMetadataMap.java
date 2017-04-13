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

package me.lucko.helper.metadata;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

final class SimpleMetadataMap implements MetadataMap {
    private final Map<MetadataKey<?>, Object> map = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public <T> void put(MetadataKey<T> key, T value) {
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
    public <T> void forcePut(MetadataKey<T> key, T value) {
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
    public <T> boolean putIfAbsent(MetadataKey<T> key, T value) {
        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(value, "value");

        lock.lock();
        try {
            return map.putIfAbsent(key, value) == null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> Optional<T> get(MetadataKey<T> key) {
        Preconditions.checkNotNull(key, "key");

        lock.lock();
        try {
            Map.Entry<MetadataKey<?>, Object> existing = null;
            for (Map.Entry<MetadataKey<?>, Object> kv : map.entrySet()) {
                if (kv.getKey().equals(key)) {
                    existing = kv;
                    break;
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

    @Override
    public <T> T getOrNull(MetadataKey<T> key) {
        Preconditions.checkNotNull(key, "key");
        return get(key).orElse(null);
    }

    @Override
    public <T> T getOrDefault(MetadataKey<T> key, T def) {
        Preconditions.checkNotNull(key, "key");
        return get(key).orElse(def);
    }

    @Override
    public boolean has(MetadataKey<?> key) {
        Preconditions.checkNotNull(key, "key");

        lock.lock();
        try {
            Map.Entry<MetadataKey<?>, Object> existing = null;
            for (Map.Entry<MetadataKey<?>, Object> kv : map.entrySet()) {
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
    public boolean remove(MetadataKey<?> key) {
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

}
