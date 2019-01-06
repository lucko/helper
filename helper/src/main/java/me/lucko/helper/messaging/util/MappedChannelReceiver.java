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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.ChannelAgent;
import me.lucko.helper.terminable.Terminable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Creates a subscription to a channel, and maps received values to a calculated key.
 *
 * @param <T> the message type
 * @param <K> the key type
 * @param <V> the value type
 */
public final class MappedChannelReceiver<T, K, V> implements Terminable {

    /**
     * Creates a new expiring mapped channel receiver.
     *
     * @param channel the channel
     * @param keyMapper the function used to map keys
     * @param valueMapper the function used to map values
     * @param expiryDuration the duration to expire entries after
     * @param unit the unit of the duration
     * @param <T> the message type
     * @param <K> the key type
     * @param <V> the value type
     * @return a new mapped channel receiver
     */
    @Nonnull
    public static <T, K, V> MappedChannelReceiver<T, K, V> createExpiring(@Nonnull Channel<T> channel, @Nonnull Function<? super T, ? extends K> keyMapper, @Nonnull Function<? super T, ? extends V> valueMapper, long expiryDuration, @Nonnull TimeUnit unit) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(keyMapper, "keyMapper");
        Objects.requireNonNull(unit, "unit");

        return new MappedChannelReceiver<>(channel, keyMapper, valueMapper, new CacheMessageStore<>(expiryDuration, unit));
    }

    /**
     * Creates a new expiring mapped channel receiver.
     *
     * @param channel the channel
     * @param keyMapper the function used to map keys
     * @param expiryDuration the duration to expire entries after
     * @param unit the unit of the duration
     * @param <T> the message type
     * @param <K> the key type
     * @return a new mapped channel receiver
     */
    @Nonnull
    public static <T, K> MappedChannelReceiver<T, K, T> createExpiring(@Nonnull Channel<T> channel, @Nonnull Function<? super T, ? extends K> keyMapper, long expiryDuration, @Nonnull TimeUnit unit) {
        return new MappedChannelReceiver<>(channel, keyMapper, Function.identity(), new CacheMessageStore<>(expiryDuration, unit));
    }

    /**
     * Creates a new mapped channel receiver.
     *
     * @param channel the channel
     * @param keyMapper the function used to map keys
     * @param valueMapper the function used to map values
     * @param map the map instance used to back the instance
     * @param <T> the message type
     * @param <K> the key type
     * @param <V> the value type
     * @return a new mapped channel receiver
     */
    @Nonnull
    public static <T, K, V> MappedChannelReceiver<T, K, V> create(@Nonnull Channel<T> channel, @Nonnull Function<? super T, ? extends K> keyMapper, @Nonnull Function<? super T, ? extends V> valueMapper, Map<K, V> map) {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(keyMapper, "keyMapper");
        Objects.requireNonNull(valueMapper, "valueMapper");
        Objects.requireNonNull(map, "map");

        return new MappedChannelReceiver<>(channel, keyMapper, valueMapper, new MapMessageStore<>(map));
    }

    /**
     * Creates a new mapped channel receiver.
     *
     * @param channel the channel
     * @param keyMapper the function used to map keys
     * @param map the map instance used to back the instance
     * @param <T> the message type
     * @param <K> the key type
     * @return a new mapped channel receiver
     */
    @Nonnull
    public static <T, K> MappedChannelReceiver<T, K, T> create(@Nonnull Channel<T> channel, @Nonnull Function<? super T, ? extends K> keyMapper, Map<K, T> map) {
        return create(channel, keyMapper, Function.identity(), map);
    }

    private final ChannelAgent<T> agent;
    private final Function<? super T, ? extends K> keyMapper;
    private final Function<? super T, ? extends V> valueMapper;
    private final MessageStore<K, V> messageStore;

    private MappedChannelReceiver(Channel<T> channel, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper, MessageStore<K, V> messageStore) {
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.messageStore = messageStore;

        this.agent = channel.newAgent(this::handleMessage);
    }

    private void handleMessage(ChannelAgent<T> agent, T message) {
        K key = this.keyMapper.apply(message);
        V value = this.valueMapper.apply(message);
        this.messageStore.put(key, value);
    }


    /**
     * Returns a map of the received data.
     *
     * @return the data
     */
    @Nonnull
    public Map<K, V> asMap() {
        return this.messageStore.asMap();
    }

    /**
     * Gets a value from the backing data
     *
     * @param key the key
     * @return the value, if present
     */
    @Nullable
    public V getValue(@Nonnull K key) {
        return this.messageStore.getValue(key);
    }

    /**
     * Invalidates a specific entry in the backing data.
     *
     * @param key the key
     */
    public void invalidateEntry(@Nonnull K key) {
        this.messageStore.invalidateEntry(key);
    }

    @Override
    public void close() {
        this.agent.close();
    }

    private interface MessageStore<K, T> {

        void put(K key, T message);

        Map<K, T> asMap();

        T getValue(K key);

        void invalidateEntry(K key);

    }

    private static final class CacheMessageStore<K, V> implements MessageStore<K, V> {
        private final Cache<K, V> cache;
        private final Map<K, V> asMap;

        CacheMessageStore(long expiryDuration, TimeUnit unit) {
            this.cache = CacheBuilder.newBuilder()
                    .expireAfterWrite(expiryDuration, unit)
                    .build();

            this.asMap = Collections.unmodifiableMap(this.cache.asMap());
        }

        @Override
        public void put(K key, V message) {
            this.cache.put(key, message);
        }

        @Override
        public Map<K, V> asMap() {
            return this.asMap;
        }

        @Override
        public V getValue(K key) {
            return this.cache.getIfPresent(key);
        }

        @Override
        public void invalidateEntry(K key) {
            this.cache.invalidate(key);
        }
    }

    private static final class MapMessageStore<K, V> implements MessageStore<K, V> {
        private final Map<K, V> map;
        private final Map<K, V> asMap;

        private MapMessageStore(Map<K, V> map) {
            this.map = map;
            this.asMap = Collections.unmodifiableMap(this.map);
        }

        @Override
        public void put(K key, V message) {
            this.map.put(key, message);
        }

        @Override
        public Map<K, V> asMap() {
            return this.asMap;
        }

        @Override
        public V getValue(K key) {
            return this.map.get(key);
        }

        @Override
        public void invalidateEntry(K key) {
            this.map.remove(key);
        }
    }
}
