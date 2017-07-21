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

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.menu.Item;
import me.lucko.helper.utils.ImmutableCollectors;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Easily construct {@link ItemStack} instances
 */
public class ItemStackBuilder {
    private final ItemStack itemStack;

    public static ItemStackBuilder of(Material material) {
        return new ItemStackBuilder(new ItemStack(material)).hideAttributes();
    }

    public static ItemStackBuilder of(ItemStack itemStack) {
        return new ItemStackBuilder(itemStack).hideAttributes();
    }

    private ItemStackBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStackBuilder transform(Consumer<ItemStack> is) {
        is.accept(itemStack);
        return this;
    }

    public ItemStackBuilder transformMeta(Consumer<ItemMeta> meta) {
        ItemMeta m = itemStack.getItemMeta();
        if (m != null) {
            meta.accept(m);
            itemStack.setItemMeta(m);
        }
        return this;
    }

    public ItemStackBuilder name(String name) {
        return transformMeta(meta -> meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name)));
    }

    public ItemStackBuilder type(Material material) {
        return transform(itemStack -> itemStack.setType(material));
    }

    public ItemStackBuilder lore(String name) {
        return transformMeta(meta -> {
            List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
            lore.add(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore);
        });
    }

    public ItemStackBuilder clearLore() {
        return transformMeta(meta -> meta.setLore(new ArrayList<>()));
    }

    public ItemStackBuilder durability(int durability) {
        return transform(itemStack -> itemStack.setDurability((short) durability));
    }

    public ItemStackBuilder data(int data) {
        return durability(data);
    }

    public ItemStackBuilder amount(int amount) {
        return transform(itemStack -> itemStack.setAmount(amount));
    }

    public ItemStackBuilder enchant(Enchantment enchantment, int level) {
        return transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, level));
    }

    public ItemStackBuilder enchant(Enchantment enchantment) {
        return transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, 1));
    }

    public ItemStackBuilder clearEnchantments() {
        return transform(itemStack -> itemStack.getEnchantments().keySet().forEach(itemStack::removeEnchantment));
    }

    public ItemStackBuilder flag(ItemFlag... flags) {
        return transformMeta(meta -> meta.addItemFlags(flags));
    }

    public ItemStackBuilder unflag(ItemFlag... flags) {
        return transformMeta(meta -> meta.removeItemFlags(flags));
    }

    public ItemStackBuilder hideAttributes() {
        return flag(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
    }

    public ItemStackBuilder showAttributes() {
        return unflag(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
    }

    public ItemStackBuilder color(Color color) {
        return transform(itemStack -> {
            Material type = itemStack.getType();
            if (type == Material.LEATHER_BOOTS || type == Material.LEATHER_CHESTPLATE || type == Material.LEATHER_HELMET || type == Material.LEATHER_LEGGINGS) {
                LeatherArmorMeta meta = (LeatherArmorMeta) itemStack.getItemMeta();
                meta.setColor(color);
                itemStack.setItemMeta(meta);
            }
        });
    }

    public ItemStackBuilder apply(Consumer<ItemStackBuilder> consumer) {
        consumer.accept(this);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }

    public Item build(Runnable handler) {
        if (handler == null) {
            return new Item(ImmutableMap.of(), itemStack);
        } else {
            return new Item(ImmutableMap.of(ClickType.RIGHT, Item.transformRunnable(handler), ClickType.LEFT, Item.transformRunnable(handler)), itemStack);
        }
    }

    public Item build(ClickType type, Runnable runnable) {
        return new Item(ImmutableMap.of(type, Item.transformRunnable(runnable)), itemStack);
    }

    public Item build(Runnable rightClick, Runnable leftClick) {
        if (rightClick != null) {
            if (leftClick != null) {
                return new Item(ImmutableMap.of(ClickType.RIGHT, Item.transformRunnable(rightClick), ClickType.LEFT, Item.transformRunnable(leftClick)), itemStack);
            } else {
                return new Item(ImmutableMap.of(ClickType.RIGHT, Item.transformRunnable(rightClick)), itemStack);
            }
        } else {
            if (leftClick != null) {
                return new Item(ImmutableMap.of(ClickType.LEFT, Item.transformRunnable(leftClick)), itemStack);
            } else {
                return new Item(ImmutableMap.of(), itemStack);
            }
        }
    }

    public Item buildFromMap(Map<ClickType, Runnable> handlers) {
        return new Item(ImmutableMap.copyOf(handlers).entrySet().stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, v -> Item.transformRunnable(v.getValue()))), itemStack);
    }

    public Item buildConsumer(Consumer<InventoryClickEvent> handler) {
        if (handler == null) {
            return new Item(ImmutableMap.of(), itemStack);
        } else {
            return new Item(ImmutableMap.of(ClickType.RIGHT, handler, ClickType.LEFT, handler), itemStack);
        }
    }

    public Item buildConsumer(ClickType type, Consumer<InventoryClickEvent> runnable) {
        return new Item(ImmutableMap.of(type, runnable), itemStack);
    }

    public Item buildConsumer(Consumer<InventoryClickEvent> rightClick, Consumer<InventoryClickEvent> leftClick) {
        if (rightClick != null) {
            if (leftClick != null) {
                return new Item(ImmutableMap.of(ClickType.RIGHT, rightClick, ClickType.LEFT, leftClick), itemStack);
            } else {
                return new Item(ImmutableMap.of(ClickType.RIGHT, rightClick), itemStack);
            }
        } else {
            if (leftClick != null) {
                return new Item(ImmutableMap.of(ClickType.LEFT, leftClick), itemStack);
            } else {
                return new Item(ImmutableMap.of(), itemStack);
            }
        }
    }

    public Item buildFromConsumerMap(Map<ClickType, Consumer<InventoryClickEvent>> handlers) {
        return new Item(ImmutableMap.copyOf(handlers), itemStack);
    }

}