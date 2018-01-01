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

package me.lucko.helper.serialize;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import me.lucko.helper.gson.JsonBuilder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Utility methods for converting ItemStacks and Inventories to and from JSON.
 */
public final class Serializers {

    public static JsonPrimitive serializeItemstack(ItemStack item) {
        return JsonBuilder.primitiveNonNull(InventorySerialization.encodeItemStackToString(item));
    }

    public static ItemStack deserializeItemstack(JsonElement data) {
        Preconditions.checkArgument(data.isJsonPrimitive());
        return InventorySerialization.decodeItemStack(data.getAsString());
    }

    public static JsonPrimitive serializeItemstacks(ItemStack[] items) {
        return JsonBuilder.primitiveNonNull(InventorySerialization.encodeItemStacksToString(items));
    }

    public static JsonPrimitive serializeInventory(Inventory inventory) {
        return JsonBuilder.primitiveNonNull(InventorySerialization.encodeInventoryToString(inventory));
    }

    public static ItemStack[] deserializeItemstacks(JsonElement data) {
        Preconditions.checkArgument(data.isJsonPrimitive());
        return InventorySerialization.decodeItemStacks(data.getAsString());
    }

    public static Inventory deserializeInventory(JsonElement data, String title) {
        Preconditions.checkArgument(data.isJsonPrimitive());
        return InventorySerialization.decodeInventory(data.getAsString(), title);
    }

    private Serializers() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
