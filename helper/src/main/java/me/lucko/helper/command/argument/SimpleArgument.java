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

package me.lucko.helper.command.argument;

import me.lucko.helper.utils.annotation.NonnullByDefault;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@NonnullByDefault
public class SimpleArgument implements Argument {
    protected final int index;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected final Optional<String> value;

    public SimpleArgument(int index, @Nullable String value) {
        this.index = index;
        this.value = Optional.ofNullable(value);
    }

    @Override
    public int index() {
        return this.index;
    }

    @Nonnull
    @Override
    public Optional<String> value() {
        return this.value;
    }

    @Override
    public boolean isPresent() {
        return this.value.isPresent();
    }
}
