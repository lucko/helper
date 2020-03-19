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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Useless implementation of {@link Slot} to fulfill not-null contracts.
 */
public class DummySlot implements Slot {

    // the parent gui
    private final Gui gui;

    // the id of this slot
    private final int id;

    public DummySlot(@Nonnull Gui gui, int id) {
        this.gui = gui;
        this.id = id;
    }

    @Nonnull
    @Override
    public Gui gui() {
        return this.gui;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public Slot applyFromItem(Item item) {
        return this;
    }

    @Nullable
    @Override
    public ItemStack getItem() {
        return null;
    }

    @Override
    public boolean hasItem() {
        return false;
    }

    @Nonnull
    @Override
    public Slot setItem(@Nonnull ItemStack item) {
        return this;
    }

    @Override
    public Slot clear() {
        return this;
    }

    @Nonnull
    @Override
    public Slot clearItem() {
        return this;
    }

    @Nonnull
    @Override
    public Slot clearBindings() {
        return this;
    }

    @Nonnull
    @Override
    public Slot clearBindings(ClickType type) {
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull ClickType type, @Nonnull Consumer<InventoryClickEvent> handler) {
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull ClickType type, @Nonnull Runnable handler) {
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull Consumer<InventoryClickEvent> handler, @Nonnull ClickType... types) {
        return this;
    }

    @Nonnull
    @Override
    public Slot bind(@Nonnull Runnable handler, @Nonnull ClickType... types) {
        return this;
    }

    @Nonnull
    @Override
    public <T extends Runnable> Slot bindAllRunnables(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers) {
        return this;
    }

    @Nonnull
    @Override
    public <T extends Consumer<InventoryClickEvent>> Slot bindAllConsumers(@Nonnull Iterable<Map.Entry<ClickType, T>> handlers) {
        return this;
    }
}
