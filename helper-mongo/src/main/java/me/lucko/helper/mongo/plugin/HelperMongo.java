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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.mongo.Mongo;
import me.lucko.helper.mongo.MongoDatabaseCredentials;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.mapping.DefaultCreator;

import javax.annotation.Nonnull;

public class HelperMongo implements Mongo {

    private final MongoClient client;
    private final MongoDatabase database;
    private final Morphia morphia;
    private final Datastore morphiaDatastore;

    public HelperMongo(@Nonnull MongoDatabaseCredentials credentials) {
        MongoCredential mongoCredential = MongoCredential.createCredential(
                credentials.getUsername(),
                credentials.getDatabase(),
                credentials.getPassword().toCharArray()
        );

        this.client = new MongoClient(
                new ServerAddress(credentials.getAddress(), credentials.getPort()),
                mongoCredential,
                MongoClientOptions.builder().build()
        );
        this.database = this.client.getDatabase(credentials.getDatabase());
        this.morphia = new Morphia();
        this.morphiaDatastore = this.morphia.createDatastore(this.client, credentials.getDatabase());
        this.morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator() {
            @Override
            protected ClassLoader getClassLoaderForClass() {
                return LoaderUtils.getPlugin().getClassloader();
            }
        });
    }

    @Nonnull
    @Override
    public MongoClient getClient() {
        return this.client;
    }

    @Nonnull
    @Override
    public MongoDatabase getDatabase() {
        return this.database;
    }

    @Override
    public MongoDatabase getDatabase(String name) {
        return this.client.getDatabase(name);
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Override
    public Morphia getMorphia() {
        return this.morphia;
    }

    @Override
    public Datastore getMorphiaDatastore() {
        return this.morphiaDatastore;
    }

    @Override
    public Datastore getMorphiaDatastore(String name) {
        return this.morphia.createDatastore(this.client, name);
    }
}
