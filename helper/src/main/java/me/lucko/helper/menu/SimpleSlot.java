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

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Basic implementation of {@link Slot}.
 */
public class SimpleSlot implements Slot {

    // the parent gui
    private final Gui gui;

    // the id of this slot
    private final int id;

    // the click handlers for this slot
    protected final Map<ClickType, Set<Consumer<InventoryClickEvent>>> handlers;

    public SimpleSlot(@Nonnull Gui gui, int id) {
        this.gui = gui;
        this.id = id;
        this.handlers = Collections.synchronizedMap(new EnumMap<>(ClickType.class));
    }

    public void handle(@Nonnull InventoryClickEvent event) {
        Set<Consumer<InventoryClickEvent>> handlers = this.handlers.get(event.getClick());
        if (handlers == null) {
            return;
        }
        for (Consumer<InventoryClickEvent> handler : handlers) {
            try {
                handler.accept(event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Gets the GUI this slot references
     *
     * @return the parent gui
     */
    @Nonnull
    @Override
    public Gui gui() {
        return this.gui;
    }

    /**
     * Gets the id of this slot
     *
     * @return the id
     */
    @Override
    public int getId() {
        return this.id;
    }

    /**
     * Applies an item model to this slot.
     *
     * @param item the item
     * @return this slot
     */
    @Override
    public Slot applyFromItem(Item item) {
        Objects.requireNonNull(item, "item");
        setItem(item.getItemStack());
        clearBindings();
        bindAllConsumers(item.getHandlers().entrySet());
        return this;
    }

    /**
     * Gets the item in this slot
     *
     * @return the item in this slot
     */
    @Nullable
    @Override
    public ItemStack getItem() {
        return this.gui.getHandle().getItem(this.id);
    }

    /**
     * Gets if this slot has an item
     *
     * @return true if this slot has an item
     */
    @Override
    public boolean hasItem() {
        return getItem() != null;
    }

    /**
     * Sets the item in this slot
     *
     * @param item the new item
     * @return this slot
     */
    @Nonnull
    @Override
    public Slot setItem(@Nonnull ItemStack item) {
        Objects.requireNonNull(item, "item");
        this.gui.getHandle().setItem(this.id, item);
        return this;
    }

    /**
     * Clears all attributes of the slot.
     *
     * @return this slot
     */
    @Override
    public Slot clear() {
        clearItem();
        clearBindings();
        return this;
    }

    /**
     * Clears the item in this slot
     *
     * @return this slot
     */
    @Nonnull
    @Override
    public Slot clearItem() {
        this.gui.getHandle().clear(this.id);
        return this;
    }

    /**
     * Clears all bindings on this slot.
     *
     * @return this slot
     */
    @Nonnull
    @Override
    public Slot clearBindings() {
        this.handlers.clear();
        return this;
    }

    /**
     * Clears all bindings on this slot for a given click type.
     *
     * @return this slot
     */
    @Nonnull
    @Override
    public Slot clearBindings(ClickType type) {
        this.handlers.remove(type);
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull ClickType type, @Nonnull Consumer<InventoryClickEvent> handler) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(handler, "handler");
        this.handlers.computeIfAbsent(type, t -> ConcurrentHashMap.newKeySet()).add(handler);
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull ClickType type, @Nonnull Runnable handler) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(handler, "handler");
        this.handlers.computeIfAbsent(type, t -> ConcurrentHashMap.newKeySet()).add(Item.transformRunnable(handler));
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull Consumer<InventoryClickEvent> handler, @Nonnull ClickType... types) {
        for (ClickType type : types) {
            bind(type, handler);
        }
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull Runnable handler, @Nonnull ClickType... types) {
        for (ClickType type : types) {
            bind(type, handler);
        }
        return this;
    }

    @Nonnull
    @Override
    public <T extends Runnable> Slot bindAllRunnables(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers) {
        Objects.requireNonNull(handlers, "handlers");
        for (Map.Entry<ClickType, T> handler : handlers) {
            bind(handler.getKey(), handler.getValue());
        }
        return this;
    }

    @Nonnull
    @Override
    public <T extends Consumer<InventoryClickEvent>> Slot bindAllConsumers(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers) {
        Objects.requireNonNull(handlers, "handlers");
        for (Map.Entry<ClickType, T> handler : handlers) {
            bind(handler.getKey(), handler.getValue());
        }
        return this;
    }

}
