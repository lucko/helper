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

package me.lucko.helper.mongo.plugin;

import me.lucko.helper.internal.HelperImplementationPlugin;
import me.lucko.helper.mongo.Mongo;
import me.lucko.helper.mongo.MongoDatabaseCredentials;
import me.lucko.helper.mongo.MongoProvider;
import me.lucko.helper.plugin.ExtendedJavaPlugin;

import javax.annotation.Nonnull;

@HelperImplementationPlugin
public class HelperMongoPlugin extends ExtendedJavaPlugin implements MongoProvider {
    private MongoDatabaseCredentials globalCredentials;
    private Mongo globalDataSource;

    @Override
    protected void enable() {
        this.globalCredentials = MongoDatabaseCredentials.fromConfig(loadConfig("config.yml"));
        this.globalDataSource = getMongo(this.globalCredentials);
        this.globalDataSource.bindWith(this);

        // expose all instances as services.
        provideService(MongoProvider.class, this);
        provideService(MongoDatabaseCredentials.class, this.globalCredentials);
        provideService(Mongo.class, this.globalDataSource);
    }

    @Nonnull
    @Override
    public Mongo getMongo() {
        return this.globalDataSource;
    }

    @Nonnull
    @Override
    public Mongo getMongo(@Nonnull MongoDatabaseCredentials credentials) {
        return new HelperMongo(credentials);
    }

    @Nonnull
    @Override
    public MongoDatabaseCredentials getGlobalCredentials() {
        return this.globalCredentials;
    }
}
