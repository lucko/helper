package me.lucko.helper.maven;

import sun.misc.Unsafe;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * An implementation of {@link URLInjector} that uses sun.misc.Unsafe to
 * inject URLs directly into a {@link URLClassLoader}'s paths.
 * <p>
 * This implementation works on Java 9+ only.
 */
final class UnsafeURLInjector implements URLInjector {

    private final ArrayDeque<URL> unopenedURLs;
    private final ArrayList<URL> pathURLs;

    public UnsafeURLInjector(final ArrayDeque<URL> unopenedURLs, final ArrayList<URL> pathURLs) {
        this.unopenedURLs = unopenedURLs;
        this.pathURLs = pathURLs;
    }

    public static URLInjector create(final URLClassLoader classLoader) {
        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            final Unsafe unsafe = (Unsafe) field.get(null);
            final Object ucp = fetchField(unsafe, URLClassLoader.class, classLoader, "ucp");
            final ArrayDeque<URL> unopenedURLs = (ArrayDeque<URL>) fetchField(unsafe, ucp, "unopenedUrls");
            final ArrayList<URL> pathURLs = (ArrayList<URL>) fetchField(unsafe, ucp, "path");
            return new UnsafeURLInjector(unopenedURLs, pathURLs);
        } catch (Throwable t) {
            return (loader, url) -> { throw new UnsupportedOperationException(); };
        }
    }

    private static Object fetchField(final Unsafe unsafe, final Object object, final String name) throws NoSuchFieldException {
        return fetchField(unsafe, object.getClass(), object, name);
    }

    private static Object fetchField(final Unsafe unsafe, final Class<?> clazz, final Object object, final String name) throws NoSuchFieldException {
        final Field field = clazz.getDeclaredField(name);
        final long offset = unsafe.objectFieldOffset(field);
        return unsafe.getObject(object, offset);
    }

    @Override public void addURL(@Nonnull ClassLoader classLoader, @Nonnull URL url) {
        unopenedURLs.addLast(url);
        pathURLs.add(url);
    }
}
