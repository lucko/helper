package me.lucko.helper.menu.scheme;

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;

import org.bukkit.Material;

import java.util.Map;

class StainedGlassScheme implements SchemeMapping {
    private final Map<Integer, Item> mapping = ImmutableMap.<Integer, Item>builder()
            .put(0, makeGlass(0))
            .put(1, makeGlass(1))
            .put(2, makeGlass(2))
            .put(3, makeGlass(3))
            .put(4, makeGlass(4))
            .put(5, makeGlass(5))
            .put(6, makeGlass(6))
            .put(7, makeGlass(7))
            .put(8, makeGlass(8))
            .put(9, makeGlass(9))
            .put(10, makeGlass(10))
            .put(11, makeGlass(11))
            .put(12, makeGlass(12))
            .put(13, makeGlass(13))
            .put(14, makeGlass(14))
            .put(15, makeGlass(15))
            .build();

    StainedGlassScheme() {}

    @Override
    public Map<Integer, Item> getMapping() {
        return mapping;
    }

    private static Item makeGlass(int data) {
        return ItemStackBuilder.of(Material.STAINED_GLASS_PANE).name("&f").data(data).build(null);
    }
}
