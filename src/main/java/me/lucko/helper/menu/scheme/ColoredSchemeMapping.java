/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me>
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

import java.util.Map;

import org.bukkit.Material;

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.Item.ItemClickHandler;

class ColoredSchemeMapping implements SchemeMapping {
    private final Material material;
    private final Map<Integer, Item> mapping;

    ColoredSchemeMapping(Material material) {
        this.material = material;
        this.mapping = ImmutableMap.<Integer, Item>builder()
                .put(0, make(0))
                .put(1, make(1))
                .put(2, make(2))
                .put(3, make(3))
                .put(4, make(4))
                .put(5, make(5))
                .put(6, make(6))
                .put(7, make(7))
                .put(8, make(8))
                .put(9, make(9))
                .put(10, make(10))
                .put(11, make(11))
                .put(12, make(12))
                .put(13, make(13))
                .put(14, make(14))
                .put(15, make(15))
                .build();
    }

    @Override
    public Map<Integer, Item> getMapping() {
        return mapping;
    }

	private Item make(int data) {
		return ItemStackBuilder.of(material).name("&f").data(data).build((ItemClickHandler) null);
	}

}
