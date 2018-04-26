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

package me.lucko.helper.lilypad.extended;

import me.lucko.helper.lilypad.LilyPad;
import me.lucko.helper.profiles.Profile;
import me.lucko.helper.terminable.Terminable;

import java.util.Map;
import java.util.UUID;

/**
 * Represents the interface for an extended LilyPad network.
 */
public interface LilyPadNetwork extends Terminable {

    /**
     * Creates a new {@link LilyPadNetwork} instance. These should be shared if possible.
     *
     * @param lilyPad the lilypad instance
     * @return the new network
     */
    static LilyPadNetwork create(LilyPad lilyPad) {
        return new LilyPadNetworkImpl(lilyPad);
    }

    /**
     * Gets the known servers in the network
     *
     * @return the known servers
     */
    Map<String, LilyPadServer> getServers();

    /**
     * Gets the players known to be online in the network.
     *
     * @return the known online players
     */
    Map<UUID, Profile> getOnlinePlayers();

    /**
     * Gets a cached overall player count
     *
     * @return the player count
     */
    int getOverallPlayerCount();

    @Override
    void close();
}
