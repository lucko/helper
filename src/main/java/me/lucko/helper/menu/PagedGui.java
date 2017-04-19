
package me.lucko.helper.menu;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apache.commons.lang.IncompleteArgumentException;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.PagedGui.PagedItemable;

/**
 * A {@link Gui} consisting of pages of {@link PagedItemable}s.
 * <p>
 * Created on Apr 12, 2017.
 * @param T The type of {@link PagedItemable} displayed.
 * @author FakeNeth
 */
public class PagedGui<T extends PagedItemable> extends Gui {

	private final ImmutableList<Integer> itemableSlots;
	private ImmutableList<T> itemables;

	private final Function<PagedGui<T>, Item> emptySlotItem;

	private int totalPages;
	private int currentPage = 1;
	private int defaultPage = currentPage;

	/**
	 * @param player The {@link Player} this {@link PagedGui} is for.
	 * @param factory The {@link Factory} containing all properties of this {@link PagedGui}.
	 */
	protected PagedGui(final Player player, final Factory<?, T> factory) {
		this(player, factory.getLines(), factory.getTitle(player), factory.getDefaultPage(), factory.getItemSlots(), factory.getItemables(player),
				factory.getEmptySlotItem());
	}

	private PagedGui(final Player player, final int lines, final String title, final int defaultPage, final Collection<Integer> itemableSlots,
			final Collection<T> itemables, final Function<PagedGui<T>, Item> emptySlotItem) {
		super(player, lines, title);
		this.itemableSlots = ImmutableList.copyOf(itemableSlots);
		this.itemables = ImmutableList.copyOf(itemables);
		this.emptySlotItem = emptySlotItem;
		this.defaultPage = defaultPage;
	}

	public ImmutableList<T> getItemables() {
		return itemables;
	}

	/**
	 * Sets the {@link Collection} of {@link PagedItemable}s used. Forces an update of the current page.
	 * @param itemables The {@link Collection} of {@link PagedItemable}s to set.
	 */
	public void setItemables(final Collection<T> itemables) {
		this.itemables = ImmutableList.copyOf(itemables);
		updateTotalPages();
		if (currentPage > totalPages) {
			currentPage = totalPages;
		}
		setPage(currentPage);
	}

	/**
	 * Sets the default page of this {@link PagedGui}.
	 * @param defaultPage The default page.
	 */
	public void setDefaultPage(final int defaultPage) {
		this.defaultPage = defaultPage;
	}

	/**
	 * @return The default page.
	 */
	public int getDefaultPage() {
		return defaultPage;
	}

	/**
	 * @return The current page.
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @return The amount of pages of {@link PagedItemable}s.
	 */
	public int getTotalPages() {
		return totalPages;
	}

	/**
	 * Forces an update of the total pages variable internally stored.
	 */
	public void updateTotalPages() {
		final int raw = (int) Math.ceil((double) itemables.size() / (double) itemableSlots.size());
		totalPages = Math.max(1, raw);
	}

	@Override
	public void open() {
		currentPage = defaultPage < 1 ? 1 : defaultPage > totalPages ? totalPages : defaultPage;
		super.open();
	}

	@Override
	public final void redraw() {
		setPage(currentPage);
		onRedraw();
	}
	
	/**
	 * @return If there is a previous paged relative to the current page.
	 */
	public boolean hasPreviousPage() {
		return currentPage > 1;
	}

	/**
	 * @return If there is a next page relative to the current page.
	 */
	public boolean hasNextPage() {
		return currentPage < totalPages;
	}


	/**
	 * Called at the end of {@link #redraw()} after the current page has been updated.
	 */
	protected void onRedraw() {}

	/**
	 * Sets the current page of this {@link PagedGui}.
	 * @param page The page to be set.
	 * @throws IllegalArgumentException If the specified page <1 || >{@link #getTotalPages()}.
	 */
	public final void setPage(final int page) throws IllegalArgumentException {

		final int totalPages = getTotalPages();

		if (page < 1 || page > totalPages) throw new IllegalArgumentException("The specified page must be >0 && <=totalPages{" + totalPages + "}");

		currentPage = page;

		final int totalItems = itemables.size();
		final int startIndex = (page - 1) * totalItems;
		for (int slotIndex = 0; slotIndex < itemableSlots.size(); slotIndex++) {
			final int itemIndex = startIndex + slotIndex;
			setItem(itemableSlots.get(slotIndex), itemIndex < totalItems ? itemables.get(itemIndex).getItem(this, itemIndex) : emptySlotItem.apply(
					this));
		}
		
		onPageUpdate();
	}

	/**
	 * Called when the current page is updated. Also called everytime {@link #onRedraw()}/{@link #redraw()} is called.
	 */
	protected void onPageUpdate() {}

