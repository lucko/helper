package me.lucko.helper.sql;

/**
 * Provides {@link HelperDataSource} instances.
 */
public interface SqlProvider {

    /**
     * Gets the global datasource.
     *
     * @return the global datasource.
     */
    HelperDataSource getDataSource();

    /**
     * Constructs a new datasource using the given credentials.
     *
     * <p>These instances are not cached, and a new datasource is created each
     * time this method is called.</p>
     *
     * @param credentials the credentials for the database
     * @return a new datasource
     */
    HelperDataSource getDataSource(DatabaseCredentials credentials);

    /**
     * Gets the global database credentials being used for the global datasource.
     *
     * @return the global credentials
     */
    DatabaseCredentials getGlobalCredentials();

}
