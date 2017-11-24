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

package me.lucko.helper.timings;

import me.lucko.helper.internal.LoaderUtils;
import me.lucko.helper.utils.annotation.NonnullByDefault;

import org.bukkit.plugin.java.JavaPlugin;

import co.aikar.timings.lib.MCTiming;
import co.aikar.timings.lib.TimingManager;

import javax.annotation.Nullable;

/**
 * Provides access to a {@link TimingManager}
 */
@NonnullByDefault
public final class Timings {
    @Nullable
    private static TimingManager timingManager = null;

    /**
     * Gets the TimingManager
     * @return a timingmanager instance
     */
    public static synchronized TimingManager get() {
        if (timingManager == null) {
            JavaPlugin plugin = LoaderUtils.getPlugin();
            timingManager = TimingManager.of(plugin);
        }

        return timingManager;
    }

    public static MCTiming ofStart(String name) {
        return get().ofStart(name);
    }

    public static MCTiming ofStart(String name, MCTiming parent) {
        return get().ofStart(name, parent);
    }

    public static MCTiming of(String name) {
        return get().of(name);
    }

    public static MCTiming of(String name, MCTiming parent) {
        return get().of(name, parent);
    }

    private Timings() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
