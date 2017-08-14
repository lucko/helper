package me.lucko.helper.utils;

/**
 * Represents a class which delegates called to a different object.
 *
 * @param <T> the delegate type
 */
public interface Delegate<T> {

    /**
     * Gets the delegate object
     *
     * @return the delegate object
     */
    T getDelegate();
}
