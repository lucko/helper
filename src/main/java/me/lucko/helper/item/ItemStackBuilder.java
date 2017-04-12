/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me> Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions: The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software. THE
 * SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package me.lucko.helper.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.Item.ItemClickHandler;
import me.lucko.helper.menu.Item.RunnableHandler;
import me.lucko.helper.version.VersionSpecific;

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
		final ItemMeta itemMeta = itemStack.getItemMeta();
		meta.accept(itemMeta);
		itemStack.setItemMeta(itemMeta);
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
		return flag(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_DESTROYS,
				ItemFlag.HIDE_PLACED_ON);
	}

	public ItemStackBuilder showAttributes() {
		return unflag(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_POTION_EFFECTS,
				ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON);
	}

	public ItemStackBuilder color(Color color) {
		return transformMeta(meta->{
			if(meta instanceof LeatherArmorMeta) {
				((LeatherArmorMeta) meta).setColor(color);
			}
		});
	}

	public ItemStackBuilder apply(Consumer<ItemStackBuilder> consumer) {
		consumer.accept(this);
		return this;
	}

	public ItemStackBuilder skullTexture(final String texture) {
		return transformMeta(meta -> {
			if (meta instanceof SkullMeta) {
				VersionSpecific.get().applySkullTexture((SkullMeta) meta, texture);
			}
		});
	}

	public ItemStackBuilder skullOwner(final String owner) {
		return transformMeta(meta -> {
			if (meta instanceof SkullMeta) {
				((SkullMeta) meta).setOwner(owner);
			}
		});
	}

	public ItemStack build() {
		return itemStack;
	}

	public Item build(Runnable handler) {
		return build(RunnableHandler.of(handler));
	}

	public Item build(ItemClickHandler handler) {
		if (handler == null) {
			return new Item(ImmutableMap.of(), itemStack);
		}
		else {
			return new Item(ImmutableMap.of(ClickType.RIGHT, handler, ClickType.LEFT, handler), itemStack);
		}
	}

	public Item build(ClickType type, Runnable runnable) {
		return build(type, RunnableHandler.of(runnable));
	}
	
	public Item build(ClickType type, ItemClickHandler handler) {
		return new Item(ImmutableMap.of(type,handler), itemStack);
	}

	public Item build(Runnable rightClick, Runnable leftClick) {
		return build(rightClick == null ? null : RunnableHandler.of(rightClick), leftClick == null ? null : RunnableHandler.of(leftClick));
	}

	public Item build(ItemClickHandler right, ItemClickHandler left) {
		if (right != null) {
			if (left != null) {
				return new Item(ImmutableMap.of(ClickType.RIGHT, right, ClickType.LEFT, left), itemStack);
			}
			else {
				return new Item(ImmutableMap.of(ClickType.RIGHT, right), itemStack);
			}
		}
		else {
			if (left != null) {
				return new Item(ImmutableMap.of(ClickType.LEFT, left), itemStack);
			}
			else {
				return new Item(ImmutableMap.of(), itemStack);
			}
		}
	}

	public Item buildFromMap(Map<ClickType, Runnable> handlers) {
		final ImmutableMap.Builder<ClickType, ItemClickHandler> converted = ImmutableMap.builder();
		handlers.forEach((type, action) -> converted.put(type, RunnableHandler.of(action)));
		return new Item(converted.build(), itemStack);
	}
	
	public Item buildFromMapNew(Map<ClickType, ItemClickHandler> handlers) {
		return new Item(ImmutableMap.copyOf(handlers), itemStack);
	}	

}
