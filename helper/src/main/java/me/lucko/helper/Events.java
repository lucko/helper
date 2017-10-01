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

package me.lucko.helper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.metadata.MetadataKey;
import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.timings.Timings;
import me.lucko.helper.utils.Cooldown;
import me.lucko.helper.utils.CooldownCollection;
import me.lucko.helper.utils.Delegates;
import me.lucko.helper.utils.LoaderUtils;
import me.lucko.helper.utils.Log;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import co.aikar.timings.lib.MCTiming;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A functional event listening utility.
 */
@NonnullByDefault
public final class Events {
    private static final BiConsumer<Event, Throwable> DEFAULT_EXCEPTION_CONSUMER = (event, throwable) -> {
        Log.severe("[EVENTS] Exception thrown whilst handling event: " + event.getClass().getName());
        throwable.printStackTrace();
    };

    public static final DefaultFilters DEFAULT_FILTERS = new DefaultFiltersImpl();

    /**
     * Makes a HandlerBuilder for a given event
     *
     * @param eventClass the class of the event
     * @param <T>        the event type
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
     * @param priority   the priority to listen at
     * @param <T>        the event type
     * @return a {@link HandlerBuilder} to construct the event handler
     * @throws NullPointerException if eventClass or priority is null
     */
    public static <T extends Event> HandlerBuilder<T> subscribe(Class<T> eventClass, EventPriority priority) {
        Preconditions.checkNotNull(eventClass, "eventClass");
        Preconditions.checkNotNull(priority, "priority");
        return new HandlerBuilderImpl<>(eventClass, priority);
    }

    /**
     * Makes a MergedHandlerBuilder for a given super type
     *
     * @param handledClass the super type of the event handler
     * @param <T>          the super type class
     * @return a {@link MergedHandlerBuilder} to construct the event handler
     */
    public static <T> MergedHandlerBuilder<T> merge(Class<T> handledClass) {
        Preconditions.checkNotNull(handledClass, "handledClass");
        return new MergedHandlerBuilderImpl<>(TypeToken.of(handledClass));
    }

    /**
     * Makes a MergedHandlerBuilder for a given super type
     *
     * @param type the super type of the event handler
     * @param <T>  the super type class
     * @return a {@link MergedHandlerBuilder} to construct the event handler
     */
    public static <T> MergedHandlerBuilder<T> merge(TypeToken<T> type) {
        Preconditions.checkNotNull(type, "type");
        return new MergedHandlerBuilderImpl<>(type);
    }

    /**
     * Makes a MergedHandlerBuilder for a super event class
     *
     * @param superClass   the abstract super event class
     * @param eventClasses the event classes to be bound to
     * @param <S>          the super class type
     * @return a {@link MergedHandlerBuilder} to construct the event handler
     */
    @SafeVarargs
    public static <S extends Event> MergedHandlerBuilder<S> merge(Class<S> superClass, Class<? extends S>... eventClasses) {
        return merge(superClass, EventPriority.NORMAL, eventClasses);
    }

    /**
     * Makes a MergedHandlerBuilder for a super event class
     *
     * @param superClass   the abstract super event class
     * @param priority     the priority to listen at
     * @param eventClasses the event classes to be bound to
     * @param <S>          the super class type
     * @return a {@link MergedHandlerBuilder} to construct the event handler
     */
    @SafeVarargs
    public static <S extends Event> MergedHandlerBuilder<S> merge(Class<S> superClass, EventPriority priority, Class<? extends S>... eventClasses) {
        Preconditions.checkNotNull(superClass, "superClass");
        Preconditions.checkNotNull(eventClasses, "eventClasses");
        Preconditions.checkNotNull(priority, "priority");
        if (eventClasses.length < 2) {
            throw new IllegalArgumentException("merge method used for only one subclass");
        }

        MergedHandlerBuilderImpl<S> h = new MergedHandlerBuilderImpl<>(TypeToken.of(superClass));
        for (Class<? extends S> clazz : eventClasses) {
            h.bindEvent(clazz, priority, e -> e);
        }
        return h;
    }

