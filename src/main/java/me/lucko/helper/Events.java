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

package me.lucko.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A utility class to help with event listening.
 */
public final class Events {
    public static final DefaultFilters DEFAULT_FILTERS = new DefaultFiltersImpl();
    private static Plugin plugin;

    /**
     * Makes a HandlerBuilder for a given event
     *
     * @param eventClass the class of the event
     * @param <T> the event type
     * @return a {@link HandlerBuilder} to construct the event handler
     * @throws NullPointerException if eventClass is null
     */
    public static <T extends Event> HandlerBuilder<T> subscribe(Class<T> eventClass) {
        return subscribe(eventClass, EventPriority.NORMAL);
    }

    /**
     * Makes a HandlerBuilder for a given event
     *
     * @param eventClass the class of the event
     * @param priority the priority to listen at
     * @param <T> the event type
     * @return a {@link HandlerBuilder} to construct the event handler
     * @throws NullPointerException if eventClass or priority is null
     */
    public static <T extends Event> HandlerBuilder<T> subscribe(Class<T> eventClass, EventPriority priority) {
        Preconditions.checkNotNull(eventClass, "eventClass");
        Preconditions.checkNotNull(priority, "priority");
        return new HandlerBuilderImpl<>(eventClass, priority);
    }

    private static Plugin getPlugin() {
        if (plugin == null) {
            plugin = JavaPlugin.getProvidingPlugin(Events.class);
        }
        return plugin;
    }

    /**
     * Responsible for the handling of a given event
     * @param <T> the event type
     */
    public interface Handler<T> {

        /**
         * Gets the class the handler is handling
         *
         * @return the class the handler is handling.
         */
        Class<T> getEventClass();

        /**
         * Gets whether the handler is active
         *
         * @return if the handler is active
         */
        boolean isActive();

        /**
         * Gets the number of times the handler has been called
         *
         * @return the number of times the handler has been called
         */
        long getCallCounter();

        /**
         * Gets the time in milliseconds when this handler will expire, if any
         *
         * @return the time in milliseconds when this handler will expire, if any
         */
        OptionalLong getExpiryTimeMillis();

        /**
         * Unregisters the handler
         *
         * @return true if the handler wasn't already unregistered
         */
        boolean unregister();

    }

    /**
     * Builds a {@link Handler}
     *
     * @param <T> the event type
     */
    public interface HandlerBuilder<T extends Event> {

        /**
         * Sets the expiry time on the handler
         *
         * @param duration the duration until expiry
         * @param unit the unit for the duration
         * @return the builder instance
         * @throws IllegalArgumentException if duration is not >= 1
         */
        HandlerBuilder<T> expireAfter(long duration, TimeUnit unit);

        /**
         * Sets the number of calls until the handler will automatically be unregistered
         *
         * <p>The call counter is only incremented if the event call passes all filters and if the handler completes
         * without throwing an exception.
         *
         * @param maxCalls the number of times the handler will be called until being unregistered.
         * @return the builder instance
         * @throws IllegalArgumentException if maxCalls is not >= 1
         */
        HandlerBuilder<T> maxCalls(long maxCalls);

        /**
         * Sets the handler to be called asynchronously.
         *
         * <p>This only applies to the handler. All filters will be evaluated on the thread on which the event was
         * called.
         *
         * @return the builder instance
         */
        HandlerBuilder<T> handleAsync();

        /**
         * Sets the exception consumer for the handler.
         *
         * <p> If an exception is thrown in the handler, it is passed to this consumer to be swallowed.
         *
         * @param consumer the consumer
         * @return the builder instance
         * @throws NullPointerException if the consumer is null
         */
        HandlerBuilder<T> exceptionConsumer(Consumer<Throwable> consumer);

        /**
         * Adds a filter to the handler.
         *
         * <p>An event will only be handled if it passes all filters. Filters are evaluated in the order they are
         * registered.
         *
         * @param predicate the filter
         * @return the builder instance
         */
        HandlerBuilder<T> filter(Predicate<T> predicate);

        /**
         * Builds and registers the Handler.
         *
         * @param handler the consumer responsible for handling the event.
         * @return a registered {@link Handler} instance.
         * @throws NullPointerException if the handler is null
         */
        default Handler<T> handler(Consumer<? super T> handler) {
            return handler((h, t) -> handler.accept(t));
        }

        /**
         * Builds and registers the Handler.
         *
         * @param handler the bi-consumer responsible for handling the event.
         * @return a registered {@link Handler} instance.
         * @throws NullPointerException if the handler is null
         */
        Handler<T> handler(BiConsumer<Handler<T>, ? super T> handler);

    }

    /**
     * Provides a set of useful default filters for passing to {@link HandlerBuilder#filter(Predicate)}.
     */
    public interface DefaultFilters {

        /**
         * Returns a predicate which only returns true if the event isn't cancelled
         *
         * @param <T> the event type
         * @return a predicate which only returns true if the event isn't cancelled
         */
        <T extends Cancellable> Predicate<T> ignoreCancelled();

        /**
         * Returns a predicate which only returns true if the player has moved over a block.
         *
         * @param <T> the event type
         * @return a predicate which only returns true if the player has moved over a block.
         */
        <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlock();

        /**
         * Returns a predicate which only returns true if the player has moved over a chunk border.
         *
         * @param <T> the event type
         * @return a predicate which only returns true if the player has moved over a chunk border.
         */
        <T extends PlayerMoveEvent> Predicate<T> ignoreSameChunk();

    }

