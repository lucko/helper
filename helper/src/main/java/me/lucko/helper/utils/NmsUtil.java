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

package me.lucko.helper.utils;

import org.bukkit.Bukkit;

import javax.annotation.Nonnull;

public final class NmsUtil {

    public static final String NMS = "net.minecraft.server";
    public static final String OBC = "org.bukkit.craftbukkit";

    private static final String SERVER_VERSION;
    private static final String SERVER_VERSION_PACKAGE_COMPONENT;

    private static final String NMS_PREFIX;
    private static final String OBC_PREFIX;

    static {
        String serverVersion = "";

        Class<?> server = Bukkit.getServer().getClass();

        // check we're dealing with a "CraftServer" and that the server isn't non-versioned.
        if (server.getSimpleName().equals("CraftServer") && !server.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            String obcPackage = server.getPackage().getName();

            // check we're dealing with a craftbukkit implementation.
            if (obcPackage.startsWith("org.bukkit.craftbukkit.")) {
                // return the nms version.
                serverVersion = obcPackage.substring("org.bukkit.craftbukkit.".length());
            }
        }

        SERVER_VERSION = serverVersion;

        if (SERVER_VERSION.isEmpty()) {
            SERVER_VERSION_PACKAGE_COMPONENT = ".";
        } else {
            SERVER_VERSION_PACKAGE_COMPONENT = "." + SERVER_VERSION + ".";
        }

        NMS_PREFIX = NMS + SERVER_VERSION_PACKAGE_COMPONENT;
        OBC_PREFIX = OBC + SERVER_VERSION_PACKAGE_COMPONENT;
    }

    @Nonnull
    public static String getServerVersion() {
        return SERVER_VERSION;
    }

    @Nonnull
    public static String nms(String className) {
        return NMS_PREFIX.concat(className);
    }

    @Nonnull
    public static Class<?> nmsClass(String className) throws ClassNotFoundException {
        return Class.forName(nms(className));
    }

    @Nonnull
    public static String obc(String className) {
        return OBC_PREFIX.concat(className);
    }

    @Nonnull
    public static Class<?> obcClass(String className) throws ClassNotFoundException {
        return Class.forName(obc(className));
    }

    private NmsUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
