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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utilities for working with {@link Component}s and formatted text strings.
 *
 * @deprecated Use {@link me.lucko.helper.text3.Text}
 */
@Deprecated
public final class Text {

    public static final char SECTION_CHAR = me.lucko.helper.text3.Text.SECTION_CHAR;
    public static final char AMPERSAND_CHAR = me.lucko.helper.text3.Text.AMPERSAND_CHAR;

    public static String joinNewline(String... strings) {
        return me.lucko.helper.text3.Text.joinNewline(strings);
    }

    public static String joinNewline(Stream<String> strings) {
        return me.lucko.helper.text3.Text.joinNewline(strings);
    }

    public static TextComponent fromLegacy(String input, char character) {
        return me.lucko.helper.text3.Text.fromLegacy(input, character);
    }

    public static TextComponent fromLegacy(String input) {
        return me.lucko.helper.text3.Text.fromLegacy(input);
    }

    public static String toLegacy(Component component, char character) {
        return me.lucko.helper.text3.Text.toLegacy(component, character);
    }

    public static String toLegacy(Component component) {
        return me.lucko.helper.text3.Text.toLegacy(component);
    }

    public static void sendMessage(CommandSender sender, Component message) {
        me.lucko.helper.text3.Text.sendMessage(sender, message);
    }

    public static void sendMessage(Iterable<CommandSender> senders, Component message) {
        me.lucko.helper.text3.Text.sendMessage(senders, message);
    }

    public static String colorize(String s) {
        return me.lucko.helper.text3.Text.colorize(s);
    }

    public static List<String> colorize(String... lines) {
        return me.lucko.helper.text3.Text.colorize(lines);
    }

    public static List<String> colorize(List<String> lines) {
        return me.lucko.helper.text3.Text.colorize(lines);
    }

    public static String decolorize(String s) {
        return me.lucko.helper.text3.Text.decolorize(s);
    }

    public static String translateAlternateColorCodes(char from, char to, String textToTranslate) {
        return me.lucko.helper.text3.Text.translateAlternateColorCodes(from, to, textToTranslate);
    }

    public static String setPlaceholders(String text) {
        return setPlaceholders(null, text);
    }

    public static String setPlaceholders(OfflinePlayer player, String text) {
        return me.lucko.helper.text3.Text.setPlaceholders(player, text);
    }

    public static List<String> setPlaceholders(String... lines) {
        return setPlaceholders(null, lines);
    }

    public static List<String> setPlaceholders(OfflinePlayer player, String... lines) {
        return me.lucko.helper.text3.Text.setPlaceholders(player, lines);
    }

    public static List<String> setPlaceholders(List<String> lines) {
        return me.lucko.helper.text3.Text.setPlaceholders(lines);
    }

    public static List<String> setPlaceholders(OfflinePlayer player, List<String> lines) {
        return me.lucko.helper.text3.Text.setPlaceholders(player, lines);
    }

    public static String setBracketPlaceholders(String text) {
        return me.lucko.helper.text3.Text.setBracketPlaceholders(text);
    }

    public static String setBracketPlaceholders(OfflinePlayer player, String text) {
        return me.lucko.helper.text3.Text.setBracketPlaceholders(player, text);
    }

    public static boolean registerPlaceholderHook(String identifier, PlaceholderHook placeholderHook) {
        return me.lucko.helper.text3.Text.registerPlaceholderHook(identifier, placeholderHook);
    }

    public static boolean unregisterPlaceholderHook(String identifier) {
        return me.lucko.helper.text3.Text.unregisterPlaceholderHook(identifier);
    }

    private static boolean isPlaceholderAPISupported() {
        return me.lucko.helper.text3.Text.isPlaceholderAPISupported();
    }

    private Text() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
