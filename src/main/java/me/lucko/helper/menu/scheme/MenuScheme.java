package me.lucko.helper.menu.scheme;

import com.google.common.base.Preconditions;

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
        Preconditions.checkNotNull(mapping, "mapping");
        this.mapping = mapping.getMapping();
        this.maskRows = new ArrayList<>();
        this.schemeRows = new ArrayList<>();
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
}
