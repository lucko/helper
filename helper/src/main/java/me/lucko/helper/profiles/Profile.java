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

package me.lucko.helper.profiles;

import org.bukkit.entity.HumanEntity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a Player's profile
 */
public interface Profile {

    /**
     * Creates a new profile instance
     *
     * @param uniqueId the unique id
     * @param name the username
     * @return the profile
     */
    @Nonnull
    static Profile create(@Nonnull UUID uniqueId, @Nullable String name) {
        return new SimpleProfile(uniqueId, name);
    }

    /**
     * Creates a new profile instance
     *
     * @param player the player to create a profile for
     * @return the profile
     */
    @Nonnull
    static Profile create(HumanEntity player) {
        return new SimpleProfile(player.getUniqueId(), player.getName());
    }

    /**
     * Gets the unique id associated with this profile
     *
     * @return the unique id
     */
    @Nonnull
    UUID getUniqueId();

    /**
     * Gets the username associated with this profile
     *
     * @return the username
     */
    @Nonnull
    Optional<String> getName();

    /**
     * Gets the timestamp when this Profile was created or last updated.
     *
     * <p>The returned value is a unix timestamp in milliseconds.</p>
     *
     * @return the profiles last update time
     */
    long getTimestamp();

}
