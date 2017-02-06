package me.lucko.helper.menu.scheme;

import me.lucko.helper.menu.Item;

import java.util.Map;

/**
 * Represents a mapping to be used in a {@link MenuScheme}
 */
public interface SchemeMapping {

    Map<Integer, Item> getMapping();

}
