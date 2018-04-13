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

package me.lucko.helper.random;

import com.google.common.base.Preconditions;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Represents a {@link Weighted} object.
 *
 * @param <T> the object type
 */
public final class WeightedObject<T> implements Weighted {

    @Nonnull
    public static <T> WeightedObject<T> of(@Nonnull T object, double weight) {
        return new WeightedObject<>(object, weight);
    }

    private final T object;
    private final double weight;

    private WeightedObject(T object, double weight) {
        Preconditions.checkArgument(weight >= 0, "weight cannot be negative");
        this.object = Objects.requireNonNull(object, "object");
        this.weight = weight;
    }

    @Nonnull
    public T get() {
        return this.object;
    }

    @Override
    public double getWeight() {
        return this.weight;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof WeightedObject)) return false;
        final WeightedObject other = (WeightedObject) o;
        return this.object.equals(other.object) && Double.compare(this.getWeight(), other.getWeight()) == 0;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.object.hashCode();
        result = result * PRIME + (int) (Double.doubleToLongBits(this.getWeight()) >>> 32 ^ Double.doubleToLongBits(this.getWeight()));
        return result;
    }

    @Override
    public String toString() {
        return "WeightedObject(object=" + this.object + ", weight=" + this.getWeight() + ")";
    }
}
