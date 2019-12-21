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

package me.lucko.helper.reflect;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility methods for working with "versioned" server classes.
 *
 * <p>Internal classes within the Minecraft server and CraftBukkit are relocated at build time
 * to prevent developers from relying upon server internals. It is however sometimes useful to be
 * able to interact with these classes (via reflection).</p>
 */
public final class ServerReflection {

    /**
     * The nms prefix (without the version component)
     */
    public static final String NMS = "net.minecraft.server";

    /**
     * The obc prefix (without the version component)
     */
    public static final String OBC = "org.bukkit.craftbukkit";

    /**
     * The server's "nms" version
     */
    private static final String SERVER_VERSION;

    /**
     * The server's "nms" version
     */
    private static final NmsVersion NMS_VERSION;

    static {
        String serverVersion = "";
        // check we're dealing with a "CraftServer" and that the server isn't non-versioned.
        Class<?> server = Bukkit.getServer().getClass();
        if (server.getSimpleName().equals("CraftServer") && !server.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            String obcPackage = server.getPackage().getName();
            // check we're dealing with a craftbukkit implementation.
            if (obcPackage.startsWith("org.bukkit.craftbukkit.")) {
                // return the nms version.
                serverVersion = obcPackage.substring("org.bukkit.craftbukkit.".length());
            }
        }
        SERVER_VERSION = serverVersion;

        if (SERVER_VERSION.isEmpty()) {
            NMS_VERSION = NmsVersion.NONE;
        } else {
            NMS_VERSION = NmsVersion.valueOf(serverVersion);
        }
    }

    /**
     * Gets the server "nms" version.
     *
     * @return the server packaging version
     */
    @Nonnull
    public static String getServerVersion() {
        return SERVER_VERSION;
    }

    /**
     * Gets the server "nms" version.
     *
     * @return the server packaging version
     */
    @Nonnull
    public static NmsVersion getNmsVersion() {
        return NMS_VERSION;
    }

    /**
     * Prepends the versioned {@link #NMS} prefix to the given class name
     *
     * @param className the name of the class
     * @return the full class name
     */
    @Nonnull
    public static String nms(String className) {
        return NMS_VERSION.nms(className);
    }

    /**
     * Prepends the versioned {@link #NMS} prefix to the given class name
     *
     * @param className the name of the class
     * @return the class represented by the full class name
     */
    @Nonnull
    public static Class<?> nmsClass(String className) throws ClassNotFoundException {
        return NMS_VERSION.nmsClass(className);
    }

    /**
     * Prepends the versioned {@link #OBC} prefix to the given class name
     *
     * @param className the name of the class
     * @return the full class name
     */
    @Nonnull
    public static String obc(String className) {
        return NMS_VERSION.obc(className);
    }

    /**
     * Prepends the versioned {@link #OBC} prefix to the given class name
     *
     * @param className the name of the class
     * @return the class represented by the full class name
     */
    @Nonnull
    public static Class<?> obcClass(String className) throws ClassNotFoundException {
        return NMS_VERSION.obcClass(className);
    }

    /**
     * Gets a {@link Field} of a given {@link Class}.
     * This method wraps the {@link NoSuchFieldException} and the {@link SecurityException} into a {@link RuntimeException}.
     *
     * @param clazz the class to which the field belongs
     * @param name the name of the field
     * @return the {@link Field} with the supplied name of the supplied class
     * @see Class#getField(String)
     */
    public static Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getField(name);
        } catch (NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a declared {@link Field} of a given {@link Class}.
     * This method wraps the {@link NoSuchFieldException} and the {@link SecurityException} into a {@link RuntimeException},
     * and sets accessible the {@link Field}.
     *
     * @param clazz the class to which the field belongs
     * @param name the name of the field
     * @return the {@link Field} with the supplied name of the supplied class
     * @see Class#getDeclaredField(String)
     */
    public static Field getDeclaredField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the value of a {@link Field} of the supplied {@link Object}.
     * This method wraps the {@link NoSuchFieldException} and the {@link IllegalAccessException} into a {@link RuntimeException}.
     *
     * @param from the class to which the field belongs
     * @param obj the object to which set the field
     * @param fieldName the name of the field
     * @param newValue the new value to set
     * @param <T> the type of the class parameters
     * @see Field#set(Object, Object) 
     */
    public static <T> void setField(Class<T> from, Object obj, String fieldName, Object newValue) {
        try {
            Field f = from.getDeclaredField(fieldName);
            boolean accessible = f.isAccessible();
            f.setAccessible(true);
            f.set(obj, newValue);
            f.setAccessible(accessible);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a {@link Method} of a given {@link Class}.
     * This method wraps the {@link NoSuchMethodException} into a {@link RuntimeException}.
     *
     * @param clazz the class to which the method belongs
     * @param name the name of the method
     * @param args the {@link Class} list representing the arguments of the method
     * @return the {@link Method} with the specified properties
     * @see Class#getMethod(String, Class[])
     */
    public static Method getMethod(Class<?> clazz, String name, Class<?>... args) {
        try {
            return clazz.getMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets a declared {@link Method} of a given {@link Class}.
     * This method wraps the {@link NoSuchMethodException} into a {@link RuntimeException},
     * and sets accessible the returned {@link Method}.
     *
     * @param clazz the class to which the method belongs
     * @param name the name of the method
     * @param args the {@link Class} list representing the arguments of the method
     * @return the {@link Method} with the specified properties
     * @see Class#getMethod(String, Class[])
     */
    public static Method getDeclaredMethod(Class<?> clazz, String name, Class<?>... args) {
        try {
            Method method = clazz.getDeclaredMethod(name, args);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the returned value of the <em>getHandle()</em> method of a given {@link Object}.
     * This can be useful for NMS and OBC code.
     *
     * @param obj the {@link Object} from which to get the returned value
     * @return the returned value of the getHandle() method of the given {@link Object}
     */
    public static Object getHandle(Object obj) {
        try {
            return getDeclaredMethod(obj.getClass(), "getHandle", new Class[0]).invoke(obj);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the value of the field <em>playerConnection</em>
     * of the instance of the {@link Player} supplied.
     * This can be useful for NMS and OBC code.
     *
     * @param player the {@link Player} from which to get the <em>playerConnection</em> field value
     * @return the value of the field <em>playerConnection</em> of the supplied player's instance
     */
    public static Object getConnection(Player player) {
        try {
            Object craftPlayer = getHandle(player);
            return getField(craftPlayer.getClass(), "playerConnection").get(craftPlayer);
        } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a Minecraft packet to a list of players.
     * This can be useful for NMS and OBC code.
     *
     * @param packet the packet to send
     * @param players the players to whom send the packet
     */
    public static void sendPacket(Object packet, Player... players) {
        try {
            for (Player player : players) {
                Object connection = getConnection(player);
                getDeclaredMethod(connection.getClass(), "sendPacket", ServerReflection.nmsClass("Packet")).invoke(connection, packet);
            }
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private ServerReflection() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

}
