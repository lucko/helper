/*
 * This file is part of SpongeAPI, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.lucko.helper.random;

import com.flowpowered.math.GenericMath;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;

/**
 * Represents a value which may vary randomly.
 */
@FunctionalInterface
public interface VariableAmount {

    /**
     * Creates a new 'fixed' variable amount, calls to {@link #getAmount} will
     * always return the fixed value.
     *
     * @param value The fixed value
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount fixed(double value) {
        return new Fixed(value);
    }

    /**
     * Creates a new variable amount which return values between the given min
     * (inclusive) and max (exclusive).
     *
     * @param min The minimum of the range (inclusive)
     * @param max The maximum of the range (exclusive)
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount range(double min, double max) {
        return new BaseAndAddition(min, fixed(max - min));
    }

    /**
     * Creates a new variable about which has a base and variance. The final
     * amount will be the base amount plus or minus a random amount between zero
     * (inclusive) and the variance (exclusive).
     *
     * @param base The base value
     * @param variance The variance
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithVariance(double base, double variance) {
        return new BaseAndVariance(base, fixed(variance));
    }

    /**
     * Creates a new variable about which has a base and variance. The final
     * amount will be the base amount plus or minus a random amount between zero
     * (inclusive) and the variance (exclusive).
     *
     * @param base The base value
     * @param variance The variance
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithVariance(double base, @Nonnull VariableAmount variance) {
        return new BaseAndVariance(base, variance);
    }

    /**
     * Creates a new variable amount which has a base and an additional amount.
     * The final amount will be the base amount plus a random amount between
     * zero (inclusive) and the additional amount (exclusive).
     *
     * @param base The base value
     * @param addition The additional amount
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithRandomAddition(double base, double addition) {
        return new BaseAndAddition(base, fixed(addition));
    }

    /**
     * Creates a new variable amount which has a base and an additional amount.
     * The final amount will be the base amount plus a random amount between
     * zero (inclusive) and the additional amount (exclusive).
     *
     * @param base The base value
     * @param addition The additional amount
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithRandomAddition(double base, @Nonnull VariableAmount addition) {
        return new BaseAndAddition(base, addition);
    }

    /**
     * Creates a new variable about which has a base and a chance to apply a
     * random variance. The chance should be between zero and one with a chance
     * of one signifying that the variance will always be applied. If the chance
     * succeeds then the final amount will be the base amount plus or minus a
     * random amount between zero (inclusive) and the variance (exclusive). If
     * the chance fails then the final amount will just be the base value.
     *
     * @param base The base value
     * @param variance The variance
     * @param chance The chance to apply the variance
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithOptionalVariance(double base, double variance, double chance) {
        return new OptionalAmount(base, chance, baseWithVariance(base, variance));
    }

    /**
     * Creates a new variable about which has a base and a chance to apply a
     * random variance. The chance should be between zero and one with a chance
     * of one signifying that the variance will always be applied. If the chance
     * succeeds then the final amount will be the base amount plus or minus a
     * random amount between zero (inclusive) and the variance (exclusive). If
     * the chance fails then the final amount will just be the base value.
     *
     * @param base The base value
     * @param variance The variance
     * @param chance The chance to apply the variance
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithOptionalVariance(double base, @Nonnull VariableAmount variance, double chance) {
        return new OptionalAmount(base, chance, baseWithVariance(base, variance));
    }

    /**
     * Creates a new variable about which has a base and a chance to apply a
     * random additional amount. The chance should be between zero and one with
     * a chance of one signifying that the additional amount will always be
     * applied. If the chance succeeds then the final amount will be the base
     * amount plus a random amount between zero (inclusive) and the additional
     * amount (exclusive). If the chance fails then the final amount will just
     * be the base value.
     *
     * @param base The base value
     * @param addition The additional amount
     * @param chance The chance to apply the additional amount
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithOptionalAddition(double base, double addition, double chance) {
        return new OptionalAmount(base, chance, baseWithRandomAddition(base, addition));
    }

    /**
     * Creates a new variable about which has a base and a chance to apply a
     * random additional amount. The chance should be between zero and one with
     * a chance of one signifying that the additional amount will always be
     * applied. If the chance succeeds then the final amount will be the base
     * amount plus a random amount between zero (inclusive) and the additional
     * amount (exclusive). If the chance fails then the final amount will just
     * be the base value.
     *
     * @param base The base value
     * @param addition The additional amount
     * @param chance The chance to apply the additional amount
     * @return A variable amount representation
     */
    @Nonnull
    static VariableAmount baseWithOptionalAddition(double base, @Nonnull VariableAmount addition, double chance) {
        return new OptionalAmount(base, chance, baseWithRandomAddition(base, addition));
    }

    /**
     * Gets an instance of the variable amount depending on the given random
     * object.
     *
     * @param random The random object
     * @return The amount
     */
    double getAmount(@Nonnull Random random);

