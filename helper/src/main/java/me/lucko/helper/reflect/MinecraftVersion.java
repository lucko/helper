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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/**
 * Encapsulates a version of Minecraft.
 *
 * @author Kristian (ProtocolLib)
 */
public final class MinecraftVersion implements Comparable<MinecraftVersion> {

    public static final Comparator<MinecraftVersion> COMPARATOR = Comparator.nullsFirst(Comparator
            .comparingInt(MinecraftVersion::getMajor)
            .thenComparingInt(MinecraftVersion::getMinor)
            .thenComparingInt(MinecraftVersion::getBuild)
            .thenComparing(Comparator.nullsLast(Comparator.comparing(MinecraftVersion::getDevelopmentStage)))
            .thenComparing(Comparator.nullsFirst(Comparator.comparing(MinecraftVersion::getSnapshot)))
    );

    /**
     * The newest known version of Minecraft
     */
    private static final String NEWEST_MINECRAFT_VERSION = "1.20.1";

    /**
     * The date (with ISO 8601 or YYYY-MM-DD) when the most recent version was released.
     */
    private static final String MINECRAFT_LAST_RELEASE_DATE = "2023-06-07";

    /**
     * Gets the {@link MinecraftVersion} of the runtime server.
     *
     * @return the runtime minecraft version.
     */
    public static MinecraftVersion getRuntimeVersion() {
        return MinecraftVersions.RUNTIME_VERSION;
    }

    /**
     * Creates a new {@link MinecraftVersion} with the given properties.
     *
     * @param major the major component
     * @param minor the minor component
     * @param build the build component
     * @return a version instance
     */
    public static MinecraftVersion of(int major, int minor, int build) {
        return new MinecraftVersion(major, minor, build, null, null);
    }

    /**
     * Parses a {@link MinecraftVersion} from a version string, in the format
     * <code>major.minor.build</code>, or in the snapshot format.
     *
     * @param version the version in text form.
     * @throws IllegalArgumentException if unable to parse
     */
    public static MinecraftVersion parse(String version) throws IllegalArgumentException {
        return parse(version, true);
    }

    /**
     * Parses a {@link MinecraftVersion} from a version string, in the format
     * <code>major.minor.build</code>, or in the snapshot format.
     *
     * @param version the version in text form.
     * @param parseSnapshot if the implementation should try to parse a snapshot version
     * @throws IllegalArgumentException if unable to parse
     */
    public static MinecraftVersion parse(String version, boolean parseSnapshot) throws IllegalArgumentException {
        String[] parts = version.split("-");
        SnapshotVersion snapshot = null;
        int[] versionComponents = new int[3];

        try {
            versionComponents = parseVersion(parts[0]);
        } catch (NumberFormatException cause) {
            // Skip snapshot parsing
            if (!parseSnapshot)
                throw cause;

            try {
                // Determine if the snapshot is newer than the current release version
                snapshot = SnapshotVersion.parse(parts[0]);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                MinecraftVersion latest = MinecraftVersion.parse(NEWEST_MINECRAFT_VERSION, false);
                boolean newer = snapshot.getSnapshotDate().compareTo(format.parse(MINECRAFT_LAST_RELEASE_DATE)) > 0;

                versionComponents[0] = latest.getMajor();
                versionComponents[1] = latest.getMinor() + (newer ? 1 : -1);
                versionComponents[2] = 0;
            } catch (Exception e) {
                throw new IllegalArgumentException("Cannot parse " + parts[0], e);
            }
        }

        int major = versionComponents[0];
        int minor = versionComponents[1];
        int build = versionComponents[2];
        String development = parts.length > 1 ? parts[1] : (snapshot != null ? "snapshot" : null);
        return new MinecraftVersion(major, minor, build, development, snapshot);
    }

    private static int[] parseVersion(String version) {
        String[] elements = version.split("\\.");
        int[] numbers = new int[3];

        // Make sure it's even a valid version
        if (elements.length < 1) {
            throw new IllegalStateException("Corrupt MC version: " + version);
        }

        // The String 1 or 1.2 is interpreted as 1.0.0 and 1.2.0 respectively.
        for (int i = 0; i < Math.min(numbers.length, elements.length); i++) {
            numbers[i] = Integer.parseInt(elements[i].trim());
        }
        return numbers;
    }


