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

package me.lucko.helper.module.utility;

import me.lucko.helper.module.AbstractModule;
import me.lucko.helper.module.DelegateModule;
import me.lucko.helper.module.Module;
import me.lucko.helper.module.TerminableModule;

import javax.annotation.Nonnull;

/**
 * Implements {@link TerminableModule} and delegates lifecycle calls to {@link Module}
 */
class DelegateTerminableModule<T extends Module> extends AbstractModule implements DelegateModule<T> {

    @Nonnull
    private final T delegate;

    DelegateTerminableModule(@Nonnull T delegate) {
        this.delegate = delegate;
    }

    @Override
    public void enable() {
        delegate.enable();
    }

    @Override
    public void disable() {
        delegate.disable();
    }

    @Override
    @Nonnull
    public T getDelegate() {
        return delegate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DelegateTerminableModule)) return false;
        final DelegateTerminableModule other = (DelegateTerminableModule) o;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + delegate.hashCode();
        return result;
    }

    public String toString() {
        return "DelegateTerminableModule(delegate=" + this.getDelegate() + ")";
    }
}