    private static class HandlerImpl<T extends Event> implements Handler<T> {
        private final Class<T> eventClass;
        private final EventPriority priority;

        private final long expiry;
        private final long maxCalls;
        private final boolean handleAsync;
        private final Consumer<Throwable> exceptionConsumer;
        private final List<Predicate<T>> filters;
        private final BiConsumer<Handler<T>, ? super T> handler;

        private final Listener listener = new Listener() {};
        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicBoolean active = new AtomicBoolean(true);

        private HandlerImpl(HandlerBuilderImpl<T> builder, BiConsumer<Handler<T>, ? super T> handler) {
            this.eventClass = builder.eventClass;
            this.priority = builder.priority;
            this.expiry = builder.expiry;
            this.maxCalls = builder.maxCalls;
            this.handleAsync = builder.handleAsync;
            this.exceptionConsumer = builder.exceptionConsumer;
            this.filters = ImmutableList.copyOf(builder.filters);
            this.handler = handler;
        }

        private void register(Plugin plugin) {
            plugin.getServer().getPluginManager().registerEvent(eventClass, listener, priority, (l, event) -> {
                if (event.getClass() != eventClass) {
                    return;
                }

                // This handler is disabled, so unregister from the event.
                if (!active.get()) {
                    event.getHandlers().unregister(listener);
                    return;
                }

                // Check if the handler has expired.
                if (expiry != -1) {
                    long now = System.currentTimeMillis();
                    if (now > expiry) {
                        event.getHandlers().unregister(listener);
                        unregister();
                        return;
                    }
                }

                // Check if the handler has reached its max calls
                if (maxCalls != -1) {
                    if (callCount.get() >= maxCalls) {
                        event.getHandlers().unregister(listener);
                        unregister();
                        return;
                    }
                }

                T eventInstance = eventClass.cast(event);

                for (Predicate<T> filter : filters) {
                    if (!filter.test(eventInstance)) {
                        return;
                    }
                }

                // Actually call the handler
                if (handleAsync) {
                    Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), () -> handle(eventInstance));
                } else {
                    handle(eventInstance);
                }
            }, plugin, false);
        }

        private void handle(T e) {
            if (exceptionConsumer == null) {
                handler.accept(this, e);
                callCount.incrementAndGet();
            } else {
                try {
                    handler.accept(this, e);
                    callCount.incrementAndGet();
                } catch (Throwable t) {
                    exceptionConsumer.accept(t);
                }
            }
        }

        @Override
        public Class<T> getEventClass() {
            return eventClass;
        }

        @Override
        public boolean isActive() {
            return active.get();
        }

        @Override
        public long getCallCounter() {
            return callCount.get();
        }

        @Override
        public OptionalLong getExpiryTimeMillis() {
            return expiry == -1 ? OptionalLong.empty() : OptionalLong.of(expiry);
        }

        @Override
        public boolean unregister() {
            return active.getAndSet(false);
        }
    }

    private static class HandlerBuilderImpl<T extends Event> implements HandlerBuilder<T> {
        private final Class<T> eventClass;
        private final EventPriority priority;

        private long expiry = -1;
        private long maxCalls = -1;
        private boolean handleAsync = false;
        private Consumer<Throwable> exceptionConsumer = null;
        private List<Predicate<T>> filters = new ArrayList<>();

        private HandlerBuilderImpl(Class<T> eventClass, EventPriority priority) {
            this.eventClass = eventClass;
            this.priority = priority;
        }

        @Override
        public HandlerBuilder<T> expireAfter(long duration, TimeUnit unit) {
            Preconditions.checkNotNull(unit, "unit");
            Preconditions.checkArgument(duration >= 1, "duration >= 1");
            this.expiry = Math.addExact(System.currentTimeMillis(), unit.toMillis(duration));
            return this;
        }

        @Override
        public HandlerBuilder<T> maxCalls(long maxCalls) {
            Preconditions.checkArgument(maxCalls >= 1, "maxCalls >= 1");
            this.maxCalls = maxCalls;
            return this;
        }

        @Override
        public HandlerBuilder<T> handleAsync() {
            this.handleAsync = true;
            return this;
        }

        @Override
        public HandlerBuilder<T> exceptionConsumer(Consumer<Throwable> exceptionConsumer) {
            Preconditions.checkNotNull(exceptionConsumer, "exceptionConsumer");
            this.exceptionConsumer = exceptionConsumer;
            return this;
        }

        @Override
        public HandlerBuilder<T> filter(Predicate<T> predicate) {
            Preconditions.checkNotNull(predicate, "predicate");
            this.filters.add(predicate);
            return this;
        }

        @Override
        public Handler<T> handler(BiConsumer<Handler<T>, ? super T> handler) {
            Preconditions.checkNotNull(handler, "handler");

            HandlerImpl<T> impl = new HandlerImpl<>(this, handler);
            impl.register(getPlugin());
            return impl;
        }
    }

    private static class DefaultFiltersImpl implements DefaultFilters {

        @Override
        public <T extends Cancellable> Predicate<T> ignoreCancelled() {
            return e -> !e.isCancelled();
        }

        @Override
        public <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlock() {
            return e -> !e.getFrom().getBlock().getLocation().equals(e.getTo().getBlock().getLocation());
        }

        @Override
        public <T extends PlayerMoveEvent> Predicate<T> ignoreSameChunk() {
            return e -> !e.getFrom().getChunk().equals(e.getTo().getChunk());
        }
    }

    private Events() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
