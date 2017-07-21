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

import com.google.common.base.Preconditions;
import com.google.common.reflect.TypeToken;

import me.lucko.helper.Scheduler;
import me.lucko.helper.messaging.AbstractMessenger;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.redis.HelperRedis;
import me.lucko.helper.redis.RedisCredentials;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.CountDownLatch;

class JedisWrapper implements HelperRedis {

    private final JedisPool jedisPool;
    private final AbstractMessenger messenger;
    private JedisPubSub listener = null;

    JedisWrapper(RedisCredentials credentials) {
        // setup jedis
        if (credentials.getPassword().trim().isEmpty()) {
            jedisPool = new JedisPool(new JedisPoolConfig(), credentials.getAddress(), credentials.getPort());
        } else {
            jedisPool = new JedisPool(new JedisPoolConfig(), credentials.getAddress(), credentials.getPort(), 0, credentials.getPassword());
        }

        try (Jedis jedis = this.jedisPool.getResource()) {
            jedis.ping();
        }

        // setup the messenger
        listener = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                if (messenger != null) {
                    messenger.registerIncomingMessage(channel, message);
                }
            }
        };

        CountDownLatch latch = new CountDownLatch(1);

        Scheduler.runAsync(() -> {
            try (Jedis jedis = getJedis()) {
                jedis.subscribe(listener, "helper-redis-dummy");
            } finally {
                latch.countDown();
            }
        });

        messenger = new AbstractMessenger(
                (channel, message) -> Scheduler.runAsync(() -> {
                    try (Jedis jedis = getJedis()) {
                        jedis.publish(channel, message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }),
                channel -> Scheduler.runAsync(() -> {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    listener.subscribe(channel);
                }),
                channel -> Scheduler.runAsync(() -> {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    listener.unsubscribe(channel);
                })
        );
    }

    @Override
    public JedisPool getJedisPool() {
        Preconditions.checkNotNull(jedisPool, "jedisPool");
        return this.jedisPool;
    }

    @Override
    public Jedis getJedis() {
        return getJedisPool().getResource();
    }

    @Override
    public boolean terminate() {
        if (jedisPool != null) {
            jedisPool.close();
            return true;
        }
        return false;
    }

    @Override
    public <T> Channel<T> getChannel(String name, TypeToken<T> type) {
        return messenger.getChannel(name, type);
    }
}
