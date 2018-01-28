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

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class InventorySerialization {

    public static byte[] encodeItemStack(ItemStack item) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeObject(item);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeItemStackToString(ItemStack item) {
        return Base64Util.encode(encodeItemStack(item));
    }

    public static ItemStack decodeItemStack(byte[] buf) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(buf)) {
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                return (ItemStack) dataInput.readObject();
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemStack decodeItemStack(String data) {
        return decodeItemStack(Base64Util.decode(data));
    }

    public static byte[] encodeItemStacks(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeInt(items.length);
                for (ItemStack item : items) {
                    dataOutput.writeObject(item);
                }
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeItemStacksToString(ItemStack[] items) {
        return Base64Util.encode(encodeItemStacks(items));
    }

    public static ItemStack[] decodeItemStacks(byte[] buf) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(buf)) {
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                ItemStack[] items = new ItemStack[dataInput.readInt()];
                for (int i = 0; i < items.length; i++) {
                    items[i] = (ItemStack) dataInput.readObject();
                }
                return items;
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ItemStack[] decodeItemStacks(String data) {
        return decodeItemStacks(Base64Util.decode(data));
    }

    public static byte[] encodeInventory(Inventory inventory) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
                dataOutput.writeInt(inventory.getSize());
                for (int i = 0; i < inventory.getSize(); i++) {
                    dataOutput.writeObject(inventory.getItem(i));
                }
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeInventoryToString(Inventory inventory) {
        return Base64Util.encode(encodeInventory(inventory));
    }

    public static Inventory decodeInventory(byte[] buf, String title) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(buf)) {
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
                Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt(), title);
                for (int i = 0; i < inventory.getSize(); i++) {
                    inventory.setItem(i, (ItemStack) dataInput.readObject());
                }
                return inventory;
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Inventory decodeInventory(String data, String title) {
        return decodeInventory(Base64Util.decode(data), title);
    }

    private InventorySerialization() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
