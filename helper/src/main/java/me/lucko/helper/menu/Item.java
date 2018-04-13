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

import com.google.common.collect.ImmutableMap;

import me.lucko.helper.utils.Delegates;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The initial model of a clickable item in a {@link Gui}. Immutable.
 */
public class Item {

    @Nonnull
    public static Item.Builder builder(@Nonnull ItemStack itemStack) {
        return new Builder(itemStack);
    }

    // the click handlers for this item
    private final Map<ClickType, Consumer<InventoryClickEvent>> handlers;
    // the backing itemstack
    private final ItemStack itemStack;

    public Item(@Nonnull Map<ClickType, Consumer<InventoryClickEvent>> handlers, @Nonnull ItemStack itemStack) {
        this.handlers = ImmutableMap.copyOf(Objects.requireNonNull(handlers, "handlers"));
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
    }

    /**
     * Gets the click handlers for this Item.
     *
     * @return the click handlers
     */
    @Nonnull
    public Map<ClickType, Consumer<InventoryClickEvent>> getHandlers() {
        return this.handlers;
    }

    /**
     * Gets the ItemStack backing this Item.
     *
     * @return the backing itemstack
     */
    @Nonnull
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Aids creation of {@link Item} instances.
     */
    public static final class Builder {
        private final ItemStack itemStack;
        private final Map<ClickType, Consumer<InventoryClickEvent>> handlers;

        private Builder(@Nonnull ItemStack itemStack) {
            this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
            this.handlers = new HashMap<>();
        }

        @Nonnull
        public Builder bind(@Nonnull ClickType type, @Nullable Consumer<InventoryClickEvent> handler) {
            Objects.requireNonNull(type, "type");
            if (handler != null) {
                this.handlers.put(type, handler);
            } else {
                this.handlers.remove(type);
            }
            return this;
        }

        @Nonnull
        public Builder bind(@Nonnull ClickType type, @Nullable Runnable handler) {
            Objects.requireNonNull(type, "type");
            if (handler != null) {
                this.handlers.put(type, transformRunnable(handler));
            } else {
                this.handlers.remove(type);
            }
            return this;
        }

        @Nonnull
        public Builder bind(@Nullable Consumer<InventoryClickEvent> handler, @Nonnull ClickType... types) {
            for (ClickType type : types) {
                bind(type, handler);
            }
            return this;
        }

        @Nonnull
        public Builder bind(@Nullable Runnable handler, @Nonnull ClickType... types) {
            for (ClickType type : types) {
                bind(type, handler);
            }
            return this;
        }

        @Nonnull
        public <T extends Runnable> Builder bindAllRunnables(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers) {
            Objects.requireNonNull(handlers, "handlers");
            for (Map.Entry<ClickType, T> handler : handlers) {
                bind(handler.getKey(), handler.getValue());
            }
            return this;
        }

        @Nonnull
        public <T extends Consumer<InventoryClickEvent>> Builder bindAllConsumers(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers) {
            Objects.requireNonNull(handlers, "handlers");
            for (Map.Entry<ClickType, T> handler : handlers) {
                bind(handler.getKey(), handler.getValue());
            }
            return this;
        }

        @Nonnull
        public Item build() {
            return new Item(this.handlers, this.itemStack);
        }
    }

    @Nonnull
    public static Consumer<InventoryClickEvent> transformRunnable(@Nonnull Runnable runnable) {
        return Delegates.runnableToConsumer(runnable);
    }
}
