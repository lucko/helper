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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import me.lucko.helper.Events;
import me.lucko.helper.Scheduler;
import me.lucko.helper.menu.Item.ItemClickHandler;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.TerminableRegistry;
import me.lucko.helper.utils.Color;
import me.lucko.helper.utils.Cooldown;

/**
 * A simple GUI abstraction
 */
public abstract class Gui implements Consumer<Terminable> {

    /**
     * Utility method to get the number of lines needed for x items
     * @param count the number of items
     * @return the number of lines needed
     */
    public static int getMenuSize(int count) {
        return (count / 9 + ((count % 9 != 0) ? 1 : 0));
    }

    // The player holding the GUI
    private final Player player;
    // The backing inventory instance
    private final Inventory inventory;
    // The initial title set when the inventory was made.
    private final String initialTitle;
    // The clickable items in the gui
    private final Map<Integer, Item> itemMap;
    // This remains true until after #redraw is called for the first time
    private boolean firstDraw = true;
    // A function used to build a fallback page when this page is closed.
    private Function<Player, Gui> fallbackGui = null;
    //Cooldown to prevent fast clicking
    private Cooldown clickCooldown = Cooldown.of(250, TimeUnit.MILLISECONDS);

    // Callbacks to be ran when the GUI is invalidated (closed). useful for cancelling tick tasks
    // Also contains the event handlers bound to this GUI, currently listening to events
    private final TerminableRegistry terminableRegistry = TerminableRegistry.create();

    private boolean valid = false;

    public Gui(Player player, int lines, String title) {
        this.player = player;
        this.initialTitle = Color.colorize(title);
        this.inventory = Bukkit.createInventory(player, lines * 9, this.initialTitle);
        this.itemMap = new HashMap<>();
    }

    /**
     * Places items on the GUI. Called when the GUI is opened.
     * Use {@link #isFirstDraw()} to determine if this is the first time {@link #redraw()} has been called.
     */
    public abstract void redraw();

    public Player getPlayer() {
        return player;
    }

    public Inventory getHandle() {
        return inventory;
    }

    public String getInitialTitle() {
        return initialTitle;
    }

    public Cooldown getClickCooldown() {
    	return clickCooldown;
    }
    
    public Function<Player, Gui> getFallbackGui() {
        return fallbackGui;
    }

    public void setFallbackGui(Function<Player, Gui> fallbackGui) {
        this.fallbackGui = fallbackGui;
    }

    public void addInvalidationCallback(Runnable r) {
        terminableRegistry.accept(Terminable.of(r));
    }
    
	public void setClickCooldown(final Cooldown clickCooldown) {
		this.clickCooldown = clickCooldown;
	}

    @Override
    public void accept(Terminable terminable) {
        terminableRegistry.accept(terminable);
    }

    public boolean isFirstDraw() {
        return firstDraw;
    }

    public void setItem(int slot, Item item) {
        itemMap.put(slot, item);
        inventory.setItem(slot, item.getItemStack());
    }

    public void setItems(Item item, int... slots) {
        for (int slot : slots) {
            setItem(slot, item);
        }
    }

    public void setItems(Iterable<Integer> slots, Item item) {
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

    public void addItem(Item item) {
        try {
            setItem(getFirstEmpty(), item);
        } catch (IndexOutOfBoundsException e) {
            // ignore
        }
    }

    public void addItems(Iterable<Item> items) {
        for (Item item : items) {
            addItem(item);
        }
    }

    public void removeItem(int slot) {
        itemMap.remove(slot);
        inventory.setItem(slot, null);
    }

    public void removeItems(int... slots) {
        for (int slot : slots) {
            removeItem(slot);
        }
    }

    public void removeItems(Iterable<Integer> slots) {
        for (int slot : slots) {
            removeItem(slot);
        }
    }

    public void clearItems() {
        itemMap.clear();
        inventory.clear();
    }

    public void fillWith(Item item) {
        for (int i = 0; i < inventory.getSize(); ++i) {
            setItem(i, item);
        }
    }

    public void open() {
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
        valid = true;
    }

    public void close() {
        player.closeInventory();
    }

    private void invalidate() {
        valid = false;

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
					if (!clickCooldown.test()) return;
                    
                    int slot = e.getRawSlot();

                    // check if the click was in the top inventory
                    if (slot != e.getSlot()) {
                        return;
                    }

                    Item item = itemMap.get(slot);
                    if (item != null) {
                        Map<ClickType, ItemClickHandler> handlers = item.getHandlers();
                        ItemClickHandler handler = handlers.get(e.getClick());
                        if (handler != null) {
                            try {
                                handler.handle(e);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                })
                .register(this);

        Events.subscribe(PlayerQuitEvent.class)
                .filter(e -> e.getPlayer().equals(player))
                .handler(e -> invalidate())
                .register(this);

        Events.subscribe(InventoryCloseEvent.class)
                .filter(e -> e.getPlayer().equals(player))
                .filter(e -> e.getInventory().equals(inventory))
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
                .register(this);
    }

}
