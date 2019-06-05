package me.lucko.helper.sql.plugin;

import com.google.common.collect.Lists;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.sql.BatchBuilder;
import me.lucko.helper.sql.Sql;
import me.lucko.helper.sql.util.ThrownConsumer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.annotation.Nonnull;

public class HelperSqlBatchBuilder implements BatchBuilder {

    @Nonnull private final Sql owner;
    @Nonnull private final String statement;
    @Nonnull private final LinkedList<ThrownConsumer<PreparedStatement, SQLException>> handlers;

    public HelperSqlBatchBuilder(@Nonnull Sql owner, @Nonnull String statement) {
        this.owner = owner;
        this.statement = statement;
        this.handlers = Lists.newLinkedList();
    }

    @Nonnull
    @Override
    public String getStatement() {
        return this.statement;
    }

    @Nonnull
    @Override
    public LinkedList<ThrownConsumer<PreparedStatement, SQLException>> getHandlers() {
        return this.handlers;
    }

    @Override
    public BatchBuilder reset() {
        this.handlers.clear();
        return this;
    }

    @Override
    public BatchBuilder batch(@Nonnull ThrownConsumer<PreparedStatement, SQLException> handler) {
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