    private final int major;
    private final int minor;
    private final int build;

    // The development stage
    @Nullable
    private final String development;

    // Snapshot?
    @Nullable
    private final SnapshotVersion snapshot;

    /**
     * Construct a version object.
     *
     * @param major - major version number.
     * @param minor - minor version number.
     * @param build - build version number.
     * @param development - development stage.
     */
    private MinecraftVersion(int major, int minor, int build, @Nullable String development, @Nullable SnapshotVersion snapshot) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.development = development;
        this.snapshot = snapshot;
    }

    /**
     * Gets the major component of the version.
     *
     * @return the major component of the version
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * Gets the minor component of the version.
     *
     * @return the minor component of the version.
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * Gets the build component of the version.
     *
     * @return the build component of the version.
     */
    public int getBuild() {
        return this.build;
    }

    /**
     * Gets the development stage.
     *
     * @return the development stage, or null if this is a release.
     */
    @Nullable
    public String getDevelopmentStage() {
        return this.development;
    }

    /**
     * Gets the snapshot version, or null if this is a release.
     *
     * @return The snapshot version.
     */
    @Nullable
    public SnapshotVersion getSnapshot() {
        return this.snapshot;
    }

    /**
     * Gets if this version is a snapshot.
     *
     * @return if this version is a snapshot.
     */
    public boolean isSnapshot() {
        return this.snapshot != null;
    }

    /**
     * Gets the version String (major.minor.build) only.
     *
     * @return a normal version string.
     */
    @Nonnull
    public String getVersion() {
        if (getDevelopmentStage() == null) {
            return String.format("%s.%s.%s", getMajor(), getMinor(), getBuild());
        } else {
            return String.format("%s.%s.%s-%s%s", getMajor(), getMinor(), getBuild(),
                    getDevelopmentStage(), isSnapshot() ? this.snapshot : "");
        }
    }

    @Override
    public int compareTo(MinecraftVersion that) {
        return COMPARATOR.compare(this, that);
    }

    /**
     * Gets if this version was released after another version.
     *
     * @param other the other version
     * @return if this version was released after another version
     */
    public boolean isAfter(MinecraftVersion other) {
        return compareTo(other) > 0;
    }

    /**
     * Gets if this version was released after another version, or is equal to it.
     *
     * @param other the other version
     * @return if this version was released after another version, or is equal to it.
     */
    public boolean isAfterOrEq(MinecraftVersion other) {
        return compareTo(other) >= 0;
    }

    /**
     * Gets if this version was released before another version.
     *
     * @param other the other version
     * @return if this version was released before another version
     */
    public boolean isBefore(MinecraftVersion other) {
        return compareTo(other) < 0;
    }

    /**
     * Gets if this version was released before another version, or is equal to it.
     *
     * @param other the other version
     * @return if this version was released before another version, or is equal to it.
     */
    public boolean isBeforeOrEq(MinecraftVersion other) {
        return compareTo(other) <= 0;
    }

    /**
     * Gets if this version was released in the period between two other versions, or is equal
     * to either of them.
     *
     * @param o1 the first other version
     * @param o2 the second other version
     * @return if this version was released between the others
     */
    public boolean isBetween(MinecraftVersion o1, MinecraftVersion o2) {
        return (isAfterOrEq(o1) && isBeforeOrEq(o2)) || (isBeforeOrEq(o1) && isAfterOrEq(o2));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof MinecraftVersion)) return false;

        MinecraftVersion other = (MinecraftVersion) obj;
        return getMajor() == other.getMajor() &&
                getMinor() == other.getMinor() &&
                getBuild() == other.getBuild() &&
                Objects.equals(getDevelopmentStage(), other.getDevelopmentStage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMajor(), getMinor(), getBuild());
    }

    @Override
    public String toString() {
        // Convert to a String that we can parse back again
        return String.format("(MC: %s)", getVersion());
    }
}