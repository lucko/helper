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

package me.lucko.helper.menu.paginated;

import com.google.common.collect.ImmutableList;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Item;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.menu.scheme.StandardSchemeMappings;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Specification class for a {@link PaginatedGui}.
 */
@NonnullByDefault
public class PaginatedGuiBuilder {

    public static final int DEFAULT_LINES = 6;

    public static final int DEFAULT_NEXT_PAGE_SLOT = new MenuScheme()
            .maskEmpty(5)
            .mask("000000010")
            .getMaskedIndexes().get(0);

    public static final int DEFAULT_PREVIOUS_PAGE_SLOT = new MenuScheme()
            .maskEmpty(5)
            .mask("010000000")
            .getMaskedIndexes().get(0);

    public static final List<Integer> DEFAULT_ITEM_SLOTS = new MenuScheme()
            .mask("011111110")
            .mask("011111110")
            .mask("011111110")
            .mask("011111110")
            .mask("011111110")
            .getMaskedIndexesImmutable();

    public static final MenuScheme DEFAULT_SCHEME = new MenuScheme(StandardSchemeMappings.STAINED_GLASS)
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .mask("100000001")
            .scheme(3, 3)
            .scheme(3, 3)
            .scheme(3, 3)
            .scheme(3, 3)
            .scheme(3, 3)
            .scheme(3, 3);

    public static final Function<PageInfo, ItemStack> DEFAULT_NEXT_PAGE_ITEM = pageInfo -> ItemStackBuilder.of(Material.ARROW)
            .name("&b&m--&b>")
            .lore("&fSwitch to the next page.")
            .lore("")
            .lore("&7Currently viewing page &b" + pageInfo.getCurrent() + "&7/&b" + pageInfo.getSize())
            .build();

    public static final Function<PageInfo, ItemStack> DEFAULT_PREVIOUS_PAGE_ITEM = pageInfo -> ItemStackBuilder.of(Material.ARROW)
            .name("&b<&b&m--")
            .lore("&fSwitch to the previous page.")
            .lore("")
            .lore("&7Currently viewing page &b" + pageInfo.getCurrent() + "&7/&b" + pageInfo.getSize())
            .build();

    public static PaginatedGuiBuilder create() {
        return new PaginatedGuiBuilder();
    }

    private int lines;
    private String title;
    private List<Integer> itemSlots;
    private int nextPageSlot;
    private int previousPageSlot;
    private MenuScheme scheme;
    private Function<PageInfo, ItemStack> nextPageItem;
    private Function<PageInfo, ItemStack> previousPageItem;

    private PaginatedGuiBuilder() {
        this.lines = DEFAULT_LINES;
        this.itemSlots = DEFAULT_ITEM_SLOTS;
        this.nextPageSlot = DEFAULT_NEXT_PAGE_SLOT;
        this.previousPageSlot = DEFAULT_PREVIOUS_PAGE_SLOT;
        this.scheme = DEFAULT_SCHEME;
        this.nextPageItem = DEFAULT_NEXT_PAGE_ITEM;
        this.previousPageItem = DEFAULT_PREVIOUS_PAGE_ITEM;
    }

    public PaginatedGuiBuilder copy() {
        PaginatedGuiBuilder copy = new PaginatedGuiBuilder();
        copy.lines = this.lines;
        copy.title = this.title;
        copy.itemSlots = this.itemSlots;
        copy.nextPageSlot = this.nextPageSlot;
        copy.previousPageSlot = this.previousPageSlot;
        copy.scheme = this.scheme.copy();
        copy.nextPageItem = this.nextPageItem;
        copy.previousPageItem = this.previousPageItem;
        return copy;
    }

    public PaginatedGuiBuilder lines(int lines) {
        this.lines = lines;
        return this;
    }

    public PaginatedGuiBuilder title(String title) {
        this.title = title;
        return this;
    }

    public PaginatedGuiBuilder itemSlots(List<Integer> itemSlots) {
        this.itemSlots = ImmutableList.copyOf(itemSlots);
        return this;
    }

    public PaginatedGuiBuilder nextPageSlot(int nextPageSlot) {
        this.nextPageSlot = nextPageSlot;
        return this;
    }

    public PaginatedGuiBuilder previousPageSlot(int previousPageSlot) {
        this.previousPageSlot = previousPageSlot;
        return this;
    }

    public PaginatedGuiBuilder scheme(MenuScheme scheme) {
        this.scheme = Objects.requireNonNull(scheme, "scheme");
        return this;
    }

    public PaginatedGuiBuilder nextPageItem(Function<PageInfo, ItemStack> nextPageItem) {
        this.nextPageItem = Objects.requireNonNull(nextPageItem, "nextPageItem");
        return this;
    }

    public PaginatedGuiBuilder previousPageItem(Function<PageInfo, ItemStack> previousPageItem) {
        this.previousPageItem = Objects.requireNonNull(previousPageItem, "previousPageItem");
        return this;
    }

    public int getLines() {
        return this.lines;
    }

    public String getTitle() {
        return this.title;
    }

    public List<Integer> getItemSlots() {
        return this.itemSlots;
    }

    public int getNextPageSlot() {
        return this.nextPageSlot;
    }

    public int getPreviousPageSlot() {
        return this.previousPageSlot;
    }

    public MenuScheme getScheme() {
        return this.scheme;
    }

    public Function<PageInfo, ItemStack> getNextPageItem() {
        return this.nextPageItem;
    }

    public Function<PageInfo, ItemStack> getPreviousPageItem() {
        return this.previousPageItem;
    }

    public PaginatedGui build(Player player, Function<PaginatedGui, List<Item>> content) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(content, "content");
        Objects.requireNonNull(this.lines, "lines");
        Objects.requireNonNull(this.title, "title");
        Objects.requireNonNull(this.itemSlots, "itemSlots");
        Objects.requireNonNull(this.nextPageSlot, "nextPageSlot");
        Objects.requireNonNull(this.previousPageSlot, "previousPageSlot");
        Objects.requireNonNull(this.scheme, "scheme");
        Objects.requireNonNull(this.nextPageItem, "nextPageItem");
        Objects.requireNonNull(this.previousPageItem, "previousPageItem");

        return new PaginatedGui(content, player, this);
    }
}
