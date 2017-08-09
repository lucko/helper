package me.lucko.helper.menu.scheme;

import me.lucko.helper.menu.Item;

import java.util.Optional;

/**
 * An empty menu scheme.
 */
final class EmptySchemeMapping implements SchemeMapping {

    @Override
    public Optional<Item> get(int key) {
        return Optional.empty();
    }

    @Override
    public Item getNullable(int key) {
        return null;
    }

    @Override
    public boolean hasMappingFor(int key) {
        return false;
    }

    @Override
    public SchemeMapping copy() {
        return this; // no need to make a copy, this class is a singleton
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
