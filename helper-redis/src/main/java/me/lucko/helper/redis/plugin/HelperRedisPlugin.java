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

import me.lucko.helper.internal.HelperImplementationPlugin;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.redis.Redis;
import me.lucko.helper.redis.RedisCredentials;
import me.lucko.helper.redis.RedisProvider;

import javax.annotation.Nonnull;

@HelperImplementationPlugin
public class HelperRedisPlugin extends ExtendedJavaPlugin implements RedisProvider {
    private RedisCredentials globalCredentials;
    private Redis globalRedis;

    @Override
    protected void enable() {
        this.globalCredentials = RedisCredentials.fromConfig(loadConfig("config.yml"));
        this.globalRedis = getRedis(this.globalCredentials);
        this.globalRedis.bindWith(this);

        // expose all instances as services.
        provideService(RedisProvider.class, this);
        provideService(RedisCredentials.class, this.globalCredentials);
        provideService(Redis.class, this.globalRedis);
        provideService(Messenger.class, this.globalRedis);
    }

    @Nonnull
    @Override
    public Redis getRedis() {
        return this.globalRedis;
    }

    @Nonnull
    @Override
    public Redis getRedis(@Nonnull RedisCredentials credentials) {
        return new HelperRedis(credentials);
    }

    @Nonnull
    @Override
    public RedisCredentials getGlobalCredentials() {
        return this.globalCredentials;
    }
}
