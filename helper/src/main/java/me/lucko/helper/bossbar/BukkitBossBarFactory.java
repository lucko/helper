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

package me.lucko.helper.bossbar;

import me.lucko.helper.text.Text;

import org.bukkit.Server;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link BossBarFactory} using Bukkit.
 */
public class BukkitBossBarFactory implements BossBarFactory {
    private final Server server;

    public BukkitBossBarFactory(Server server) {
        this.server = server;
    }

    @Nonnull
    @Override
    public BossBar newBossBar() {
        return new BukkitBossBar(this.server.createBossBar("null", convertColor(BossBarColor.defaultColor()), convertStyle(BossBarStyle.defaultStyle())));
    }

    private static class BukkitBossBar implements BossBar {
        private final org.bukkit.boss.BossBar bar;

        BukkitBossBar(org.bukkit.boss.BossBar bar) {
            this.bar = bar;
        }

        @Nonnull
        @Override
        public String title() {
            return this.bar.getTitle();
        }

        @Nonnull
        @Override
        public BossBar title(@Nonnull String title) {
            this.bar.setTitle(Text.colorize(title));
            return this;
        }

        @Override
        public double progress() {
            return this.bar.getProgress();
        }

        @Nonnull
        @Override
        public BossBar progress(double progress) {
            this.bar.setProgress(progress);
            return this;
        }

        @Nonnull
        @Override
        public BossBarColor color() {
            return convertColor(this.bar.getColor());
        }

        @Nonnull
        @Override
        public BossBar color(@Nonnull BossBarColor color) {
            this.bar.setColor(convertColor(color));
            return this;
        }

        @Nonnull
        @Override
        public BossBarStyle style() {
            return convertStyle(this.bar.getStyle());
        }

        @Nonnull
        @Override
        public BossBar style(@Nonnull BossBarStyle style) {
            this.bar.setStyle(convertStyle(style));
            return this;
        }

        @Override
        public boolean visible() {
            return this.bar.isVisible();
        }

        @Nonnull
        @Override
        public BossBar visible(boolean visible) {
            this.bar.setVisible(visible);
            return this;
        }

        @Nonnull
        @Override
        public List<Player> players() {
            return this.bar.getPlayers();
        }

        @Override
        public void addPlayer(@Nonnull Player player) {
            this.bar.addPlayer(player);
        }

        @Override
        public void removePlayer(@Nonnull Player player) {
            this.bar.removePlayer(player);
        }

        @Override
        public void removeAll() {
            this.bar.removeAll();
        }

        @Override
        public void close() {
            removeAll();
        }
    }

    private static BossBarStyle convertStyle(BarStyle style) {
        switch (style) {
            case SOLID:
                return BossBarStyle.SOLID;
            case SEGMENTED_6:
                return BossBarStyle.SEGMENTED_6;
            case SEGMENTED_10:
                return BossBarStyle.SEGMENTED_10;
            case SEGMENTED_12:
                return BossBarStyle.SEGMENTED_12;
            case SEGMENTED_20:
                return BossBarStyle.SEGMENTED_20;
            default:
                return BossBarStyle.defaultStyle();
        }
    }

    private static BarStyle convertStyle(BossBarStyle style) {
        switch (style) {
            case SOLID:
                return BarStyle.SOLID;
            case SEGMENTED_6:
                return BarStyle.SEGMENTED_6;
            case SEGMENTED_10:
                return BarStyle.SEGMENTED_10;
            case SEGMENTED_12:
                return BarStyle.SEGMENTED_12;
            case SEGMENTED_20:
                return BarStyle.SEGMENTED_20;
            default:
                return convertStyle(BossBarStyle.defaultStyle());
        }
    }

    private static BossBarColor convertColor(BarColor color) {
        switch (color) {
            case PINK:
                return BossBarColor.PINK;
            case BLUE:
                return BossBarColor.BLUE;
            case RED:
                return BossBarColor.RED;
            case GREEN:
                return BossBarColor.GREEN;
            case YELLOW:
                return BossBarColor.YELLOW;
            case PURPLE:
                return BossBarColor.PURPLE;
            case WHITE:
                return BossBarColor.WHITE;
            default:
                return BossBarColor.defaultColor();
        }
    }

    private static BarColor convertColor(BossBarColor color) {
        switch (color) {
            case PINK:
                return BarColor.PINK;
            case BLUE:
                return BarColor.BLUE;
            case RED:
                return BarColor.RED;
            case GREEN:
                return BarColor.GREEN;
            case YELLOW:
                return BarColor.YELLOW;
            case PURPLE:
                return BarColor.PURPLE;
            case WHITE:
                return BarColor.WHITE;
            default:
                return convertColor(BossBarColor.defaultColor());
        }
    }
}
