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
