package me.lucko.helper.sql;

import me.lucko.helper.Schedulers;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.sql.util.ThrownConsumer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import javax.annotation.Nonnull;

/**
 * Represents a statement meant to be executed more than a single time.
 *
 * <p>It will be executed all at once, using a single database connection.</p>
 */
public interface BatchBuilder {

    /**
     * Gets the statement to be executed when this batch is finished.
     *
     * @return the statement to be executed
     */
    @Nonnull
    String getStatement();

    /**
     * Gets a {@link Collection} of handlers for this statement.
     *
     * @return the handlers for this statement
     */
    @Nonnull
    Collection<ThrownConsumer<PreparedStatement, SQLException>> getHandlers();

    /**
     * Resets this BatchBuilder, making it possible to re-use
     * for multiple situations.
     *
     * @return this builder
     */
    BatchBuilder reset();

    /**
     * Adds an additional handler to be executed when this batch is finished.
     *
     * @param handler the statement handler
     * @return this builder
     */
    BatchBuilder batch(@Nonnull ThrownConsumer<PreparedStatement, SQLException> handler);

    /**
     * Executes the statement for this batch, with the handlers used to prepare it.
     */
    void execute();

    /**
     * Executes the statement for this batch, with the handlers used to prepare it.
     *
     * <p>Will return a {@link Promise} to do this.</p>
     *
     * @return a promise to execute this batch asynchronously
     */
    @Nonnull
    Promise<Void> executeAsync();
}
