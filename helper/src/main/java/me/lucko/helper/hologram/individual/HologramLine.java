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

package me.lucko.helper.hologram.individual;

import com.google.common.collect.ImmutableList;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a line in a hologram.
 */
public interface HologramLine {

    /**
     * Returns a new {@link HologramLine.Builder}.
     *
     * @return a new builder
     */
    static HologramLine.Builder builder() {
        return new Builder();
    }

    /**
     * Returns a hologram line that doesn't change between players.
     *
     * @param text the text to display
     * @return the line
     */
    @Nonnull
    static HologramLine fixed(@Nonnull String text) {
        return viewer -> text;
    }

    @Nonnull
    static HologramLine fromFunction(@Nonnull Function<Player, String> function) {
        return function::apply;
    }

    /**
     * Gets the string representation of the line, for the given player.
     *
     * @param viewer the player
     * @return the line
     */
    @Nonnull
    String resolve(Player viewer);

    final class Builder {
        private final ImmutableList.Builder<HologramLine> lines = ImmutableList.builder();

        private Builder() {

        }

        public Builder line(HologramLine line) {
            this.lines.add(line);
            return this;
        }

        public Builder lines(Iterable<? extends HologramLine> lines) {
            this.lines.addAll(lines);
            return this;
        }

        public Builder line(String line) {
            return line(HologramLine.fixed(line));
        }

        public Builder fromFunction(@Nonnull Function<Player, String> function) {
            return line(HologramLine.fromFunction(function));
        }

        public List<HologramLine> build() {
            return this.lines.build();
        }
    }

}
