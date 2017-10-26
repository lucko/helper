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

package me.lucko.helper.js.utils;

import me.lucko.helper.function.Numbers;
import me.lucko.helper.gson.GsonProvider;
import me.lucko.helper.hologram.Hologram;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.menu.Gui;
import me.lucko.helper.menu.paginated.PaginatedGui;
import me.lucko.helper.menu.scheme.MenuScheme;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.messaging.bungee.BungeeMessaging;
import me.lucko.helper.metadata.Metadata;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.scoreboard.Scoreboard;
import me.lucko.helper.serialize.BlockPosition;
import me.lucko.helper.text.TextUtils;

public final class EnsureLoad {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void ensure() {
        /*
         * Forces the initialisation of classes. This only needs to be done once per package,
         * and doesn't need to be performed for classes or packages which are naturally used by helper-js.
         *
         * This forces the resolveWildcardPackage function to return all helper packages.
         */
        forceInit(Numbers.class);
        forceInit(GsonProvider.class);
        forceInit(Hologram.class);
        forceInit(ItemStackBuilder.class);
        forceInit(Gui.class);
        forceInit(MenuScheme.class);
        forceInit(PaginatedGui.class);
        forceInit(Messenger.class);
        forceInit(BungeeMessaging.class);
        forceInit(Metadata.class);
        forceInit(Promise.class);
        forceInit(Scoreboard.class);
        forceInit(BlockPosition.class);
        forceInit(TextUtils.class);
    }

    // simply passing the class as a parameter is enough to load it
    @SuppressWarnings("UnusedReturnValue")
    private static <T> Class<T> forceInit(Class<T> clazz) {
        // do nothing
        return clazz;
    }

    private EnsureLoad() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
