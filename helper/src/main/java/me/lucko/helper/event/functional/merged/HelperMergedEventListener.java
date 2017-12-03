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

package me.lucko.helper.event.functional.merged;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import me.lucko.helper.Helper;
import me.lucko.helper.event.MergedSubscription;
import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.timings.Timings;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import co.aikar.timings.lib.MCTiming;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

class HelperMergedEventListener<T> implements MergedSubscription<T>, EventExecutor, Listener {
    private final TypeToken<T> handledClass;
    private final Map<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> mappings;

    private final BiConsumer<Event, Throwable> exceptionConsumer;

    private final Predicate<T>[] filters;
    private final BiPredicate<MergedSubscription<T>, T>[] preExpiryTests;
    private final BiPredicate<MergedSubscription<T>, T>[] midExpiryTests;
    private final BiPredicate<MergedSubscription<T>, T>[] postExpiryTests;
    private final BiConsumer<MergedSubscription<T>, ? super T>[] handlers;

    private final MCTiming timing;

    private final AtomicLong callCount = new AtomicLong(0);
    private final AtomicBoolean active = new AtomicBoolean(true);

    @SuppressWarnings("unchecked")
    HelperMergedEventListener(MergedBuilder<T> builder, List<BiConsumer<MergedSubscription<T>, ? super T>> handlers) {
        this.handledClass = builder.handledClass;
        this.mappings = ImmutableMap.copyOf(builder.mappings);
        this.exceptionConsumer = builder.exceptionConsumer;

        this.filters = builder.filters.toArray(new Predicate[builder.filters.size()]);
        this.preExpiryTests = builder.preExpiryTests.toArray(new BiPredicate[builder.preExpiryTests.size()]);
        this.midExpiryTests = builder.midExpiryTests.toArray(new BiPredicate[builder.midExpiryTests.size()]);
        this.postExpiryTests = builder.postExpiryTests.toArray(new BiPredicate[builder.postExpiryTests.size()]);
        this.handlers = handlers.toArray(new BiConsumer[handlers.size()]);

        this.timing = Timings.of("helper-events: " + handlers.stream().map(handler -> Delegate.resolve(handler).getClass().getName()).collect(Collectors.joining(" | ")));
    }

    void register(Plugin plugin) {
        for (Map.Entry<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> ent : mappings.entrySet()) {
            Helper.plugins().registerEvent(ent.getKey(), this, ent.getValue().getPriority(), this, plugin, false);
        }
    }

    @Override
    public void execute(Listener listener, Event event) {
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

        // this handler is disabled, so unregister from the event.
        if (!active.get()) {
            event.getHandlers().unregister(listener);
            return;
        }

        // obtain the handled instance
        T handledInstance = function.apply(event);

        // check pre-expiry tests
        for (BiPredicate<MergedSubscription<T>, T> test : preExpiryTests) {
            if (test.test(this, handledInstance)) {
                event.getHandlers().unregister(listener);
                active.set(false);
                return;
            }
        }

        // begin "handling" of the event
        try (MCTiming t = timing.startTiming()) {
            // check the filters
            for (Predicate<T> filter : filters) {
                if (!filter.test(handledInstance)) {
                    return;
                }
            }

            // check mid-expiry tests
            for (BiPredicate<MergedSubscription<T>, T> test : midExpiryTests) {
                if (test.test(this, handledInstance)) {
                    event.getHandlers().unregister(listener);
                    active.set(false);
                    return;
                }
            }

            // call the handler
            for (BiConsumer<MergedSubscription<T>, ? super T> handler : handlers) {
                handler.accept(this, handledInstance);
            }

            // increment call counter
            callCount.incrementAndGet();
        } catch (Throwable t) {
            exceptionConsumer.accept(event, t);
        }

        // check post-expiry tests
        for (BiPredicate<MergedSubscription<T>, T> test : postExpiryTests) {
            if (test.test(this, handledInstance)) {
                event.getHandlers().unregister(listener);
                active.set(false);
                return;
            }
        }
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
    public boolean unregister() {
        // already unregistered
        if (!active.getAndSet(false)) {
            return false;
        }

        // also remove the handler directly, just in case the event has a really low throughput.
        // (the event would also be unregistered next time it's called - but this obviously assumes
        // the event will be called again soon)
        for (Class<? extends Event> clazz : mappings.keySet()) {
            unregisterListener(clazz, this);
        }

        return true;
    }

    @Nonnull
    @Override
    public Class<? super T> getHandledClass() {
        return handledClass.getRawType();
    }

    @Nonnull
    @Override
    public Set<Class<? extends Event>> getEventClasses() {
        return mappings.keySet();
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void unregisterListener(Class<? extends Event> eventClass, Listener listener) {
        try {
            // unfortunately we can't cache this reflect call, as the method is static
            Method getHandlerListMethod = eventClass.getMethod("getHandlerList");
            HandlerList handlerList = (HandlerList) getHandlerListMethod.invoke(null);
            handlerList.unregister(listener);
        } catch (Throwable t) {
            // ignored
        }
    }
}
