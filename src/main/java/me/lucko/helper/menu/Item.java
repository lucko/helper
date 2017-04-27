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

package me.lucko.helper.menu;

import java.util.Map;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Preconditions;

public class Item {

    private final Map<ClickType, ItemClickHandler> handlers;
    private final ItemStack itemStack;

    public Item(Map<ClickType, ItemClickHandler> handlers, ItemStack itemStack) {
        Preconditions.checkNotNull(handlers, "handlers");
        Preconditions.checkNotNull(itemStack, "itemStack");

        this.handlers = handlers;
        this.itemStack = itemStack;
    }

    public Map<ClickType, ItemClickHandler> getHandlers() {
        return handlers;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
    
	public interface ItemClickHandler {
		
		void handle(final InventoryClickEvent event);
		
	}

	public interface RunnableHandler extends Runnable, ItemClickHandler {

		public static ItemClickHandler of(final Runnable runnable) {
			return runnable == null ? null : (RunnableHandler) runnable::run;
		}
		
		default void handle(final InventoryClickEvent event) {
			run();
		}

	}

}