	/**
	 * Gets a {@link Runnable} that when applied to an {@link Item} would result in the page incrementing.
	 * @param onAccept The {@link Runnable} called when a click shows the next page.
	 * @param onDeny The {@link Runnable} caled when a click does not show the next page, most likely due to the current page already being the final page.
	 * @return The {@link Runnable}.
	 */
	public Runnable newIncrementer(final Runnable onAccept, final Runnable onDeny) {
		return () -> {
			if (currentPage >= totalPages) {
				onDeny.run();
			}
			else {
				setPage(currentPage + 1);
				onAccept.run();
			}
		};
	}

	/**
	 * Gets a {@link Runnable} that when applied to an {@link Item} would result in the page decrementing.
	 * @param onAccept The {@link Runnable} called when a click shows the previous page.
	 * @param onDeny The {@link Runnable} caled when a click does not show the previous page, most likely due to the current page already being the first page.
	 * @return The {@link Runnable}.
	 */
	public Runnable newDecrementer(final Runnable onAccept, final Runnable onDeny) {
		return () -> {
			if (currentPage <= 1) {
				onDeny.run();
			}
			else {
				setPage(currentPage - 1);
				onAccept.run();
			}
		};
	}

	/**
	 * An {@link Object} that can be represented as an {@link Item} in a {@link PagedGui}.
	 * <p>
	 * Created on Apr 12, 2017.
	 * @author FakeNeth
	 */
	public interface PagedItemable {

		/**
		 * @param gui The {@link PagedGui} the {@link Item} will be displayed in.
		 * @param index The index this {@link PagedItemable} is in the {@link List} used by the {@link PagedGui}.
		 * @return The {@link Item}.
		 */
		Item getItem(final PagedGui<?> gui, final int index);

	}

	/**
	 * A factory for {@link PagedItemable}s.
	 * <p>
	 * Created on Apr 12, 2017.
	 * @param <T> The type of {@link PagedGui} being produced by this {@link Factory}.
	 * @param <I> The type of {@link PagedItemable} to be used by {@link PagedGui}s constructed by this {@link Factory}.
	 * @author FakeNeth
	 */
	public static class Factory<T extends PagedGui<I>, I extends PagedItemable> {

		private Function<Player, String> title = (player) -> "";
		private int lines = 3;
		private final List<Integer> itemableSlots = new LinkedList<>();
		private int defaultPage = 1;
		private Function<Player, Collection<I>> itemables = (player) -> Lists.newArrayList();
		private Function<PagedGui<I>, Item> emptySlotItem = (gui) -> ItemStackBuilder.of(Material.AIR).build((Runnable) null);

		private BiFunction<Player, Factory<T, I>, T> constructor;
		private BiFunction<Player, Factory<T, I>, PagedGui<I>> constructorRaw = (player, factory) -> new PagedGui<I>(player, factory);

		public static <I extends PagedItemable> Factory<PagedGui<I>, I> getNew() {
			return new Factory<>();
		}

		public static <T extends PagedGui<I>, I extends PagedItemable> Factory<T, I> getNew(final BiFunction<Player, Factory<T, I>, T> constructor) {
			return new Factory<T, I>().setConstructor(constructor);
		}

		private Factory() {}

		public Function<PagedGui<I>, Item> getEmptySlotItem() {
			return emptySlotItem;
		}

