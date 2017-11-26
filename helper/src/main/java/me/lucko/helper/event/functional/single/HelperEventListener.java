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

package me.lucko.helper.event.functional.single;

import com.google.common.collect.ImmutableList;

import me.lucko.helper.Helper;
import me.lucko.helper.event.SingleSubscription;
import me.lucko.helper.interfaces.Delegate;
import me.lucko.helper.timings.Timings;

import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import co.aikar.timings.lib.MCTiming;

import java.lang.reflect.Method;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

class HelperEventListener<T extends Event> implements SingleSubscription<T>, EventExecutor, Listener {
    private final Class<T> eventClass;
    private final EventPriority priority;

    private final long expiry;
    private final long maxCalls;
    private final BiConsumer<? super T, Throwable> exceptionConsumer;
    private final List<Predicate<T>> filters;
    private final BiConsumer<SingleSubscription<T>, ? super T> handler;
    private final MCTiming timing;

    private final AtomicLong callCount = new AtomicLong(0);
    private final AtomicBoolean active = new AtomicBoolean(true);

    HelperEventListener(SingleBuilder<T> builder, BiConsumer<SingleSubscription<T>, ? super T> handler) {
        this.eventClass = builder.eventClass;
        this.priority = builder.priority;
        this.expiry = builder.expiry;
        this.maxCalls = builder.maxCalls;
        this.exceptionConsumer = builder.exceptionConsumer;
        this.filters = ImmutableList.copyOf(builder.filters);
        this.handler = handler;
        this.timing = Timings.of("helper-events: " + Delegate.resolve(handler).getClass().getName());
    }

    void register(Plugin plugin) {
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

    @Nonnull
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
