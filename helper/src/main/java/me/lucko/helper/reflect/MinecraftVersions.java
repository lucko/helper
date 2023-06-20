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

package me.lucko.helper.reflect;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class which holds some common versions of Minecraft.
 *
 * @author Kristian (ProtocolLib)
 */
public final class MinecraftVersions {

    /**
     * Version 1.20 - the trails and tails update
     */
    public static final MinecraftVersion v1_20 = MinecraftVersion.parse("1.20");
    /**
     * Version 1.19 - the wild update
     */
    public static final MinecraftVersion v1_19 = MinecraftVersion.parse("1.19");

    /**
     * Version 1.18 - caves and cliffs part 2
     */
    public static final MinecraftVersion v1_18 = MinecraftVersion.parse("1.18");
    
    /**
     * Version 1.17 - caves and cliffs part 1
     */
    public static final MinecraftVersion v1_17 = MinecraftVersion.parse("1.17");

    /**
     * Version 1.16 - nether update
     */
    public static final MinecraftVersion v1_16 = MinecraftVersion.parse("1.16");

    /**
     * Version 1.15 - buzzy bees update
     */
    public static final MinecraftVersion v1_15 = MinecraftVersion.parse("1.15");

    /**
     * Version 1.14 - village and pillage update
     */
    public static final MinecraftVersion v1_14 = MinecraftVersion.parse("1.14");

    /**
     * Version 1.13 - update aquatic.
     */
    public static final MinecraftVersion v1_13 = MinecraftVersion.parse("1.13");

    /**
     * Version 1.12 - the world of color update.
     */
    public static final MinecraftVersion v1_12 = MinecraftVersion.parse("1.12");

    /**
     * Version 1.11 - the exploration update.
     */
    public static final MinecraftVersion v1_11 = MinecraftVersion.parse("1.11");

    /**
     * Version 1.10 - the frostburn update.
     */
    public static final MinecraftVersion v1_10 = MinecraftVersion.parse("1.10");

    /**
     * Version 1.9 - the combat update.
     */
    public static final MinecraftVersion v1_9 = MinecraftVersion.parse("1.9");

    /**
     * Version 1.8 - the "bountiful" update.
     */
    public static final MinecraftVersion v1_8 = MinecraftVersion.parse("1.8");

    /**
     * Version 1.7.8 - the update that changed the skin format (and distribution - R.I.P. player disguise)
     */
    public static final MinecraftVersion v1_7_8 = MinecraftVersion.parse("1.7.8");

    /**
     * Version 1.7.2 - the update that changed the world.
     */
    public static final MinecraftVersion v1_7_2 = MinecraftVersion.parse("1.7.2");

    /**
     * Version 1.6.1 - the horse update.
     */
    public static final MinecraftVersion v1_6_1 = MinecraftVersion.parse("1.6.1");

    /**
     * Version 1.5.0 - the redstone update.
     */
    public static final MinecraftVersion v1_5_0 = MinecraftVersion.parse("1.5.0");

    /**
     * Version 1.4.2 - the scary update (Wither Boss).
     */
    public static final MinecraftVersion v1_4_2 = MinecraftVersion.parse("1.4.2");

    /**
     * Regular expression used to parse version strings.
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-\\.]+)\\s*\\)");

    /**
     * The version of the runtime
     */
    static final MinecraftVersion RUNTIME_VERSION = parseServerVersion(Bukkit.getVersion());

    private static MinecraftVersion parseServerVersion(String serverVersion) {
        Matcher version = VERSION_PATTERN.matcher(serverVersion);

        if (version.matches() && version.group(1) != null) {
            return MinecraftVersion.parse(version.group(1));
        } else {
            throw new IllegalStateException("Cannot parse version String '" + serverVersion + "'");
        }
    }

    private MinecraftVersions() {}
}
