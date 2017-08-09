package me.lucko.helper.menu.scheme;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import me.lucko.helper.menu.Item;

import java.util.Map;

/**
 * Implements {@link SchemeMapping} using an immutable map.
 */
public class AbstractSchemeMapping implements SchemeMapping {
    private final Map<Integer, Item> mapping;

    public AbstractSchemeMapping(Map<Integer, Item> mapping) {
        Preconditions.checkNotNull(mapping, "mapping");
        this.mapping = ImmutableMap.copyOf(mapping);
    }

    @Override
    public Item getNullable(int key) {
        return mapping.get(key);
    }

    @Override
    public boolean hasMappingFor(int key) {
        return mapping.containsKey(key);
    }

    @Override
    public SchemeMapping copy() {
        return this; // no need to make a copy, the backing data is immutable
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractSchemeMapping && ((AbstractSchemeMapping) obj).mapping.equals(mapping);
    }

    @Override
    public int hashCode() {
        return mapping.hashCode();
    }
}