    /**
     * Submit the event on the current thread
     *
     * @param event the event to call
     */
    public static void call(Event event) {
        Helper.plugins().callEvent(event);
    }

    /**
     * Submit the event on a new async thread.
     *
     * @param event the event to call
     */
    public static void callAsync(Event event) {
        Scheduler.runAsync(() -> call(event));
    }

    /**
     * Submit the event on the main server thread.
     *
     * @param event the event to call
     */
    public static void callSync(Event event) {
        Scheduler.runSync(() -> call(event));
    }

    /**
     * Responsible for the handling of a given event
     *
     * @param <T> the event type
     */
    @NonnullByDefault
    public interface Handler<T> extends Terminable {

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

        @Override
        default boolean terminate() {
            return unregister();
        }
    }

    /**
     * Responsible for the handling of a merged event
     *
     * @param <T> the event type
     */
    @NonnullByDefault
    public interface MergedHandler<T> extends Terminable {

        /**
         * Gets the handled class
         *
         * @return the handled class
         */
        Class<? super T> getHandledClass();

        /**
         * Gets a set of the individual event classes being listened to
         *
         * @return the individual classes
         */
        Set<Class<? extends Event>> getEventClasses();

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

        @Override
        default boolean terminate() {
            return unregister();
        }
    }

    /**
     * Builds a {@link Handler}
     *
     * @param <T> the event type
     */
    @NonnullByDefault
    public interface HandlerBuilder<T extends Event> {

        /**
         * Sets the expiry time on the handler
         *
         * @param duration the duration until expiry
         * @param unit     the unit for the duration
         * @return the builder instance
         * @throws IllegalArgumentException if duration is not greater than or equal to 1
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
         * @throws IllegalArgumentException if maxCalls is not greater than or equal to 1
         */
        HandlerBuilder<T> expireAfter(long maxCalls);

        /**
         * Sets the exception consumer for the handler.
         *
         * <p> If an exception is thrown in the handler, it is passed to this consumer to be swallowed.
         *
         * @param consumer the consumer
         * @return the builder instance
         * @throws NullPointerException if the consumer is null
         */
        HandlerBuilder<T> exceptionConsumer(BiConsumer<? super T, Throwable> consumer);

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
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @return the builder instance
         */
        HandlerBuilder<T> withCooldown(Cooldown cooldown);

        /**
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @param cooldownFailConsumer a consumer to be called when the cooldown fails.
         * @return the builder instance
         */
        HandlerBuilder<T> withCooldown(Cooldown cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer);

        /**
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @return the builder instance
         */
        HandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown);

