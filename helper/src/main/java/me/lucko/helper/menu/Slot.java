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

import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a slot in a {@link Gui}.
 *
 * All changes made to this object are applied to the backing Gui instance, and vice versa.
 */
public interface Slot {

    /**
     * Gets the GUI this slot references
     *
     * @return the parent gui
     */
    @Nonnull
    Gui gui();

    /**
     * Gets the id of this slot
     *
     * @return the id
     */
    int getId();

    /**
     * Applies an item model to this slot.
     *
     * @param item the item
     * @return this slot
     */
    Slot applyFromItem(Item item);

    /**
     * Gets the item in this slot
     *
     * @return the item in this slot
     */
    @Nullable
    ItemStack getItem();

    /**
     * Gets if this slot has an item
     *
     * @return true if this slot has an item
     */
    boolean hasItem();

    /**
     * Sets the item in this slot
     *
     * @param item the new item
     * @return this slot
     */
    @Nonnull
    Slot setItem(@Nonnull ItemStack item);

    /**
     * Clears all attributes of the slot.
     *
     * @return this slot
     */
    Slot clear();

    /**
     * Clears the item in this slot
     *
     * @return this slot
     */
    @Nonnull
    Slot clearItem();

    /**
     * Clears all bindings on this slot.
     *
     * @return this slot
     */
    @Nonnull
    Slot clearBindings();

    /**
     * Clears all bindings on this slot for a given click type.
     *
     * @return this slot
     */
    @Nonnull
    Slot clearBindings(ClickType type);

    @Nonnull
    Slot bind(@Nonnull ClickType type, @Nonnull Consumer<InventoryClickEvent> handler);

    @Nonnull
    Slot bind(@Nonnull ClickType type, @Nonnull Runnable handler);

    @Nonnull
    Slot bind(@Nonnull Consumer<InventoryClickEvent> handler, @Nonnull ClickType... types);

    @Nonnull
    Slot bind(@Nonnull Runnable handler, @Nonnull ClickType... types);

    @Nonnull
    <T extends Runnable> Slot bindAllRunnables(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers);

    @Nonnull
    <T extends Consumer<InventoryClickEvent>> Slot bindAllConsumers(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers);

}
