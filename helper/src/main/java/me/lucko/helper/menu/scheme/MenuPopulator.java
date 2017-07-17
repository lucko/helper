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

package me.lucko.helper.menu.scheme;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility to help place items into a {@link Gui}
 */
public class MenuPopulator {

    private final Gui gui;
    private final ImmutableList<Integer> slots;
    private List<Integer> remainingSlots;

    public MenuPopulator(Gui gui, MenuScheme scheme) {
        Preconditions.checkNotNull(gui, "gui");
        Preconditions.checkNotNull(scheme, "scheme");

        this.remainingSlots = scheme.getMaskedIndexes();
        Preconditions.checkArgument(this.remainingSlots.size() > 0, "no slots in scheme");

        this.gui = gui;
        this.slots = ImmutableList.copyOf(this.remainingSlots);
    }

    public MenuPopulator(Gui gui, List<Integer> slots) {
        Preconditions.checkNotNull(gui, "gui");
        Preconditions.checkNotNull(slots, "slots");

        Preconditions.checkArgument(slots.size() > 0, "no slots in list");

        this.gui = gui;
        this.slots = ImmutableList.copyOf(slots);
        this.remainingSlots = new ArrayList<>(this.slots);
    }

    private MenuPopulator(MenuPopulator other) {
        this.gui = other.gui;
        this.slots = other.slots;
        this.remainingSlots = new ArrayList<>(this.slots);
    }

    /**
     * Gets an immutable copy of the slots used by this populator.
     *
     * @return the slots used by this populator.
     */
    public ImmutableList<Integer> getSlots() {
        return this.slots;
    }

    /**
     * Resets the slot order used by this populator to the state it was in upon construction
     */
    public void reset() {
        this.remainingSlots = new ArrayList<>(this.slots);
    }

    /**
     * Places an item onto the {@link Gui} using the next available slot in the populator
     *
     * @param item the item to place
     * @return the populator
     * @throws IllegalStateException if there are not more slots
     */
    public MenuPopulator accept(Item item) {
        if (placeIfSpace(item)) {
            return this;
        } else {
            throw new IllegalStateException("No more slots");
        }
    }

    /**
     * Places an item onto the {@link Gui} using the next available slot in the populator
     *
     * @param item the item to place
     * @return the populator
     */
    public MenuPopulator acceptIfSpace(Item item) {
        placeIfSpace(item);
        return this;
    }

    /**
     * Places an item onto the {@link Gui} using the next available slot in the populator
     *
     * @param item the item to place
     * @return true if there was a slot left in the populator to place this item onto, false otherwise
     */
    public boolean placeIfSpace(Item item) {
        Preconditions.checkNotNull(item, "item");
        if (remainingSlots.size() == 0) {
            return false;
        }

        int slot = remainingSlots.remove(0);
        gui.setItem(slot, item);
        return true;
    }

    /**
     * Gets the number of remaining slots in the populator.
     *
     * @return the number of remaining slots
     */
    public int getRemainingSpace() {
        return remainingSlots.size();
    }

    public MenuPopulator copy() {
        return new MenuPopulator(this);
    }
}
