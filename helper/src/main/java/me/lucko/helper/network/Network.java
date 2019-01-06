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

package me.lucko.helper.network;

import me.lucko.helper.Services;
import me.lucko.helper.messaging.InstanceData;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.network.event.NetworkEvent;
import me.lucko.helper.network.metadata.ServerMetadataProvider;
import me.lucko.helper.profiles.Profile;
import me.lucko.helper.terminable.Terminable;

import net.kyori.event.EventBus;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Represents the interface for a network.
 */
public interface Network extends Terminable {

    /**
     * Creates a new {@link Network} instance. These should be shared if possible.
     *
     * @param messenger the messenger
     * @param instanceData the instance data
     * @return the new network
     */
    static Network create(Messenger messenger, InstanceData instanceData) {
        return new AbstractNetwork(messenger, instanceData);
    }

    /**
     * Tries to obtain an instance of network from the services manager, falling
     * back to given supplier if one is not already present.
     *
     * @param ifElse the supplier
     * @return the network instance
     */
    static Network obtain(Supplier<Network> ifElse) {
        Network network = Services.get(Network.class).orElse(null);
        if (network == null) {
            network = ifElse.get();
            Services.provide(Network.class, network);
        }
        return network;
    }

    /**
     * Gets the known servers in the network
     *
     * @return the known servers
     */
    Map<String, Server> getServers();

    /**
     * Gets the players known to be online in the network.
     *
     * @return the known online players
     */
    Map<UUID, Profile> getOnlinePlayers();

    /**
     * Gets the overall player count
     *
     * @return the player count
     */
    int getOverallPlayerCount();

    /**
     * Registers a metadata provider for this server with the network.
     *
     * @param metadataProvider the provider
     */
    void registerMetadataProvider(ServerMetadataProvider metadataProvider);

    /**
     * Gets the network event bus.
     *
     * @return the event bus
     */
    EventBus<NetworkEvent> getEventBus();

    @Override
    void close();
}
