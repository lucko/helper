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

package me.lucko.helper.module;

import me.lucko.helper.terminable.Terminable;
import me.lucko.helper.terminable.registry.TerminableRegistry;

/**
 * A terminable extension of {@link Module}.
 *
 * <p>The end of the modules lifecycle is encapsulated by a {@link Terminable}, accessed via {@link #disableHandle()}.</p>
 *
 * <p>The modules terminable state is encapsulated by a {@link TerminableRegistry}, accessed via {@link #registry()}.</p>
 */
public interface TerminableModule extends Module {

    /**
     * Gets the {@link TerminableRegistry} in use by this {@link TerminableModule}.
     *
     * @return the registry
     */
    TerminableRegistry registry();

    /**
     * Gets a {@link Terminable}, which when {@link Terminable#terminate()}d,
     * will {@link #disable()} this {@link TerminableModule}.
     *
     * @return the disable handle
     */
    Terminable disableHandle();
    
}
