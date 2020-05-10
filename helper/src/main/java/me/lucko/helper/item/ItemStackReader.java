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

package me.lucko.helper.item;

import com.google.common.collect.Iterables;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Utility for creating {@link ItemStackBuilder}s from {@link ConfigurationSection config} files.
 */
public class ItemStackReader {

    /**
     * The default helper {@link ItemStackReader} implementation.
     */
    public static final ItemStackReader DEFAULT = new ItemStackReader();

    // Allow subclassing
    protected ItemStackReader() {

    }

    /**
     * Reads an {@link ItemStackBuilder} from the given config.
     *
     * @param config the config to read from
     * @return the item
     */
    public final ItemStackBuilder read(ConfigurationSection config) {
        return read(config, VariableReplacer.NOOP);
    }

    /**
     * Reads an {@link ItemStackBuilder} from the given config.
     *
     * @param config the config to read from
     * @param variableReplacer the variable replacer to use to replace variables in the name and lore.
     * @return the item
     */
    public ItemStackBuilder read(ConfigurationSection config, VariableReplacer variableReplacer) {
        return ItemStackBuilder.of(parseMaterial(config))
                .apply(isb -> {
                    parseData(config).ifPresent(isb::data);
                    parseName(config).map(variableReplacer::replace).ifPresent(isb::name);
                    parseLore(config).map(variableReplacer::replace).ifPresent(isb::lore);
                });
    }

    protected Material parseMaterial(ConfigurationSection config) {
        return parseMaterial(config.getString("material"));
    }

    protected Material parseMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to parse material '" + name + "'");
        }
    }

    protected OptionalInt parseData(ConfigurationSection config) {
        if (config.contains("data")) {
            return OptionalInt.of(config.getInt("data"));
        }
        return OptionalInt.empty();
    }

    protected Optional<String> parseName(ConfigurationSection config) {
        if (config.contains("name")) {
            return Optional.of(config.getString("name"));
        }
        return Optional.empty();
    }

    protected Optional<List<String>> parseLore(ConfigurationSection config) {
        if (config.contains("lore")) {
            return Optional.of(config.getStringList("lore"));
        }
        return Optional.empty();
    }

    /**
     * Function for replacing variables in item names and lores.
     */
    @FunctionalInterface
    public interface VariableReplacer {

        /**
         * No-op instance.
         */
        VariableReplacer NOOP = string -> string;

        /**
         * Replace variables in the input {@code string}.
         *
         * @param string the string
         * @return the replaced string
         */
        String replace(String string);

        /**
         * Replaces variables in the input {@code list} of {@link String}s.
         *
         * @param list the list
         * @return the replaced list
         */
        default Iterable<String> replace(List<String> list) {
            return Iterables.transform(list, this::replace);
        }
    }

}
