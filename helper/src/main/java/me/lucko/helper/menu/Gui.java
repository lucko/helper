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

import me.lucko.helper.Events;
import me.lucko.helper.Scheduler;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.metadata.MetadataMap;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.registry.TerminableRegistry;
import me.lucko.helper.utils.Color;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A simple GUI abstraction
 */
@NonnullByDefault
public abstract class Gui implements TerminableConsumer {
    public static final MetadataKey<Gui> OPEN_GUI_KEY = MetadataKey.create("open-gui", Gui.class);

    /**
     * Utility method to get the number of lines needed for x items
     *
     * @param count the number of items
     * @return the number of lines needed
     */
    public static int getMenuSize(int count) {
        Preconditions.checkArgument(count >= 0, "count < 0");
        return getMenuSize(count, 9);
    }

    /**
     * Utility method to get the number of lines needed for x items
     *
     * @param count the number of items
     * @param itemsPerLine the number of items per line
     * @return the number of lines needed
     */
    public static int getMenuSize(int count, int itemsPerLine) {
        Preconditions.checkArgument(itemsPerLine >= 1, "itemsPerLine < 1");
        return (count / itemsPerLine + ((count % itemsPerLine != 0) ? 1 : 0));
    }

    // The player holding the GUI
    private final Player player;
    // The backing inventory instance
    private final Inventory inventory;
    // The initial title set when the inventory was made.
    private final String initialTitle;
    // The slots in the gui, lazily loaded
    private final Map<Integer, SimpleSlot> slots;
    // This remains true until after #redraw is called for the first time
    private boolean firstDraw = true;
    // A function used to build a fallback page when this page is closed.
    @Nullable
    private Function<Player, Gui> fallbackGui = null;

    // Callbacks to be ran when the GUI is invalidated (closed). useful for cancelling tick tasks
    // Also contains the event handlers bound to this GUI, currently listening to events
    private final TerminableRegistry terminableRegistry = TerminableRegistry.create();

    private boolean valid = false;

    public Gui(Player player, int lines, String title) {
        this.player = Preconditions.checkNotNull(player, "player");
        this.initialTitle = Color.colorize(Preconditions.checkNotNull(title, "title"));
        this.inventory = Bukkit.createInventory(player, lines * 9, this.initialTitle);
        this.slots = new HashMap<>();
    }

    /**
     * Places items on the GUI. Called when the GUI is opened.
     * Use {@link #isFirstDraw()} to determine if this is the first time redraw has been called.
     */
    public abstract void redraw();

    /**
     * Gets the player viewing this Gui
     *
     * @return the player viewing this gui
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the delegate Bukkit inventory
     *
     * @return the bukkit inventory being wrapped by this instance
     */
    public Inventory getHandle() {
        return inventory;
    }

    /**
     * Gets the initial title which was set when this GUI was made
     *
     * @return the initial title used when this GUI was made
     */
    public String getInitialTitle() {
        return initialTitle;
    }

    @Nullable
    public Function<Player, Gui> getFallbackGui() {
        return fallbackGui;
    }

    public void setFallbackGui(@Nullable Function<Player, Gui> fallbackGui) {
        this.fallbackGui = fallbackGui;
    }

    @Nonnull
    @Override
    public <T extends Terminable> T bind(@Nonnull T terminable) {
        return terminableRegistry.bind(terminable);
    }

    @Nonnull
    @Override
    public <T extends Runnable> T bindRunnable(@Nonnull T runnable) {
        return terminableRegistry.bindRunnable(runnable);
    }

    public boolean isFirstDraw() {
        return firstDraw;
    }

    public Slot getSlot(int slot) {
        if (slot < 0 || slot >= inventory.getSize()) {
            throw new IllegalArgumentException("Invalid slot id: " + slot);
        }

        return slots.computeIfAbsent(slot, i -> new SimpleSlot(this, i));
    }

    public void setItem(int slot, Item item) {
        getSlot(slot).applyFromItem(item);
    }

    public void setItems(Item item, int... slots) {
        Preconditions.checkNotNull(item, "item");
        for (int slot : slots) {
            setItem(slot, item);
        }
    }