    /**
     * Gets an instance of the variable amount using the thread's
     * {@link ThreadLocalRandom} instance.
     *
     * @return The amount
     */
    default double getAmount() {
        return getAmount(ThreadLocalRandom.current());
    }

    /**
     * Gets the amount as if from {@link #getAmount(Random)} but floored to the
     * nearest integer equivalent.
     *
     * @param random The random object
     * @return The floored amount
     */
    default int getFlooredAmount(@Nonnull Random random) {
        return GenericMath.floor(getAmount(random));
    }

    /**
     * Gets the amount as if from {@link #getAmount()} but floored to the
     * nearest integer equivalent.
     *
     * @return The floored amount
     */
    default int getFlooredAmount() {
        return GenericMath.floor(getAmount());
    }

    /**
     * Represents a fixed amount, calls to {@link #getAmount} will always return
     * the same fixed value.
     */
    final class Fixed implements VariableAmount {
        private final double amount;

        private Fixed(double amount) {
            this.amount = amount;
        }

        @Override
        public double getAmount(@Nonnull Random random) {
            return this.amount;
        }

        @Override
        public String toString() {
            return "VariableAmount.Fixed(amount=" + this.getAmount() + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof Fixed)) return false;
            final Fixed other = (Fixed) o;
            return Double.compare(this.amount, other.amount) == 0;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (int) (Double.doubleToLongBits(this.amount) >>> 32 ^ Double.doubleToLongBits(this.amount));
            return result;
        }
    }

    /**
     * Represents a base amount with a variance, the final amount will be the
     * base amount plus or minus a random amount between zero (inclusive) and
     * the variance (exclusive).
     */
    final class BaseAndVariance implements VariableAmount {
        private final double base;
        private final VariableAmount variance;

        private BaseAndVariance(double base, @Nonnull VariableAmount variance) {
            this.base = base;
            this.variance = variance;
        }

        @Override
        public double getAmount(@Nonnull Random random) {
            double var = this.variance.getAmount(random);
            return this.base + random.nextDouble() * var * 2 - var;
        }
        @Override
        public String toString() {
            return "VariableAmount.BaseAndVariance(base=" + this.base + ", variance=" + this.variance + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof BaseAndVariance)) return false;
            final BaseAndVariance other = (BaseAndVariance) o;
            return Double.compare(this.base, other.base) == 0 && this.variance.equals(other.variance);
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (int) (Double.doubleToLongBits(this.base) >>> 32 ^ Double.doubleToLongBits(this.base));
            result = result * PRIME + this.variance.hashCode();
            return result;
        }
    }

    /**
     * Represents a base amount with a random addition, the final amount will be
     * the base amount plus a random amount between zero (inclusive) and the
     * addition (exclusive).
     */
    final class BaseAndAddition implements VariableAmount {
        private final double base;
        private final VariableAmount addition;

        private BaseAndAddition(double base, VariableAmount addition) {
            this.base = base;
            this.addition = addition;
        }

        @Override
        public double getAmount(@Nonnull Random random) {
            return this.base + (random.nextDouble() * this.addition.getAmount(random));
        }

        @Override
        public String toString() {
            return "VariableAmount.BaseAndAddition(base=" + this.base + ", addition=" + this.addition + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof BaseAndAddition)) return false;
            final BaseAndAddition other = (BaseAndAddition) o;
            return Double.compare(this.base, other.base) == 0 && this.addition.equals(other.addition);
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (int) (Double.doubleToLongBits(this.base) >>> 32 ^ Double.doubleToLongBits(this.base));
            result = result * PRIME + this.addition.hashCode();
            return result;
        }
    }

    /**
     * Represents a variable amount which has a base and a chance of varying.
     * This wraps another {@link VariableAmount} which it refers to if the
     * chance succeeds.
     */
    final class OptionalAmount implements VariableAmount {
        private final double base;
        private final double chance;
        private final VariableAmount inner;

        OptionalAmount(double base, double chance, VariableAmount inner) {
            this.base = base;
            this.chance = chance;
            this.inner = inner;
        }

        @Override
        public double getAmount(@Nonnull Random random) {
            if (random.nextDouble() < this.chance) {
                return this.inner.getAmount(random);
            }
            return this.base;
        }

        @Override
        public String toString() {
            return "VariableAmount.OptionalAmount(base=" + this.base + ", chance=" + this.chance + ", inner=" + this.inner + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof OptionalAmount)) return false;
            final OptionalAmount other = (OptionalAmount) o;
            return Double.compare(this.base, other.base) == 0 && Double.compare(this.chance, other.chance) == 0 && this.inner.equals(other.inner);
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            result = result * PRIME + (int) (Double.doubleToLongBits(this.base) >>> 32 ^ Double.doubleToLongBits(this.base));
            result = result * PRIME + (int) (Double.doubleToLongBits(this.chance) >>> 32 ^ Double.doubleToLongBits(this.chance));
            result = result * PRIME + this.inner.hashCode();
            return result;
        }
    }

}