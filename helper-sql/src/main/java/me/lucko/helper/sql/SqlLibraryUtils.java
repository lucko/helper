package me.lucko.helper.sql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class SqlLibraryUtils {

    public static void downloadSqlLibrary(String libraryUrl, File jarFile) {
        try {
            downloadSqlLibrary(new URL(libraryUrl), jarFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void downloadSqlLibrary(URL libraryUrl, File jarFile) {
        if (!jarFile.exists())
            try {
                if (!jarFile.getParentFile().exists() && !jarFile.getParentFile().mkdirs())
                    throw new RuntimeException();
                InputStream is = libraryUrl.openStream();
                Files.copy(is, jarFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public static void registerSqlDriver(String driverClassName, File jarFile) {
        try {
            URL u = new URL("jar:file:" + jarFile.getAbsolutePath() + "!/");
            URLClassLoader ucl = new URLClassLoader(new URL[]{u});
            Driver driver = (Driver) Class.forName(driverClassName, true, ucl).newInstance();
            DriverManager.registerDriver(new DriverShim(driver));
        } catch (MalformedURLException | SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | SecurityException e) {
            e.printStackTrace();
        }
    }

    public static void registerSqlDriver(String driverClassName, String libraryUrl, File jarFile) {
        try {
            registerSqlDriver(driverClassName, new URL(libraryUrl), jarFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void registerSqlDriver(String driverClassName, URL libraryUrl, File jarFile) {
        downloadSqlLibrary(libraryUrl, jarFile);
        registerSqlDriver(driverClassName, jarFile);
    }

    private SqlLibraryUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
