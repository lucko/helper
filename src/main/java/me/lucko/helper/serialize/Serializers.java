/*
 * Copyright (c) 2017 Lucko (Luck) <luck@lucko.me>
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Utility methods for converting ItemStacks and Inventories to and from JSON.
 */
public final class Serializers {
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();
    private static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().create();

    public static JsonElement serializeItemstack(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack");

        Map<String, Object> data = itemStack.serialize();
        return GSON.toJsonTree(data, MAP_TYPE);
    }

    public static ItemStack deserializeItemstack(JsonElement jsonElement) {
        Preconditions.checkNotNull(jsonElement, "jsonElement");

        Map<String, Object> data = GSON.fromJson(jsonElement, MAP_TYPE);
        return ItemStack.deserialize(data);
    }

    public static JsonPrimitive serializeItemstacks(ItemStack[] items) throws IllegalStateException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeInt(items.length);

                for (ItemStack item : items) {
                    dataOutput.writeObject(item);
                }

                return new JsonPrimitive(Base64Coder.encodeLines(outputStream.toByteArray()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static JsonPrimitive serializeInventory(Inventory inventory) throws IllegalStateException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeInt(inventory.getSize());

                for (int i = 0; i < inventory.getSize(); i++) {
                    dataOutput.writeObject(inventory.getItem(i));
                }

                return new JsonPrimitive(Base64Coder.encodeLines(outputStream.toByteArray()));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public static ItemStack[] deserializeItemstacks(JsonElement data) throws IOException {
        Preconditions.checkArgument(data.isJsonPrimitive());
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data.getAsString()))) {
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                ItemStack[] items = new ItemStack[dataInput.readInt()];

                for (int i = 0; i < items.length; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }

                return items;
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    public static Inventory deserializeInventory(JsonElement data, String title) throws IOException {
        Preconditions.checkArgument(data.isJsonPrimitive());
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data.getAsString()))) {
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt(), title);

                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, (ItemStack) dataInput.readObject());
                }

                return inventory;
            }
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }

    private Serializers() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
