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

import org.bukkit.Material;

import java.util.Collections;

/**
 * Contains a number of default {@link SchemeMapping}s.
 */
public final class StandardSchemeMappings {

    public static final SchemeMapping STAINED_GLASS = new ColoredSchemeMapping(Material.STAINED_GLASS_PANE);
    public static final SchemeMapping STAINED_GLASS_BLOCK = new ColoredSchemeMapping(Material.STAINED_GLASS);
    public static final SchemeMapping HARDENED_CLAY = new ColoredSchemeMapping(Material.STAINED_CLAY);
    public static final SchemeMapping WOOL = new ColoredSchemeMapping(Material.WOOL);
    public static final SchemeMapping EMPTY = Collections::emptyMap;

    private StandardSchemeMappings() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
