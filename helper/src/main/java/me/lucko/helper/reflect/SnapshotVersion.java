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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

/**
 * Encapsulates a snapshot version of Minecraft.
 *
 * @author Kristian (ProtocolLib)
 */
public class SnapshotVersion implements Comparable<SnapshotVersion> {

    public static final Comparator<SnapshotVersion> COMPARATOR = Comparator.nullsFirst(Comparator
            .comparing(SnapshotVersion::getSnapshotDate)
            .thenComparing(SnapshotVersion::getSnapshotWeekVersion)
    );

    private static final Pattern SNAPSHOT_PATTERN = Pattern.compile("(\\d{2}w\\d{2})([a-z])");

    /**
     * Parses a snapshot version
     *
     * @param version the version string
     * @return the parsed version
     * @throws IllegalArgumentException if the version is not a snapshot version
     */
    @Nonnull
    public static SnapshotVersion parse(String version) throws IllegalArgumentException {
        return new SnapshotVersion(version);
    }

    private final Date snapshotDate;
    private final int snapshotWeekVersion;

    private transient String rawString;

    private SnapshotVersion(String version) {
        Matcher matcher = SNAPSHOT_PATTERN.matcher(version.trim());

        if (matcher.matches()) {
            try {
                this.snapshotDate = getDateFormat().parse(matcher.group(1));
                this.snapshotWeekVersion = matcher.group(2).charAt(0) - 'a';
                this.rawString = version;
            } catch (ParseException e) {
                throw new IllegalArgumentException("Date implied by snapshot version is invalid.", e);
            }
        } else {
            throw new IllegalArgumentException("Cannot parse " + version + " as a snapshot version.");
        }
    }

    /**
     * Retrieve the snapshot date parser.
     * <p>
     * We have to create a new instance of SimpleDateFormat every time as it is not thread safe.
     * @return The date formatter.
     */
    private static SimpleDateFormat getDateFormat() {
        SimpleDateFormat format = new SimpleDateFormat("yy'w'ww", Locale.US);
        format.setLenient(false);
        return format;
    }

    /**
     * Retrieve the snapshot version within a week, starting at zero.
     *
     * @return The weekly version
     */
    public int getSnapshotWeekVersion() {
        return this.snapshotWeekVersion;
    }

    /**
     * Retrieve the week this snapshot was released.
     *
     * @return The week.
     */
    public Date getSnapshotDate() {
        return this.snapshotDate;
    }

    /**
     * Retrieve the raw snapshot string (yy'w'ww[a-z]).
     *
     * @return The snapshot string.
     */
    public String getSnapshotString() {
        if (this.rawString == null) {
            // It's essential that we use the same locale
            Calendar current = Calendar.getInstance(Locale.US);
            current.setTime(this.snapshotDate);
            this.rawString = String.format("%02dw%02d%s",
                    current.get(Calendar.YEAR) % 100,
                    current.get(Calendar.WEEK_OF_YEAR),
                    (char) ('a' + this.snapshotWeekVersion));
        }
        return this.rawString;
    }

    @Override
    public int compareTo(SnapshotVersion that) {
        return COMPARATOR.compare(this, that);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SnapshotVersion)) return false;


        SnapshotVersion other = (SnapshotVersion) obj;
        return Objects.equals(this.snapshotDate, other.getSnapshotDate()) &&
                this.snapshotWeekVersion == other.getSnapshotWeekVersion();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.snapshotDate, this.snapshotWeekVersion);
    }

    @Override
    public String toString() {
        return getSnapshotString();
    }
}
