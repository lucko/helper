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
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import me.lucko.helper.mongo.HelperMongo;
import me.lucko.helper.mongo.MongoDatabaseCredentials;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.Collections;

import javax.annotation.Nonnull;

public class MongoWrapper implements HelperMongo {

    private final MongoClient client;
    private final MongoDatabase database;
    private final Morphia morphia;
    private final Datastore morphiaDatastore;

    public MongoWrapper(@Nonnull MongoDatabaseCredentials credentials) {
        MongoCredential mongoCredential = MongoCredential.createCredential(
                credentials.getUsername(),
                credentials.getDatabase(),
                credentials.getPassword().toCharArray()
        );

        client = new MongoClient(
                new ServerAddress(credentials.getAddress(), credentials.getPort()),
                Collections.singletonList(mongoCredential)
        );
        database = client.getDatabase(credentials.getDatabase());
        morphia = new Morphia();
        morphiaDatastore = morphia.createDatastore(client, credentials.getDatabase());
    }

    @Nonnull
    @Override
    public MongoClient getClient() {
        return client;
    }

    @Nonnull
    @Override
    public MongoDatabase getDatabase() {
        return database;
    }

    @Override
    public MongoDatabase getDatabase(String name) {
        return client.getDatabase(name);
    }

    @Override
    public boolean terminate() {
        if (client != null) {
            client.close();
            return true;
        }
        return false;
    }

    @Override
    public Morphia getMorphia() {
        return morphia;
    }

    @Override
    public Datastore getMorphiaDatastore() {
        return morphiaDatastore;
    }

    @Override
    public Datastore getMorphiaDatastore(String name) {
        return morphia.createDatastore(client, name);
    }
}
