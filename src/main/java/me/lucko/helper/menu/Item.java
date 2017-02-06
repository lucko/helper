package me.lucko.helper.menu;

import com.google.common.base.Preconditions;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class Item {

    private final Map<ClickType, Runnable> handlers;
    private final ItemStack itemStack;

    public Item(Map<ClickType, Runnable> handlers, ItemStack itemStack) {
        Preconditions.checkNotNull(handlers, "handlers");
        Preconditions.checkNotNull(itemStack, "itemStack");

        this.handlers = handlers;
        this.itemStack = itemStack;
    }

    public Map<ClickType, Runnable> getHandlers() {
        return handlers;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