        /**
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @param cooldownFailConsumer a consumer to be called when the cooldown fails.
         * @return the builder instance
         */
        HandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer);

        /**
         * Builds and registers the Handler.
         *
         * @param handler the consumer responsible for handling the event.
         * @return a registered {@link Handler} instance.
         * @throws NullPointerException if the handler is null
         */
        default Handler<T> handler(Consumer<? super T> handler) {
            return handler(Delegates.consumerToBiConsumerSecond(handler));
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
     * Builds a {@link MergedHandler}
     *
     * @param <T> the super event type
     */
    @NonnullByDefault
    public interface MergedHandlerBuilder<T> {

        /**
         * Binds this handler to an event
         *
         * @param eventClass the event class to bind to
         * @param function   the function to remap the event
         * @param <E>        the event class
         * @return the builder instance
         */
        <E extends Event> MergedHandlerBuilder<T> bindEvent(Class<E> eventClass, Function<E, T> function);

        /**
         * Binds this handler to an event
         *
         * @param eventClass the event class to bind to
         * @param priority   the priority to listen at
         * @param function   the function to remap the event
         * @param <E>        the event class
         * @return the builder instance
         */
        <E extends Event> MergedHandlerBuilder<T> bindEvent(Class<E> eventClass, EventPriority priority, Function<E, T> function);

        /**
         * Sets the expiry time on the handler
         *
         * @param duration the duration until expiry
         * @param unit     the unit for the duration
         * @return the builder instance
         * @throws IllegalArgumentException if duration is not greater than or equal to 1
         */
        MergedHandlerBuilder<T> expireAfter(long duration, TimeUnit unit);

        /**
         * Sets the number of calls until the handler will automatically be unregistered
         *
         * <p>The call counter is only incremented if the event call passes all filters and if the handler completes
         * without throwing an exception.
         *
         * @param maxCalls the number of times the handler will be called until being unregistered.
         * @return the builder instance
         * @throws IllegalArgumentException if maxCalls is not greater than or equal to 1
         */
        MergedHandlerBuilder<T> expireAfter(long maxCalls);

        /**
         * Sets the exception consumer for the handler.
         *
         * <p> If an exception is thrown in the handler, it is passed to this consumer to be swallowed.
         *
         * @param consumer the consumer
         * @return the builder instance
         * @throws NullPointerException if the consumer is null
         */
        MergedHandlerBuilder<T> exceptionConsumer(BiConsumer<Event, Throwable> consumer);

        /**
         * Adds a filter to the handler.
         *
         * <p>An event will only be handled if it passes all filters. Filters are evaluated in the order they are
         * registered.
         *
         * @param predicate the filter
         * @return the builder instance
         */
        MergedHandlerBuilder<T> filter(Predicate<T> predicate);

        /**
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @return the builder instance
         */
        MergedHandlerBuilder<T> withCooldown(Cooldown cooldown);

        /**
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @param cooldownFailConsumer a consumer to be called when the cooldown fails.
         * @return the builder instance
         */
        MergedHandlerBuilder<T> withCooldown(Cooldown cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer);

        /**
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @return the builder instance
         */
        MergedHandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown);

        /**
         * Adds a filter to the handler, only allowing it to pass if {@link Cooldown#test()} returns true.
         *
         * @param cooldown the cooldown
         * @param cooldownFailConsumer a consumer to be called when the cooldown fails.
         * @return the builder instance
         */
        MergedHandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer);

        /**
         * Builds and registers the Handler.
         *
         * @param handler the consumer responsible for handling the event.
         * @return a registered {@link Handler} instance.
         * @throws NullPointerException  if the handler is null
         * @throws IllegalStateException if no events have been bound to
         */
        default MergedHandler<T> handler(Consumer<? super T> handler) {
            return handler(Delegates.consumerToBiConsumerSecond(handler));
        }

        /**
         * Builds and registers the Handler.
         *
         * @param handler the bi-consumer responsible for handling the event.
         * @return a registered {@link Handler} instance.
         * @throws NullPointerException  if the handler is null
         * @throws IllegalStateException if no events have been bound to
         */
        MergedHandler<T> handler(BiConsumer<MergedHandler<T>, ? super T> handler);

    }

    /**
     * Provides a set of useful default filters for passing to {@link HandlerBuilder#filter(Predicate)}.
     */
    @NonnullByDefault
    public interface DefaultFilters {

        /**
         * Returns a predicate which only returns true if the event isn't cancelled
         *
         * @param <T> the event type
         * @return a predicate which only returns true if the event isn't cancelled
         */
        <T extends Cancellable> Predicate<T> ignoreCancelled();

        /**
         * Returns a predicate which only returns true if the player has moved over a block
         *
         * @param <T> the event type
         * @return a predicate which only returns true if the player has moved over a block
         */
        <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlock();

        /**
         * Returns a predicate which only returns true if the player has moved over a block, not including movement
         * directly up and down. (so jumping wouldn't return true)
         *
         * @param <T> the event type
         * @return a predicate which only returns true if the player has moved across a block border
         */
        <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlockAndY();

        /**
         * Returns a predicate which only returns true if the player has moved over a chunk border
         *
         * @param <T> the event type
         * @return a predicate which only returns true if the player has moved over a chunk border
         */
        <T extends PlayerMoveEvent> Predicate<T> ignoreSameChunk();

        /**
         * Returns a predicate which only returns true if the entity has a given metadata key
         *
         * @param key the metadata key
         * @param <T> the event type
         * @return a predicate which only returns true if the entity has a given metadata key
         */
        <T extends EntityEvent> Predicate<T> entityHasMetadata(MetadataKey<?> key);

        /**
         * Returns a predicate which only returns true if the player has a given metadata key
         *
         * @param key the metadata key
         * @param <T> the event type
         * @return a predicate which only returns true if the player has a given metadata key
         */
        <T extends PlayerEvent> Predicate<T> playerHasMetadata(MetadataKey<?> key);

        /**
         * Returns a predicate which only returns true if the player has the given permission
         *
         * @param permission the permission
         * @param <T> the event type
         * @return a predicate which only returns true if the player has the given permission
         */
        <T extends PlayerEvent> Predicate<T> playerHasPermission(String permission);

    }

    private static class HelperEventHandler<T extends Event> implements Handler<T>, EventExecutor, Listener {
        private final Class<T> eventClass;
        private final EventPriority priority;

        private final long expiry;
        private final long maxCalls;
        private final BiConsumer<? super T, Throwable> exceptionConsumer;
        private final List<Predicate<T>> filters;
        private final BiConsumer<Handler<T>, ? super T> handler;
        private final MCTiming timing;

        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicBoolean active = new AtomicBoolean(true);

        private HelperEventHandler(HandlerBuilderImpl<T> builder, BiConsumer<Handler<T>, ? super T> handler) {
            this.eventClass = builder.eventClass;
            this.priority = builder.priority;
            this.expiry = builder.expiry;
            this.maxCalls = builder.maxCalls;
            this.exceptionConsumer = builder.exceptionConsumer;
            this.filters = ImmutableList.copyOf(builder.filters);
            this.handler = handler;
            this.timing = Timings.of("helper-events: " + Delegate.resolve(handler).getClass().getName());
        }

        private void register(Plugin plugin) {
            Helper.plugins().registerEvent(eventClass, this, priority, this, plugin, false);
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
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
                    active.set(false);
                    return;
                }
            }

            // Check if the handler has reached its max calls
            if (checkMaxCalls(listener, event.getHandlers())) {
                return;
            }

            T eventInstance = eventClass.cast(event);

            // Actually call the handler
            try {
                try (MCTiming t = timing.startTiming()) {
                    for (Predicate<T> filter : filters) {
                        if (!filter.test(eventInstance)) {
                            return;
                        }
                    }

                    handler.accept(this, eventInstance);
                }

                callCount.incrementAndGet();
            } catch (Throwable t) {
                exceptionConsumer.accept(eventInstance, t);
            }

            // has it expired now?
            checkMaxCalls(listener, event.getHandlers());
        }

        private boolean checkMaxCalls(Listener listener, HandlerList handlerList) {
            if (maxCalls != -1) {
                if (callCount.get() >= maxCalls) {
                    handlerList.unregister(listener);
                    active.set(false);
                    return true;
                }
            }
            return false;
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
        public boolean hasTerminated() {
            return !active.get();
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
            // already unregistered
            if (!active.getAndSet(false)) {
                return false;
            }

            // Also remove the handler directly, just in case the event has a really low throughput.
            // Unfortunately we can't cache this call, as the method is static
            try {
                Method getHandlerListMethod = eventClass.getMethod("getHandlerList");
                HandlerList handlerList = (HandlerList) getHandlerListMethod.invoke(null);
                handlerList.unregister(this);
            } catch (Throwable t) {
                // ignored
            }
            return true;
        }
    }

    private static class HelperMergedEventHandler<T> implements MergedHandler<T>, EventExecutor, Listener {
        private final TypeToken<T> handledClass;
        private final Map<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> mappings;

        private final long expiry;
        private final long maxCalls;
        private final BiConsumer<Event, Throwable> exceptionConsumer;
        private final List<Predicate<T>> filters;
        private final BiConsumer<MergedHandler<T>, ? super T> handler;
        private final MCTiming timing;

        private final AtomicLong callCount = new AtomicLong(0);
        private final AtomicBoolean active = new AtomicBoolean(true);

        private HelperMergedEventHandler(MergedHandlerBuilderImpl<T> builder, BiConsumer<MergedHandler<T>, ? super T> handler) {
            this.handledClass = builder.handledClass;
            this.mappings = ImmutableMap.copyOf(builder.mappings);
            this.expiry = builder.expiry;
            this.maxCalls = builder.maxCalls;
            this.exceptionConsumer = builder.exceptionConsumer;
            this.filters = ImmutableList.copyOf(builder.filters);
            this.handler = handler;
            this.timing = Timings.of("helper-events: " + Delegate.resolve(handler).getClass().getName());
        }

        private void register(Plugin plugin) {
            for (Map.Entry<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> ent : mappings.entrySet()) {
                Helper.plugins().registerEvent(ent.getKey(), this, ent.getValue().getPriority(), this, plugin, false);
            }
        }

        @Override
        public void execute(Listener listener, Event event) throws EventException {
            Function<Object, T> function = null;

            for (Map.Entry<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> ent : mappings.entrySet()) {
                if (event.getClass() == ent.getKey()) {
                    function = ent.getValue().getFunction();
                    break;
                }
            }

            if (function == null) {
                return;
            }

            // This handler is disabled, so unregister from the event.
            if (!active.get()) {
                event.getHandlers().unregister(listener);
                return;
            }

            // Check if the handler has expired.
            if (checkMaxCalls()) {
                return;
            }

            // Check if the handler has reached its max calls
            if (maxCalls != -1) {
                if (callCount.get() >= maxCalls) {
                    unregister();
                    return;
                }
            }

            T eventInstance = function.apply(event);

            // Actually call the handler
            try {
                try (MCTiming t = timing.startTiming()) {
                    for (Predicate<T> filter : filters) {
                        if (!filter.test(eventInstance)) {
                            return;
                        }
                    }

                    handler.accept(this, eventInstance);
                }

                callCount.incrementAndGet();
            } catch (Throwable t) {
                exceptionConsumer.accept(event, t);
            }

            // check if the call caused the method to expire.
            checkMaxCalls();
        }

        private boolean checkMaxCalls() {
            if (maxCalls != -1) {
                if (callCount.get() >= maxCalls) {
                    unregister();
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isActive() {
            return active.get();
        }

        @Override
        public boolean hasTerminated() {
            return !active.get();
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
            // already unregistered
            if (!active.getAndSet(false)) {
                return false;
            }

            // Also remove the handler directly, just in case the event has a really low throughput.
            // Unfortunately we can't cache this call, as the method is static
            for (Class<? extends Event> clazz : mappings.keySet()) {
                try {
                    Method getHandlerListMethod = clazz.getMethod("getHandlerList");
                    HandlerList handlerList = (HandlerList) getHandlerListMethod.invoke(null);
                    handlerList.unregister(this);
                } catch (Throwable t) {
                    // ignored
                }
            }
            return true;
        }

        @Override
        public Class<? super T> getHandledClass() {
            return handledClass.getRawType();
        }

        @Override
        public Set<Class<? extends Event>> getEventClasses() {
            return mappings.keySet();
        }
    }

    private static class HandlerBuilderImpl<T extends Event> implements HandlerBuilder<T> {
        private final Class<T> eventClass;
        private final EventPriority priority;

        private long expiry = -1;
        private long maxCalls = -1;
        private BiConsumer<? super T, Throwable> exceptionConsumer = DEFAULT_EXCEPTION_CONSUMER;
        private final List<Predicate<T>> filters = new ArrayList<>();

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
        public HandlerBuilder<T> expireAfter(long maxCalls) {
            Preconditions.checkArgument(maxCalls >= 1, "maxCalls >= 1");
            this.maxCalls = maxCalls;
            return this;
        }

        @Override
        public HandlerBuilder<T> exceptionConsumer(BiConsumer<? super T, Throwable> exceptionConsumer) {
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
        public HandlerBuilder<T> withCooldown(Cooldown cooldown) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            filter(t -> cooldown.test());
            return this;
        }

        @Override
        public HandlerBuilder<T> withCooldown(Cooldown cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            Preconditions.checkNotNull(cooldownFailConsumer, "cooldownFailConsumer");
            filter(t -> {
                if (cooldown.test()) {
                    return true;
                }

                cooldownFailConsumer.accept(cooldown, t);
                return false;
            });
            return this;
        }

        @Override
        public HandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            filter(t -> cooldown.get(t).test());
            return this;
        }

        @Override
        public HandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            Preconditions.checkNotNull(cooldownFailConsumer, "cooldownFailConsumer");
            filter(t -> {
                if (cooldown.get(t).test()) {
                    return true;
                }

                cooldownFailConsumer.accept(cooldown.get(t), t);
                return false;
            });
            return this;
        }

        @Override
        public Handler<T> handler(BiConsumer<Handler<T>, ? super T> handler) {
            Preconditions.checkNotNull(handler, "handler");

            HelperEventHandler<T> impl = new HelperEventHandler<>(this, handler);
            impl.register(LoaderUtils.getPlugin());
            return impl;
        }
    }

    private static class MergedHandlerBuilderImpl<T> implements MergedHandlerBuilder<T> {
        private final TypeToken<T> handledClass;
        private final Map<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> mappings = new HashMap<>();

        private long expiry = -1;
        private long maxCalls = -1;
        private BiConsumer<Event, Throwable> exceptionConsumer = DEFAULT_EXCEPTION_CONSUMER;
        private final List<Predicate<T>> filters = new ArrayList<>();

        private MergedHandlerBuilderImpl(TypeToken<T> handledClass) {
            this.handledClass = handledClass;
        }

        @Override
        public <E extends Event> MergedHandlerBuilder<T> bindEvent(Class<E> eventClass, Function<E, T> function) {
            return bindEvent(eventClass, EventPriority.NORMAL, function);
        }

        @Override
        public <E extends Event> MergedHandlerBuilder<T> bindEvent(Class<E> eventClass, EventPriority priority, Function<E, T> function) {
            Preconditions.checkNotNull(eventClass, "eventClass");
            Preconditions.checkNotNull(priority, "priority");
            Preconditions.checkNotNull(function, "function");

            mappings.put(eventClass, new MergedHandlerMapping<>(priority, function));
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> expireAfter(long duration, TimeUnit unit) {
            Preconditions.checkNotNull(unit, "unit");
            Preconditions.checkArgument(duration >= 1, "duration >= 1");
            this.expiry = Math.addExact(System.currentTimeMillis(), unit.toMillis(duration));
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> expireAfter(long maxCalls) {
            Preconditions.checkArgument(maxCalls >= 1, "maxCalls >= 1");
            this.maxCalls = maxCalls;
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> exceptionConsumer(BiConsumer<Event, Throwable> exceptionConsumer) {
            Preconditions.checkNotNull(exceptionConsumer, "exceptionConsumer");
            this.exceptionConsumer = exceptionConsumer;
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> filter(Predicate<T> predicate) {
            Preconditions.checkNotNull(predicate, "predicate");
            this.filters.add(predicate);
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> withCooldown(Cooldown cooldown) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            filter(t -> cooldown.test());
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> withCooldown(Cooldown cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            Preconditions.checkNotNull(cooldownFailConsumer, "cooldownFailConsumer");
            filter(t -> {
                if (cooldown.test()) {
                    return true;
                }

                cooldownFailConsumer.accept(cooldown, t);
                return false;
            });
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            filter(t -> cooldown.get(t).test());
            return this;
        }

        @Override
        public MergedHandlerBuilder<T> withCooldown(CooldownCollection<? super T> cooldown, BiConsumer<Cooldown, ? super T> cooldownFailConsumer) {
            Preconditions.checkNotNull(cooldown, "cooldown");
            Preconditions.checkNotNull(cooldownFailConsumer, "cooldownFailConsumer");
            filter(t -> {
                if (cooldown.get(t).test()) {
                    return true;
                }

                cooldownFailConsumer.accept(cooldown.get(t), t);
                return false;
            });
            return this;
        }

        @Override
        public MergedHandler<T> handler(BiConsumer<MergedHandler<T>, ? super T> handler) {
            Preconditions.checkNotNull(handler, "handler");

            if (mappings.isEmpty()) {
                throw new IllegalStateException("No mappings were created");
            }

            HelperMergedEventHandler<T> impl = new HelperMergedEventHandler<>(this, handler);
            impl.register(LoaderUtils.getPlugin());
            return impl;
        }
    }

    private static class MergedHandlerMapping<T, E extends Event> {
        private final EventPriority priority;
        private final Function<Object, T> function;

        private MergedHandlerMapping(EventPriority priority, Function<E, T> function) {
            this.priority = priority;
            //noinspection unchecked
            this.function = o -> function.apply((E) o);
        }

        public Function<Object, T> getFunction() {
            return function;
        }

        public EventPriority getPriority() {
            return priority;
        }
    }

    @SuppressWarnings("unchecked")
    private static class DefaultFiltersImpl implements DefaultFilters {
        private static final Predicate<? extends Cancellable> IGNORE_CANCELLED = e -> !e.isCancelled();

        private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_BLOCK = e ->
                e.getFrom().getBlockX() != e.getTo().getBlockX() ||
                e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
                e.getFrom().getBlockY() != e.getTo().getBlockY() ||
                !e.getFrom().getWorld().equals(e.getTo().getWorld());

        private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_BLOCK_AND_Y = e ->
                e.getFrom().getBlockX() != e.getTo().getBlockX() ||
                e.getFrom().getBlockZ() != e.getTo().getBlockZ() ||
                !e.getFrom().getWorld().equals(e.getTo().getWorld());

        private static final Predicate<? extends PlayerMoveEvent> IGNORE_SAME_CHUNK = e ->
                (e.getFrom().getBlockX() >> 4) != (e.getTo().getBlockX() >> 4) ||
                (e.getFrom().getBlockZ() >> 4) != (e.getTo().getBlockZ() >> 4) ||
                !e.getFrom().getWorld().equals(e.getTo().getWorld());

        @Override
        public <T extends Cancellable> Predicate<T> ignoreCancelled() {
            return (Predicate<T>) IGNORE_CANCELLED;
        }

        @Override
        public <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlock() {
            return (Predicate<T>) IGNORE_SAME_BLOCK;
        }

        @Override
        public <T extends PlayerMoveEvent> Predicate<T> ignoreSameBlockAndY() {
            return (Predicate<T>) IGNORE_SAME_BLOCK_AND_Y;
        }

        @Override
        public <T extends PlayerMoveEvent> Predicate<T> ignoreSameChunk() {
            return (Predicate<T>) IGNORE_SAME_CHUNK;
        }

        @Override
        public <T extends EntityEvent> Predicate<T> entityHasMetadata(MetadataKey<?> key) {
            return e -> Metadata.provideForEntity(e.getEntity()).has(key);
        }

        @Override
        public <T extends PlayerEvent> Predicate<T> playerHasMetadata(MetadataKey<?> key) {
            return e -> Metadata.provideForPlayer(e.getPlayer()).has(key);
        }

        @Override
        public <T extends PlayerEvent> Predicate<T> playerHasPermission(String permission) {
            return e -> e.getPlayer().hasPermission(permission);
        }
    }

    private Events() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
