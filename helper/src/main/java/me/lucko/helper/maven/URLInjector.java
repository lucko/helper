package me.lucko.helper.maven;

import javax.annotation.Nonnull;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Handles injecting URLs into a {@link URLClassLoader}
 */
interface URLInjector {

    /**
     * Adds the given URL to the class loader
     *
     * @param classLoader ClassLoader to add to
     * @param url         URL to add
     * @throws Exception Any exception whilst adding
     */
    void addURL(@Nonnull ClassLoader classLoader, @Nonnull URL url) throws Exception;

}
