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

import me.lucko.helper.promise.Promise;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nonnull;

/**
 * A repository of profiles, which can get or lookup {@link Profile} instances
 * for given unique ids or names.
 *
 * <p>Methods which are prefixed with <b>get</b> perform a quick local search,
 * and return no result if no value is cached locally.</p>
 *
 * <p>Methods which are prefixed with <b>lookup</b> perform a more complete search,
 * usually querying an underlying database.</p>
 */
public interface ProfileRepository {

    /**
     * Gets a profile from this repository, using the unique id as the base for
     * the request.
     *
     * <p>If this repository does not contain a profile matching the unique id, a
     * profile will still be returned, but will not be populated with a name.</p>
     *
     * @param uniqueId the unique id to get a profile for
     * @return a profile for the uuid
     */
    @Nonnull
    Profile getProfile(@Nonnull UUID uniqueId);

    /**
     * Gets a profile from this repository, using the name as the base
     * for the request.
     *
     * <p>If this repository does not contain a profile matching the name, an
     * empty optional will be returned.</p>
     *
     * <p>In the case that there is more than one profile in the repository
     * matching the name, the most up-to-date record is returned.</p>
     *
     * @param name the name to get a profile for
     * @return a profile for the name
     */
    @Nonnull
    Optional<Profile> getProfile(@Nonnull String name);

    /**
     * Gets a collection of profiles known to the repository.
     *
     * <p>Returned profiles will always be populated with both a unique id
     * and a username.</p>
     *
     * @return a collection of known profiles
     */
    @Nonnull
    Collection<Profile> getKnownProfiles();

    /**
     * Populates a map of unique id to profile for the given iterable of unique ids.
     *
     * <p>The map will only contain an entry for each given unique id if there is a
     * corresponding profile for the unique id in the repository.</p>
     *
     * @param uniqueIds the unique ids to get profiles for
     * @return a map of uuid to profile, where possible, for each uuid in the iterable
     * @see #getProfile(UUID)
     */
    @Nonnull
    default Map<UUID, Profile> getProfiles(@Nonnull Iterable<UUID> uniqueIds) {
        Objects.requireNonNull(uniqueIds, "uniqueIds");
        Map<UUID, Profile> ret = new HashMap<>();
        for (UUID uniqueId : uniqueIds) {
            Profile profile = getProfile(uniqueId);
            if (profile.getName().isPresent()) {
                ret.put(uniqueId, profile);
            }
        }
        return ret;
    }

    /**
     * Populates a map of name to profile for the given iterable of names.
     *
     * <p>The map will only contain an entry for each given name if there is a
     * corresponding profile for the name in the repository.</p>
     *
     * @param names the names to get profiles for
     * @return a map of name to profile, where possible, for each name in the iterable
     * @see #getProfile(String)
     */
    @Nonnull
    default Map<String, Profile> getProfilesByName(@Nonnull Iterable<String> names) {
        Objects.requireNonNull(names, "names");
        Map<String, Profile> ret = new HashMap<>();
        for (String name : names) {
            getProfile(name).ifPresent(p -> ret.put(name, p));
        }
        return ret;
    }

    /**
     * Gets a profile from this repository, using the unique id as the base for
     * the request.
     *
     * <p>If this repository does not contain a profile matching the unique id, a
     * profile will still be returned, but will not be populated with a name.</p>
     *
     * @param uniqueId the unique id to get a profile for
     * @return a profile for the uuid
     */
    @Nonnull
    Promise<Profile> lookupProfile(@Nonnull UUID uniqueId);

    /**
     * Gets a profile from this repository, using the name as the base
     * for the request.
     *
     * <p>If this repository does not contain a profile matching the name, an
     * empty optional will be returned.</p>
     *
     * <p>In the case that there is more than one profile in the repository
     * matching the name, the most up-to-date record is returned.</p>
     *
     * @param name the name to get a profile for
     * @return a profile for the name
     */
    @Nonnull
    Promise<Optional<Profile>> lookupProfile(@Nonnull String name);

    /**
     * Gets a collection of profiles known to the repository.
     *
     * <p>Returned profiles will always be populated with both a unique id
     * and a username.</p>
     *
     * @return a collection of known profiles
     */
    @Nonnull
    Promise<Collection<Profile>> lookupKnownProfiles();

    /**
     * Populates a map of unique id to profile for the given iterable of unique ids.
     *
     * <p>The map will only contain an entry for each given unique id if there is a
     * corresponding profile for the unique id in the repository.</p>
     *
     * @param uniqueIds the unique ids to get profiles for
     * @return a map of uuid to profile, where possible, for each uuid in the iterable
     * @see #getProfile(UUID)
     */
    @Nonnull
    Promise<Map<UUID, Profile>> lookupProfiles(@Nonnull Iterable<UUID> uniqueIds);

    /**
     * Populates a map of name to profile for the given iterable of names.
     *
     * <p>The map will only contain an entry for each given name if there is a
     * corresponding profile for the name in the repository.</p>
     *
     * @param names the names to get profiles for
     * @return a map of name to profile, where possible, for each name in the iterable
     * @see #getProfile(String)
     */
    @Nonnull
    Promise<Map<String, Profile>> lookupProfilesByName(@Nonnull Iterable<String> names);

}
