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

package me.lucko.helper.event.functional.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import me.lucko.helper.event.ProtocolSubscription;
import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.protocol.Protocol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

class HelperProtocolListener extends PacketAdapter implements ProtocolSubscription {
    private final Set<PacketType> types;

    private final BiConsumer<? super PacketEvent, Throwable> exceptionConsumer;

    private final Predicate<PacketEvent>[] filters;
    private final BiPredicate<ProtocolSubscription, PacketEvent>[] preExpiryTests;
    private final BiPredicate<ProtocolSubscription, PacketEvent>[] midExpiryTests;
    private final BiPredicate<ProtocolSubscription, PacketEvent>[] postExpiryTests;
    private final BiConsumer<ProtocolSubscription, ? super PacketEvent>[] handlers;

    private final AtomicLong callCount = new AtomicLong(0);
    private final AtomicBoolean active = new AtomicBoolean(true);

    @SuppressWarnings("unchecked")
    HelperProtocolListener(ProtocolSubscriptionBuilderImpl builder, List<BiConsumer<ProtocolSubscription, ? super PacketEvent>> handlers) {
        super(LoaderUtils.getPlugin(), builder.priority, builder.types);

        this.types = builder.types;
        this.exceptionConsumer = builder.exceptionConsumer;

        this.filters = builder.filters.toArray(new Predicate[builder.filters.size()]);
        this.preExpiryTests = builder.preExpiryTests.toArray(new BiPredicate[builder.preExpiryTests.size()]);
        this.midExpiryTests = builder.midExpiryTests.toArray(new BiPredicate[builder.midExpiryTests.size()]);
        this.postExpiryTests = builder.postExpiryTests.toArray(new BiPredicate[builder.postExpiryTests.size()]);
        this.handlers = handlers.toArray(new BiConsumer[handlers.size()]);

        Protocol.manager().addPacketListener(this);
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        onPacket(event);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        onPacket(event);
    }

    private void onPacket(PacketEvent event) {
        // check we actually want this event
        if (!this.types.contains(event.getPacketType())) {
            return;
        }

        // this handler is disabled, so don't listen
        if (!this.active.get()) {
            return;
        }

        // check pre-expiry tests
        for (BiPredicate<ProtocolSubscription, PacketEvent> test : this.preExpiryTests) {
            if (test.test(this, event)) {
                unregister();
                return;
            }
        }

        // begin "handling" of the event
        try {
            // check the filters
            for (Predicate<PacketEvent> filter : this.filters) {
                if (!filter.test(event)) {
                    return;
                }
            }

            // check mid-expiry tests
            for (BiPredicate<ProtocolSubscription, PacketEvent> test : this.midExpiryTests) {
                if (test.test(this, event)) {
                    unregister();
                    return;
                }
            }

            // call the handler
            for (BiConsumer<ProtocolSubscription, ? super PacketEvent> handler : this.handlers) {
                handler.accept(this, event);
            }

            // increment call counter
            this.callCount.incrementAndGet();
        } catch (Throwable t) {
            this.exceptionConsumer.accept(event, t);
        }

        // check post-expiry tests
        for (BiPredicate<ProtocolSubscription, PacketEvent> test : this.postExpiryTests) {
            if (test.test(this, event)) {
                unregister();
                return;
            }
        }
    }

    @Nonnull
    @Override
    public Set<PacketType> getPackets() {
        return this.types;
    }

    @Override
    public boolean isActive() {
        return this.active.get();
    }

    @Override
    public boolean isClosed() {
        return !this.active.get();
    }

    @Override
    public long getCallCounter() {
        return this.callCount.get();
    }

    @Override
    public boolean unregister() {
        // already unregistered
        if (!this.active.getAndSet(false)) {
            return false;
        }

        Protocol.manager().removePacketListener(this);
        return true;
    }

    @Override
    public Collection<Object> getFunctions() {
        List<Object> functions = new ArrayList<>();
        Collections.addAll(functions, this.filters);
        Collections.addAll(functions, this.preExpiryTests);
        Collections.addAll(functions, this.midExpiryTests);
        Collections.addAll(functions, this.postExpiryTests);
        Collections.addAll(functions, this.handlers);
        return functions;
    }
}