		/**
		 * Sets the {@link Function} used to retrieve {@link Item}s that will fill {@link PagedItemable} slots of which there arent enough
		 * {@link PagedItemable}s to fill.
		 * @param emptySlotItem The {@link Function}.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> withEmptySlotItem(final Function<PagedGui<I>, Item> emptySlotItem) {
			this.emptySlotItem = emptySlotItem;
			return this;
		}

		public int getLines() {
			return lines;
		}

		/**
		 * Sets the amount of lines {@link PagedGui}s constructed by this {@link Factory} will have.
		 * @param lines The line amount.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> withLines(final int lines) {
			this.lines = lines;
			return this;
		}

		public String getTitle(final Player player) {
			return title.apply(player);
		}

		public Function<Player, String> getTitle() {
			return title;
		}

		/**
		 * Sets the title {@link PagedGui}s constructed by this {@link Factory} will have.
		 * @param title The title.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> withTitle(final String title) {
			this.title = (player) -> title;
			return this;
		}

		/**
		 * Sets the title {@link PagedGui}s constructed by this {@link Factory} will have.
		 * @param title The {@link Supplier} used to retrieve titles.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> withTitle(final Supplier<String> title) {
			this.title = (player) -> title.get();
			return this;
		}

		/**
		 * Sets the title {@link PagedGui}s constructed by this {@link Factory} will have.
		 * @param title The {@link Function} used to retrieve titles for specified {@link Player}s.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> withTitle(final Function<Player, String> title) {
			this.title = title;
			return this;
		}

		/**
		 * Sets the default page {@link PagedGui}s constructed by this {@link Factory} will have.
		 * @param defaultPage The default page. If it is larger than the total amount of pages, the final page will be the default instead.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> withDefaultPage(final int defaultPage) {
			this.defaultPage = defaultPage;
			return this;
		}

		public int getDefaultPage() {
			return defaultPage;
		}

		/**
		 * Adds the specified slots to the slots {@link PagedItemable}s are displayed in.
		 * @param slots The slots to add.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> addItemSlots(final int... slots) {
			for (final int slot : slots) {
				itemableSlots.add(slot);
			}
			return this;
		}

		/**
		 * Adds the specified slots to the slots {@link PagedItemable}s are displayed in.
		 * @param slots The slots to add.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> addItemSlots(final Collection<Integer> slots) {
			itemableSlots.addAll(slots);
			return this;
		}

		/**
		 * Adds the specified range slots to the slots {@link PagedItemable}s are displayed in.
		 * @param The floor of the range, inclusive.
		 * @param The ceiling of the range, inclusive.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> addItemSlotsRange(final int floor, final int ceil) {
			setItemSlots(IntStream.range(floor, ceil + 1).toArray());
			return this;
		}

		/**
		 * Sets the specified slots {@link PagedItemable}s are displayed in.
		 * @param slots The slots to set.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> setItemSlots(final int... slots) {
			itemableSlots.clear();
			addItemSlots(slots);
			return this;
		}

		/**
		 * Sets the specified slots {@link PagedItemable}s are displayed in.
		 * @param slots The slots to set.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> setItemSlots(final Collection<Integer> slots) {
			itemableSlots.clear();
			addItemSlots(slots);
			return this;
		}

		/**
		 * Sets the range of specified slots {@link PagedItemable}s are displayed in.
		 * @param The floor of the range, inclusive.
		 * @param The ceiling of the range, inclusive.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> setItemSlotsRange(final int floor, final int ceil) {
			itemableSlots.clear();
			addItemSlotsRange(floor, ceil);
			return this;
		}

		public List<Integer> getItemSlots() {
			return itemableSlots;
		}

		/**
		 * Sets the {@link Collection} of {@link PagedItemable}s to be displayed.
		 * @param itemables The {@link Collection}.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> setItemables(final Collection<I> itemables) {
			this.itemables = (player) -> itemables;
			return this;
		}

		/**
		 * Sets the {@link Collection} of {@link PagedItemable}s to be displayed.
		 * @param itemables A {@link Supplier} supplying the {@link Collection}.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> setItemables(final Supplier<Collection<I>> itemables) {
			this.itemables = (player) -> itemables.get();
			return this;
		}

		/**
		 * Sets the {@link Collection} of {@link PagedItemable}s to be displayed.
		 * @param itemables A {@link Function} used to get a player specific {@link Collection}.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> setItemables(final Function<Player, Collection<I>> itemables) {
			this.itemables = itemables;
			return this;
		}

		public Collection<I> getItemables(final Player player) {
			return itemables.apply(player);
		}

		public Function<Player, Collection<I>> getItemables() {
			return itemables;
		}

		/**
		 * Sets the {@link BiFunction} used to construct {@link PagedGui} instances.
		 * @param constructor The {@link BiFunction}.
		 * @return This {@link Factory} instance.
		 */
		public Factory<T, I> setConstructor(final BiFunction<Player, Factory<T, I>, T> constructor) {
			this.constructor = constructor;
			this.constructorRaw = (BiFunction<Player, Factory<T, I>, PagedGui<I>>) constructor;
			return this;
		}

		public BiFunction<Player, Factory<T, I>, T> getConstructor() {
			return this.constructor;
		}

		public BiFunction<Player, Factory<T, I>, PagedGui<I>> getRawConstructor() {
			return this.constructorRaw;
		}

		/**
		 * Constructs a new {@link PagedGui} for the specified {@link Player}.
		 * @param The {@link Player} the constructed {@link PagedGui} is for.
		 * @return The {@link PagedGui}.
		 * @throws IncompleteArgumentException If a required property has not been set.
		 */
		public T build(final Player player) throws IncompleteArgumentException {
			return constructor.apply(player, this);
		}

		/**
		 * Constructs a new {@link PagedGui} for the specified {@link Player}.
		 * @param The {@link Player} the constructed {@link PagedGui} is for.
		 * @return The {@link PagedGui}.
		 * @throws IncompleteArgumentException If a required property has not been set.
		 */
		public PagedGui<I> buildRaw(final Player player) throws IncompleteArgumentException {
			return constructorRaw.apply(player, this);
		}

	}

}
