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

package me.lucko.helper.network.modules;

import me.lucko.helper.eventbus.Subscribers;
import me.lucko.helper.network.Network;
import me.lucko.helper.network.event.NetworkEvent;
import me.lucko.helper.network.event.ServerConnectEvent;
import me.lucko.helper.network.event.ServerDisconnectEvent;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;

import net.kyori.event.EventBus;

import javax.annotation.Nonnull;

public class NetworkStatusModule implements TerminableModule {
    private final Network network;

    public NetworkStatusModule(Network network) {
        this.network = network;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        EventBus<NetworkEvent> bus = this.network.getEventBus();

        Subscribers.register(bus, ServerConnectEvent.class, event -> {
            broadcast("&7[&anetwork&7] &b" + event.getId() + " &7connected.");
        }).bindWith(consumer);

        Subscribers.register(bus, ServerDisconnectEvent.class, event -> {
            if (event.getReason() != null && !event.getReason().isEmpty()) {
                broadcast("&7[&anetwork&7] &b" + event.getId() + " &7disconnected. (reason: " + event.getReason() + ")");
            } else {
                broadcast("&7[&anetwork&7] &b" + event.getId() + " &7disconnected. (reason unknown)");
            }

        }).bindWith(consumer);
    }

    private static void broadcast(String message) {
        Players.stream()
                .filter(p -> p.hasPermission("helper.networkstatus.alerts"))
                .forEach(p -> Players.msg(p, message));
    }
}
