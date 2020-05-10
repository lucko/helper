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

package me.lucko.helper.text;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderHook;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.serializer.ComponentSerializers;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for working with {@link Component}s and formatted text strings.
 */
@SuppressWarnings("deprecation")
public final class Text {

    private static final Plugin PAPI_PLUGIN = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");

    public static final char SECTION_CHAR = '\u00A7'; // §
    public static final char AMPERSAND_CHAR = '&';

    public static String joinNewline(String... strings) {
        return joinNewline(Arrays.stream(strings));
    }

    public static String joinNewline(Stream<String> strings) {
        return strings.collect(Collectors.joining("\n"));
    }

    public static TextComponent fromLegacy(String input, char character) {
        return ComponentSerializers.LEGACY.deserialize(input, character);
    }

    public static TextComponent fromLegacy(String input) {
        return ComponentSerializers.LEGACY.deserialize(input);
    }

    public static String toLegacy(Component component, char character) {
        return ComponentSerializers.LEGACY.serialize(component, character);
    }

    public static String toLegacy(Component component) {
        return ComponentSerializers.LEGACY.serialize(component);
    }

    public static void sendMessage(CommandSender sender, Component message) {
        TextAdapter.sendComponent(sender, message);
    }

    public static void sendMessage(Iterable<CommandSender> senders, Component message) {
        TextAdapter.sendComponent(senders, message);
    }

    public static String colorize(String s) {
        return s == null ? null : translateAlternateColorCodes(AMPERSAND_CHAR, SECTION_CHAR, s);
    }

    public static List<String> colorize(String... lines) {
        List<String> s = new ArrayList<>();
        for (String value : lines) {
            s.add(colorize(value));
        }
        return s;
    }

    public static List<String> colorize(List<String> lines) {
        List<String> s = new ArrayList<>();
        for (String value : lines) {
            s.add(colorize(value));
        }
        return s;
    }

    public static String decolorize(String s) {
        return s == null ? null : translateAlternateColorCodes(SECTION_CHAR, AMPERSAND_CHAR, s);
    }

    public static String translateAlternateColorCodes(char from, char to, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == from && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = to;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static String setPlaceholders(String text) {
        return setPlaceholders(null, text);
    }

    public static String setPlaceholders(OfflinePlayer player, String text) {
        if (isPlaceholderAPISupported()) {
            return PlaceholderAPI.setPlaceholders((OfflinePlayer) player, text);
        }
        return colorize(text);
    }

    public static List<String> setPlaceholders(String... lines) {
        return setPlaceholders(null, lines);
    }

    public static List<String> setPlaceholders(OfflinePlayer player, String... lines) {
        if (!isPlaceholderAPISupported()) {
            return colorize(lines);
        }
        List<String> s = new ArrayList<>();
        for (String value : lines) {
            s.add(setPlaceholders(player, value));
        }
        return s;
    }

    public static List<String> setPlaceholders(List<String> lines) {
        return setPlaceholders(null, lines);
    }

    public static List<String> setPlaceholders(OfflinePlayer player, List<String> lines) {
        if (!isPlaceholderAPISupported()) {
            return colorize(lines);
        }
        List<String> s = new ArrayList<>();
        for (String value : lines) {
            s.add(setPlaceholders(player, value));
        }
        return s;
    }

    public static String setBracketPlaceholders(String text) {
        return setBracketPlaceholders(null, text);
    }

    public static String setBracketPlaceholders(OfflinePlayer player, String text) {
        if (isPlaceholderAPISupported()) {
            return PlaceholderAPI.setBracketPlaceholders(player, text);
        }
        return colorize(text);
    }

    public static boolean registerPlaceholderHook(String identifier, PlaceholderHook placeholderHook) {
        if (isPlaceholderAPISupported()) {
            return PlaceholderAPI.registerPlaceholderHook(identifier, placeholderHook);
        }
        return false;
    }

    public static boolean unregisterPlaceholderHook(String identifier) {
        if (isPlaceholderAPISupported()) {
            return PlaceholderAPI.unregisterPlaceholderHook(identifier);
        }
        return false;
    }

    private static boolean isPlaceholderAPISupported() {
        return PAPI_PLUGIN != null && PAPI_PLUGIN.isEnabled();
    }

    private Text() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
