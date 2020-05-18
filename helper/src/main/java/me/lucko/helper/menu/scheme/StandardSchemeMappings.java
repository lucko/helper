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

import com.google.common.collect.Range;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains a number of default {@link SchemeMapping}s.
 */
@NonnullByDefault
public final class StandardSchemeMappings
{
    public static final List<String> COLOR_MAPPINGS = Collections.unmodifiableList(Arrays.asList("WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK", "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"));
    public static final List<Material> REMAPPED_STAINED_GLASS_PANE = Collections.unmodifiableList(COLOR_MAPPINGS.stream().map(s -> Material.getMaterial(s + "_STAINED_GLASS_PANE")).collect(Collectors.toList()));
    public static final List<Material> REMAPPED_STAINED_GLASS = Collections.unmodifiableList(COLOR_MAPPINGS.stream().map(s -> Material.getMaterial(s + "_STAINED_GLASS")).collect(Collectors.toList()));
    public static final List<Material> REMAPPED_TERRACOTTA = Collections.unmodifiableList(COLOR_MAPPINGS.stream().map(s -> Material.getMaterial(s + "_TERRACOTTA")).collect(Collectors.toList()));
    public static final List<Material> REMAPPED_WOOL = Collections.unmodifiableList(COLOR_MAPPINGS.stream().map(s -> Material.getMaterial(s + "_WOOL")).collect(Collectors.toList()));

    public static final SchemeMapping EMPTY = new EmptySchemeMapping();
    private static final Range<Integer> COLORED_MATERIAL_RANGE = Range.closed(0, 15);
    public static final SchemeMapping STAINED_GLASS = forColoredMaterial(REMAPPED_STAINED_GLASS_PANE);
    public static final SchemeMapping STAINED_GLASS_BLOCK = forColoredMaterial(REMAPPED_STAINED_GLASS);
    public static final SchemeMapping TERRACOTTA = forColoredMaterial(REMAPPED_TERRACOTTA);
    public static final SchemeMapping WOOL = forColoredMaterial(REMAPPED_WOOL);

    private StandardSchemeMappings()
    {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    private static SchemeMapping forColoredMaterial(List<Material> coloredMaterials)
    {
        return FunctionalSchemeMapping.of(
                data -> ItemStackBuilder.of(coloredMaterials.get(data)).name("&f").data(data).build(null),
                COLORED_MATERIAL_RANGE
        );
    }
}
