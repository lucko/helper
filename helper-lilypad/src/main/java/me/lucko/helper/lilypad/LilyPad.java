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

package me.lucko.helper.lilypad;

import me.lucko.helper.messaging.InstanceData;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.network.redirect.PlayerRedirector;
import me.lucko.helper.profiles.Profile;

import org.bukkit.entity.Player;

import lilypad.client.connect.api.Connect;

import javax.annotation.Nonnull;

/**
 * Represents a hook with LilyPad {@link Connect}.
 */
public interface LilyPad extends Messenger, InstanceData, PlayerRedirector {

    /**
     * Gets the Connect instance
     *
     * @return the Connect instance
     */
    @Nonnull
    Connect getConnect();

    /**
     * Requests that a certain player is moved to the given server.
     *
     * @param serverId the id of the server
     * @param playerUsername the username of the player
     */
    void redirectPlayer(@Nonnull String serverId, @Nonnull String playerUsername);

    /**
     * Requests that a certain player is moved to the given server.
     *
     * @param serverId the id of the server
     * @param player the player
     */
    default void redirectPlayer(@Nonnull String serverId, @Nonnull Player player) {
        redirectPlayer(serverId, player.getName());
    }

    @Override
    default void redirectPlayer(String serverId, Profile profile) {
        redirectPlayer(serverId, profile.getName().orElseThrow(() -> new IllegalArgumentException("Username must be set")));
    }
}
