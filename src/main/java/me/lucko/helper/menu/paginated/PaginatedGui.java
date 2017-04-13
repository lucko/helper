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

package me.lucko.helper.menu.paginated;

import com.google.common.collect.ImmutableList;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuScheme;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class PaginatedGui extends Gui {

    private final MenuScheme scheme;
    private final List<Integer> itemSlots;

    private final int nextPageSlot;
    private final int previousPageSlot;
    private final Function<PageInfo, ItemStack> nextPageItem;
    private final Function<PageInfo, ItemStack> previousPageItem;

    private List<Item> content;

    // starts at 1
    private int page;

    public PaginatedGui(Function<PaginatedGui, List<Item>> content, Player player, PaginatedGuiBuilder model) {
        super(player, model.getLines(), model.getTitle());

        this.content = ImmutableList.copyOf(content.apply(this));
        this.page = 1;

        this.scheme = model.getScheme();
        this.itemSlots = ImmutableList.copyOf(model.getItemSlots());
        this.nextPageSlot = model.getNextPageSlot();
        this.previousPageSlot = model.getPreviousPageSlot();
        this.nextPageItem = model.getNextPageItem();
        this.previousPageItem = model.getPreviousPageItem();
    }

    @Override
    public void redraw() {
        if (isFirstDraw()) {
            scheme.apply(this);
        }

        // get available slots for items
        List<Integer> slots = new ArrayList<>(itemSlots);

        // work out the items to display on this page
        List<List<Item>> pages = divideList(content, slots.size());
        List<Item> page = pages.isEmpty() ? new ArrayList<>() : pages.get(this.page - 1);

        // place prev/next page buttons
        if (this.page == 1) {
            // can't go back further
            removeItem(previousPageSlot);
        } else {
            setItem(previousPageSlot, ItemStackBuilder.of(previousPageItem.apply(PageInfo.create(this.page, pages.size())))
                    .build(() -> {
                        this.page = this.page - 1;
                        redraw();
                    }));
        }

        if (this.page >= pages.size()) {
            // can't go forward a page
            removeItem(nextPageSlot);
        } else {
            setItem(nextPageSlot, ItemStackBuilder.of(nextPageItem.apply(PageInfo.create(this.page, pages.size())))
                    .build(() -> {
                        this.page = this.page + 1;
                        redraw();
                    }));
        }

        // remove previous items
        if (!isFirstDraw()) {
            slots.forEach(this::removeItem);
        }

        // place the actual items
        for (Item item : page) {
            int index = slots.remove(0);
            setItem(index, item);
        }
    }

    private static <T> List<List<T>> divideList(List<T> source, int size) {
        List<List<T>> lists = new ArrayList<>();
        Iterator<T> it = source.iterator();
        while (it.hasNext()) {
            List<T> subList = new ArrayList<>();
            for (int i = 0; it.hasNext() && i < size; i++) {
                subList.add(it.next());
            }
            lists.add(subList);
        }
        return lists;
    }

    public void updateContent(List<Item> content) {
        this.content = ImmutableList.copyOf(content);
    }

}
