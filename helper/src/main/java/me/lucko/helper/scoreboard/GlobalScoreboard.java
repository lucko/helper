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

package me.lucko.helper.scoreboard;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.lucko.helper.utils.LoaderUtils;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Contains a "global" scoreboard instance, lazily loaded on first request.
 */
public final class GlobalScoreboard {
    private static PacketScoreboard scoreboard = null;

    /**
     * Gets the global scoreboard
     * @return a scoreboard instance
     * @throws IllegalStateException if ProtocolLib is not loaded
     */
    public static synchronized PacketScoreboard get() {
        if (scoreboard == null) {
            try {
                Class.forName("com.comphenix.protocol.ProtocolManager");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("ProtocolLib not loaded");
            }

            JavaPlugin plugin = LoaderUtils.getPlugin();
            if (plugin instanceof ExtendedJavaPlugin) {
                scoreboard = new PacketScoreboard(((ExtendedJavaPlugin) plugin));
            } else {
                scoreboard = new PacketScoreboard();
            }
        }

        return scoreboard;
    }

    private GlobalScoreboard() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