    public void setItems(Iterable<Integer> slots, Item item) {
        Preconditions.checkNotNull(item, "item");
        Preconditions.checkNotNull(slots, "slots");
        for (int slot : slots) {
            setItem(slot, item);
        }
    }

    public int getFirstEmpty() {
        int ret = inventory.firstEmpty();
        if (ret < 0) {
            throw new IndexOutOfBoundsException("no empty slots");
        }
        return ret;
    }

    public Optional<Slot> getFirstEmptySlot() {
        int ret = inventory.firstEmpty();
        if (ret < 0) {
            return Optional.empty();
        }
        return Optional.of(getSlot(ret));
    }

    public void addItem(Item item) {
        Preconditions.checkNotNull(item, "item");
        getFirstEmptySlot().ifPresent(s -> s.applyFromItem(item));
    }

    public void addItems(Iterable<Item> items) {
        Preconditions.checkNotNull(items, "items");
        for (Item item : items) {
            addItem(item);
        }
    }

    public void fillWith(Item item) {
        Preconditions.checkNotNull(item, "item");
        for (int i = 0; i < inventory.getSize(); ++i) {
            setItem(i, item);
        }
    }

    public void removeItem(int slot) {
        getSlot(slot).clear();
    }

    public void removeItems(int... slots) {
        for (int slot : slots) {
            removeItem(slot);
        }
    }

    public void removeItems(Iterable<Integer> slots) {
        Preconditions.checkNotNull(slots, "slots");
        for (int slot : slots) {
            removeItem(slot);
        }
    }

    public void clearItems() {
        inventory.clear();
        slots.values().forEach(Slot::clearBindings);
    }

    public void open() {
        if (valid) {
            throw new IllegalStateException("Gui is already opened.");
        }

        firstDraw = true;
        try {
            redraw();
        } catch (Exception e) {
            e.printStackTrace();
            invalidate();
            return;
        }

        firstDraw = false;
        startListening();
        player.openInventory(inventory);
        Metadata.provideForPlayer(player).put(OPEN_GUI_KEY, this);
        valid = true;
    }

    public void close() {
        player.closeInventory();
    }

    private void invalidate() {
        valid = false;

        MetadataMap metadataMap = Metadata.provideForPlayer(player);
        Gui existing = metadataMap.getOrNull(OPEN_GUI_KEY);
        if (existing == this) {
            metadataMap.remove(OPEN_GUI_KEY);
        }

        // stop listening
        terminableRegistry.terminate();

        // clear all items from the GUI, just in case the menu didn't close properly.
        clearItems();
    }

    /**
     * Returns true unless this GUI has been invalidated, through being closed, or the player leaving.
     * @return true unless this GUI has been invalidated.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Registers the event handlers for this GUI
     */
    private void startListening() {
        Events.subscribe(InventoryClickEvent.class)
                .filter(e -> e.getInventory().getHolder() != null)
                .filter(e -> e.getInventory().getHolder().equals(player))
                .handler(e -> {
                    e.setCancelled(true);

                    if (!isValid()) {
                        close();
                    }

                    int slotId = e.getRawSlot();

                    // check if the click was in the top inventory
                    if (slotId != e.getSlot()) {
                        return;
                    }

                    SimpleSlot slot = slots.get(slotId);
                    if (slot != null) {
                        slot.handle(e);
                    }
                })
                .bindWith(this);

        Events.subscribe(PlayerQuitEvent.class)
                .filter(e -> e.getPlayer().equals(player))
                .filter(e -> isValid())
                .handler(e -> invalidate())
                .bindWith(this);

        Events.subscribe(InventoryCloseEvent.class)
                .filter(e -> e.getPlayer().equals(player))
                .filter(e -> e.getInventory().equals(inventory))
                .filter(e -> isValid())
                .handler(e -> {
                    invalidate();

                    // Check for a fallback GUI
                    Function<Player, Gui> fallback = fallbackGui;
                    if (fallback == null) {
                        return;
                    }

                    // Open at a delay
                    Scheduler.runLaterSync(() -> {
                        if (!player.isOnline()) {
                            return;
                        }
                        fallback.apply(player).open();
                    }, 1L);
                })
                .bindWith(this);
    }

}
