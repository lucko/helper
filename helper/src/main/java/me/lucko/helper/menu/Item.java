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

package me.lucko.helper.menu;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A clickable item in a {@link Gui}
 */
public class Item {

    public static Consumer<InventoryClickEvent> transformRunnable(Runnable runnable) {
        return new DelegateConsumer<>(runnable);
    }

    public static Item.Builder builder(ItemStack itemStack) {
        return new Builder(itemStack);
    }

    private final Map<ClickType, Consumer<InventoryClickEvent>> handlers;
    private final ItemStack itemStack;

    public Item(Map<ClickType, Consumer<InventoryClickEvent>> handlers, ItemStack itemStack) {
        this.handlers = Preconditions.checkNotNull(handlers, "handlers");
        this.itemStack = Preconditions.checkNotNull(itemStack, "itemStack");
    }

    public Map<ClickType, Consumer<InventoryClickEvent>> getHandlers() {
        return handlers;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static final class Builder {
        private final ItemStack itemStack;
        private final Map<ClickType, Consumer<InventoryClickEvent>> handlers;

        private Builder(ItemStack itemStack) {
            this.itemStack = Preconditions.checkNotNull(itemStack, "itemStack");
            this.handlers = new HashMap<>();
        }

        public Builder bind(ClickType type, Consumer<InventoryClickEvent> handler) {
            Preconditions.checkNotNull(type, "type");
            if (handler != null) {
                handlers.put(type, handler);
            } else {
                handlers.remove(type);
            }
            return this;
        }

        public Builder bind(ClickType type, Runnable handler) {
            Preconditions.checkNotNull(type, "type");
            if (handler != null) {
                handlers.put(type, transformRunnable(handler));
            } else {
                handlers.remove(type);
            }
            return this;
        }

        public Builder bind(Consumer<InventoryClickEvent> handler, ClickType... types) {
            for (ClickType type : types) {
                bind(type, handler);
            }
            return this;
        }

        public Builder bind(Runnable handler, ClickType... types) {
            for (ClickType type : types) {
                bind(type, handler);
            }
            return this;
        }

        public <T extends Runnable> Builder bindAllRunnables(Iterable<Map.Entry<ClickType, T>> handlers) {
            Preconditions.checkNotNull(handlers, "handlers");
            for (Map.Entry<ClickType, T> handler : handlers) {
                bind(handler.getKey(), handler.getValue());
            }
            return this;
        }

        public <T extends Consumer<InventoryClickEvent>> Builder bindAllConsumers(Iterable<Map.Entry<ClickType, T>> handlers) {
            Preconditions.checkNotNull(handlers, "handlers");
            for (Map.Entry<ClickType, T> handler : handlers) {
                bind(handler.getKey(), handler.getValue());
            }
            return this;
        }

        public Item build() {
            return new Item(ImmutableMap.copyOf(handlers), itemStack);
        }
    }

    private static final class DelegateConsumer<T> implements Consumer<T> {
        private final Runnable delegate;

        private DelegateConsumer(Runnable delegate) {
            this.delegate = delegate;
        }

        public Runnable getDelegate() {
            return delegate;
        }

        @Override
        public void accept(T t) {
            delegate.run();
        }
    }
}
