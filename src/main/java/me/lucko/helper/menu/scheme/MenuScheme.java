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

import com.google.common.collect.ImmutableList;

import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Helps to populate a menu with border items
 */
public class MenuScheme {
    private final Map<Integer, Item> mapping;
    private final List<boolean[]> maskRows;
    private final List<int[]> schemeRows;

    public MenuScheme(SchemeMapping mapping) {
        this.mapping = mapping == null ? StandardSchemeMappings.EMPTY.getMapping() : mapping.getMapping();
        this.maskRows = new ArrayList<>();
        this.schemeRows = new ArrayList<>();
    }

    public MenuScheme() {
        this(null);
    }

    public MenuScheme mask(String s) {
        char[] chars = s.toCharArray();
        if (chars.length != 9) {
            throw new IllegalArgumentException("invalid mask: " + s);
        }
        boolean[] ret = new boolean[9];
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '1' || c == 't') {
                ret[i] = true;
            } else if (c == '0' || c == 'f') {
                ret[i] = false;
            } else {
                throw new IllegalArgumentException("invalid mask character: " + c);
            }
        }
        maskRows.add(ret);
        return this;
    }

    public MenuScheme masks(String... strings) {
        for (String s : strings) {
            mask(s);
        }
        return this;
    }

    public MenuScheme maskEmpty(int lines) {
        for (int i = 0; i < lines; i++) {
            maskRows.add(new boolean[]{false, false, false, false, false, false, false, false, false});
            schemeRows.add(new int[]{});
        }
        return this;
    }

    public MenuScheme scheme(int... schemeIds) {
        for (int schemeId : schemeIds) {
            if (!mapping.containsKey(schemeId)) {
                throw new IllegalArgumentException("mapping does not contain value for id: " + schemeId);
            }
        }
        schemeRows.add(schemeIds);
        return this;
    }

    public void apply(Gui gui) {
        try {
            // the index of the item slot in the inventory
            AtomicInteger invIndex = new AtomicInteger(-1);

            // iterate all of the loaded masks
            for (int i = 0; i < maskRows.size(); i++) {
                boolean[] mask = maskRows.get(i);
                int[] scheme = schemeRows.get(i);

                AtomicInteger schemeIndex = new AtomicInteger(-1);

                // iterate the values in the mask (0 --> 8)
                for (boolean b : mask) {

                    // increment the index in the gui. we're handling a new item.
                    int index = invIndex.incrementAndGet();

                    // if this index is masked.
                    if (b) {
                        // the index of the mapping from schemeRows
                        int schemeId = schemeIndex.incrementAndGet();

                        // this is the value from the scheme map for this slot.
                        int schemeMappingId = scheme[schemeId];

                        // lookup the value for this location, and apply it to the gui
                        gui.setItem(index, mapping.get(schemeMappingId));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getMaskedIndexes() {
        List<Integer> ret = new ArrayList<>();
        try {
            // the index of the item slot in the inventory
            AtomicInteger invIndex = new AtomicInteger(-1);

            // iterate all of the loaded masks
            for (int i = 0; i < maskRows.size(); i++) {
                boolean[] mask = maskRows.get(i);

                // iterate the values in the mask (0 --> 8)
                for (boolean b : mask) {

                    // increment the index in the gui. we're handling a new item.
                    int index = invIndex.incrementAndGet();

                    // if this index is masked.
                    if (b) {
                        ret.add(index);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public ImmutableList<Integer> getMaskedIndexesImmutable() {
        return ImmutableList.copyOf(getMaskedIndexes());
    }
}
