package me.lucko.helper.sql.plugin;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.sql.DatabaseCredentials;
import me.lucko.helper.sql.HelperDataSource;
import me.lucko.helper.sql.SqlProvider;

public class SqlPlugin extends ExtendedJavaPlugin implements SqlProvider {
    private DatabaseCredentials globalCredentials;
    private HelperDataSource globalDataSource;

    @Override
    public void onEnable() {
        this.globalCredentials = DatabaseCredentials.fromConfig(loadConfig("config.yml"));
        this.globalDataSource = new HikariWrapper(this.globalCredentials);

        // expose all instances as services.
        provideService(SqlProvider.class, this);
        provideService(DatabaseCredentials.class, this.globalCredentials);
        provideService(HelperDataSource.class, this.globalDataSource);
    }

    @Override
    public HelperDataSource getDataSource() {
        return this.globalDataSource;
    }

    @Override
    public HelperDataSource getDataSource(DatabaseCredentials credentials) {
        return new HikariWrapper(credentials);
    }

    @Override
    public DatabaseCredentials getGlobalCredentials() {
        return this.globalCredentials;
    }
}
