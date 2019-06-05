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

package me.lucko.helper.sql.plugin;

import me.lucko.helper.promise.Promise;
import me.lucko.helper.sql.Sql;
import me.lucko.helper.sql.batch.BatchBuilder;

import be.bendem.sqlstreams.util.SqlConsumer;

import java.sql.PreparedStatement;
import java.util.LinkedList;

import javax.annotation.Nonnull;

public class HelperSqlBatchBuilder implements BatchBuilder {

    @Nonnull private final Sql owner;
    @Nonnull private final String statement;
    @Nonnull private final LinkedList<SqlConsumer<PreparedStatement>> handlers;

    public HelperSqlBatchBuilder(@Nonnull Sql owner, @Nonnull String statement) {
        this.owner = owner;
        this.statement = statement;
        this.handlers = new LinkedList<>();
    }

    @Nonnull
    @Override
    public String getStatement() {
        return this.statement;
    }

    @Nonnull
    @Override
    public LinkedList<SqlConsumer<PreparedStatement>> getHandlers() {
        return this.handlers;
    }

    @Override
    public BatchBuilder reset() {
        this.handlers.clear();
        return this;
    }

    @Override
    public BatchBuilder batch(@Nonnull SqlConsumer<PreparedStatement> handler) {
        this.handlers.add(handler);
        return this;
    }

    @Override
    public void execute() {
        this.owner.executeBatch(this);
    }

    @Nonnull
    @Override
    public Promise<Void> executeAsync() {
        return this.owner.executeBatchAsync(this);
    }
}
