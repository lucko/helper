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

package me.lucko.helper.redis.plugin;

import com.google.common.reflect.TypeToken;

import me.lucko.helper.Schedulers;
import me.lucko.helper.messaging.AbstractMessenger;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.redis.Redis;
import me.lucko.helper.redis.RedisCredentials;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.utils.Log;

import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

public class HelperRedis implements Redis {

    private final JedisPool jedisPool;
    private final AbstractMessenger messenger;

    private final Set<String> channels = new HashSet<>();
    private final CompositeTerminable registry = CompositeTerminable.create();

    private PubSubListener listener = null;

    public HelperRedis(@Nonnull RedisCredentials credentials) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(16);

        // setup jedis
        if (credentials.getPassword().trim().isEmpty()) {
            this.jedisPool = new JedisPool(config, credentials.getAddress(), credentials.getPort());
        } else {
            this.jedisPool = new JedisPool(config, credentials.getAddress(), credentials.getPort(), 2000, credentials.getPassword());
        }

        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.ping();
        }

        Schedulers.async().run(new Runnable() {
            private boolean broken = false;

            @Override
            public void run() {
                if (this.broken) {
                    Log.info("[helper-redis] Retrying subscription...");
                    this.broken = false;
                }

                try (Jedis jedis = getJedis()) {
                    try {
                        HelperRedis.this.listener = new PubSubListener();
                        jedis.subscribe(HelperRedis.this.listener, "helper-redis-dummy".getBytes(StandardCharsets.UTF_8));
                    } catch (Exception e) {
                        // Attempt to unsubscribe this instance and try again.
                        new RuntimeException("Error subscribing to listener", e).printStackTrace();
                        try {
                            HelperRedis.this.listener.unsubscribe();
                        } catch (Exception ignored) {

                        }
                        HelperRedis.this.listener = null;
                        this.broken = true;
                    }
                }

                if (this.broken) {
                    // reschedule the runnable
                    Schedulers.async().runLater(this, 1L);
                }
            }
        });

        Schedulers.async().runRepeating(() -> {
            // ensure subscribed to all channels
            PubSubListener listener = HelperRedis.this.listener;

            if (listener == null || !listener.isSubscribed()) {
                return;
            }

            for (String channel : this.channels) {
                listener.subscribe(channel.getBytes(StandardCharsets.UTF_8));
            }

        }, 2L, 2L).bindWith(this.registry);

        this.messenger = new AbstractMessenger(
                (channel, message) -> {
                    try (Jedis jedis = getJedis()) {
                        jedis.publish(channel.getBytes(StandardCharsets.UTF_8), message);
                    }
                },
                channel -> {
                    Log.info("[helper-redis] Subscribing to channel: " + channel);
                    this.channels.add(channel);
                    this.listener.subscribe(channel.getBytes(StandardCharsets.UTF_8));
                },
                channel -> {
                    Log.info("[helper-redis] Unsubscribing from channel: " + channel);
                    this.channels.remove(channel);
                    this.listener.unsubscribe(channel.getBytes(StandardCharsets.UTF_8));
                }
        );
    }

    @Nonnull
    @Override
    public JedisPool getJedisPool() {
        Objects.requireNonNull(this.jedisPool, "jedisPool");
        return this.jedisPool;
    }

    @Nonnull
    @Override
    public Jedis getJedis() {
        return getJedisPool().getResource();
    }

    @Override
    public void close() throws Exception {
        if (this.listener != null) {
            this.listener.unsubscribe();
            this.listener = null;
        }

        if (this.jedisPool != null) {
            this.jedisPool.close();
        }

        this.registry.close();
    }

    @Nonnull
    @Override
    public <T> Channel<T> getChannel(@Nonnull String name, @Nonnull TypeToken<T> type) {
        return this.messenger.getChannel(name, type);
    }

    private final class PubSubListener extends BinaryJedisPubSub {
        private final ReentrantLock lock = new ReentrantLock();
        private final Set<String> subscribed = ConcurrentHashMap.newKeySet();

        @Override
        public void subscribe(byte[]... channels) {
            this.lock.lock();
            try {
                for (byte[] channel : channels) {
                    String channelName = new String(channel, StandardCharsets.UTF_8);
                    if (this.subscribed.add(channelName)) {
                        super.subscribe(channel);
                    }
                }
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        public void unsubscribe(byte[]... channels) {
            this.lock.lock();
            try {
                super.unsubscribe(channels);
            } finally {
                this.lock.unlock();
            }
        }

        @Override
        public void onSubscribe(byte[] channel, int subscribedChannels) {
            Log.info("[helper-redis] Subscribed to channel: " + new String(channel, StandardCharsets.UTF_8));
        }

        @Override
        public void onUnsubscribe(byte[] channel, int subscribedChannels) {
            String channelName = new String(channel, StandardCharsets.UTF_8);
            Log.info("[helper-redis] Unsubscribed from channel: " + channelName);
            this.subscribed.remove(channelName);
        }

        @Override
        public void onMessage(byte[] channel, byte[] message) {
            String channelName = new String(channel, StandardCharsets.UTF_8);
            try {
                HelperRedis.this.messenger.registerIncomingMessage(channelName, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
