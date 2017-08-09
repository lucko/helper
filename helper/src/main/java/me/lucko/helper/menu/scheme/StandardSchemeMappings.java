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

package me.lucko.helper.menu.scheme;

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;

import org.bukkit.Material;

import java.util.Map;
import java.util.function.IntFunction;

/**
 * Contains a number of default {@link SchemeMapping}s.
 */
public final class StandardSchemeMappings {

    public static final SchemeMapping STAINED_GLASS = forColoredMaterial(Material.STAINED_GLASS_PANE);
    public static final SchemeMapping STAINED_GLASS_BLOCK = forColoredMaterial(Material.STAINED_GLASS);
    public static final SchemeMapping HARDENED_CLAY = forColoredMaterial(Material.STAINED_CLAY);
    public static final SchemeMapping WOOL = forColoredMaterial(Material.WOOL);
    public static final SchemeMapping EMPTY = new EmptySchemeMapping();

    private static SchemeMapping forColoredMaterial(Material material) {
        final IntFunction<Item> func = value -> ItemStackBuilder.of(material).name("&f").data(value).build(null);

        Map<Integer, Item> map = ImmutableMap.<Integer, Item>builder()
                .put(0, func.apply(0))
                .put(1, func.apply(1))
                .put(2, func.apply(2))
                .put(3, func.apply(3))
                .put(4, func.apply(4))
                .put(5, func.apply(5))
                .put(6, func.apply(6))
                .put(7, func.apply(7))
                .put(8, func.apply(8))
                .put(9, func.apply(9))
                .put(10, func.apply(10))
                .put(11, func.apply(11))
                .put(12, func.apply(12))
                .put(13, func.apply(13))
                .put(14, func.apply(14))
                .put(15, func.apply(15))
                .build();

        return new AbstractSchemeMapping(map);
    }

    private StandardSchemeMappings() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
