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

package me.lucko.helper.js.bindings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import me.lucko.scriptcontroller.bindings.BindingsBuilder;
import me.lucko.scriptcontroller.bindings.BindingsSupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Some misc functions to help with using Java collections in JS
 */
public class GeneralScriptBindings implements BindingsSupplier {

    private static final Supplier<ArrayList> ARRAY_LIST = ArrayList::new;
    private static final Supplier<LinkedList> LINKED_LIST = LinkedList::new;
    private static final Supplier<HashSet> HASH_SET = HashSet::new;
    private static final Supplier<HashMap> HASH_MAP = HashMap::new;
    private static final Supplier<CopyOnWriteArrayList> COPY_ON_WRITE_ARRAY_LIST = CopyOnWriteArrayList::new;
    private static final Supplier<Set> CONCURRENT_HASH_SET = ConcurrentHashMap::newKeySet;
    private static final Supplier<ConcurrentHashMap> CONCURRENT_HASH_MAP = ConcurrentHashMap::new;

    private static final Function<Object[], ArrayList> LIST_OF = objects -> new ArrayList<>(Arrays.asList(objects));
    private static final Function<Object[], HashSet> SET_OF = objects -> new HashSet<>(Arrays.asList(objects));

    private static final Function<Object[], ImmutableList> IMMUTABLE_LIST_OF = ImmutableList::copyOf;
    private static final Function<Object[], ImmutableSet> IMMUTABLE_SET_OF = ImmutableSet::copyOf;

    private static final Function<String, UUID> PARSE_UUID = s -> {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    };

    public void accumulateTo(BindingsBuilder bindings) {
        // standard java collections
        bindings.put("newArrayList", ARRAY_LIST);
        bindings.put("newLinkedList", LINKED_LIST);
        bindings.put("newHashSet", HASH_SET);
        bindings.put("newHashMap", HASH_MAP);
        bindings.put("newCopyOnWriteArrayList", COPY_ON_WRITE_ARRAY_LIST);
        bindings.put("newConcurrentHashSet", CONCURRENT_HASH_SET);
        bindings.put("newConcurrentHashMap", CONCURRENT_HASH_MAP);

        bindings.put("listOf", LIST_OF);
        bindings.put("setOf", SET_OF);

        // guava immutables
        bindings.put("immutableListOf", IMMUTABLE_LIST_OF);
        bindings.put("immutableSetOf", IMMUTABLE_SET_OF);

        // misc
        bindings.put("parseUuid", PARSE_UUID);
    }
}
