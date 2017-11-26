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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;

import me.lucko.helper.Helper;
import me.lucko.helper.event.MergedSubscription;
import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.timings.Timings;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import co.aikar.timings.lib.MCTiming;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

class HelperMergedEventListener<T> implements MergedSubscription<T>, EventExecutor, Listener {
    private final TypeToken<T> handledClass;
    private final Map<Class<? extends Event>, MergedHandlerMapping<T, ? extends Event>> mappings;

    private final long expiry;
    private final long maxCalls;
    private final BiConsumer<Event, Throwable> exceptionConsumer;
    private final List<Predicate<T>> filters;
    private final BiConsumer<MergedSubscription<T>, ? super T> handler;
    private final MCTiming timing;

    private final AtomicLong callCount = new AtomicLong(0);
    private final AtomicBoolean active = new AtomicBoolean(true);

    HelperMergedEventListener(MergedBuilder<T> builder, BiConsumer<MergedSubscription<T>, ? super T> handler) {
        this.handledClass = builder.handledClass;
        this.mappings = ImmutableMap.copyOf(builder.mappings);
        this.expiry = builder.expiry;
        this.maxCalls = builder.maxCalls;
        this.exceptionConsumer = builder.exceptionConsumer;
        this.filters = ImmutableList.copyOf(builder.filters);
        this.handler = handler;
        this.timing = Timings.of("helper-events: " + Delegate.resolve(handler).getClass().getName());
    }

    void register(Plugin plugin) {
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

    @Nonnull
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
}
