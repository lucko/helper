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

import javax.script.Bindings;

/**
 * Some misc functions to help with using Java collections in JS
 */
public final class GeneralScriptBindings {

    private static final Supplier<ArrayList> ARRAY_LIST = GeneralScriptBindings::newArrayList;
    private static final Supplier<LinkedList> LINKED_LIST = GeneralScriptBindings::newLinkedList;
    private static final Supplier<HashSet> HASH_SET = GeneralScriptBindings::newHashSet;
    private static final Supplier<HashMap> HASH_MAP = GeneralScriptBindings::newHashMap;
    private static final Supplier<CopyOnWriteArrayList> COPY_ON_WRITE_ARRAY_LIST = GeneralScriptBindings::newCopyOnWriteArrayList;
    private static final Supplier<Set> CONCURRENT_HASH_SET = GeneralScriptBindings::newConcurrentHashSet;
    private static final Supplier<ConcurrentHashMap> CONCURRENT_HASH_MAP = GeneralScriptBindings::newConcurrentHashMap;

    private static final Function<Object[], ArrayList> LIST_OF = GeneralScriptBindings::listOf;
    private static final Function<Object[], HashSet> SET_OF = GeneralScriptBindings::setOf;

    private static final Function<Object[], ImmutableList> IMMUTABLE_LIST_OF = GeneralScriptBindings::immutableListOf;
    private static final Function<Object[], ImmutableSet> IMMUTABLE_SET_OF = GeneralScriptBindings::immutableSetOf;

    private static final Function<String, UUID> PARSE_UUID = GeneralScriptBindings::parseUuid;

    public static void appendTo(Bindings bindings) {
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

    private static <T> ArrayList<T> newArrayList() { return new ArrayList<>(); }
    private static <T> LinkedList<T> newLinkedList() { return new LinkedList<>(); }
    private static <T> HashSet<T> newHashSet() { return new HashSet<>(); }
    private static <K, V> HashMap<K, V> newHashMap() { return new HashMap<>(); }
    private static <T> CopyOnWriteArrayList<T> newCopyOnWriteArrayList() { return new CopyOnWriteArrayList<>(); }
    private static <T> Set<T> newConcurrentHashSet() { return ConcurrentHashMap.newKeySet(); }
    private static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap() { return new ConcurrentHashMap<>(); }
    private static ArrayList listOf(Object[] objects) { return new ArrayList<>(Arrays.asList(objects)); }
    private static HashSet setOf(Object[] objects) { return new HashSet<>(Arrays.asList(objects)); }
    private static ImmutableList immutableListOf(Object[] objects) { return ImmutableList.copyOf(objects); }
    private static ImmutableSet immutableSetOf(Object[] objects) { return ImmutableSet.copyOf(objects); }
    private static UUID parseUuid(String s) {
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private GeneralScriptBindings() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
