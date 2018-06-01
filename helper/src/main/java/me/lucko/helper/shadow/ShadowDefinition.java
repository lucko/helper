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

package me.lucko.helper.shadow;

import me.lucko.helper.reflect.NmsVersion;
import me.lucko.helper.reflect.ServerReflection;
import me.lucko.helper.reflect.proxy.MoreMethodHandles;
import me.lucko.helper.shadow.model.Shadow;
import me.lucko.helper.shadow.model.name.Name;
import me.lucko.helper.shadow.model.name.ObfuscatedName;
import me.lucko.helper.shadow.model.name.ObfuscationMapping;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a processed {@link Shadow} definition.
 */
final class ShadowDefinition {
    private final Class<? extends Shadow> shadowClass;
    private final Class<?> targetClass;

    // caches
    private final Map<Method, MethodHandle> methods = new ConcurrentHashMap<>();
    private final Map<Method, FieldMethodHandle> fields = new ConcurrentHashMap<>();
    private final Map<Class[], MethodHandle> constructors = new ConcurrentHashMap<>();

    ShadowDefinition(Class<? extends Shadow> shadowClass, Class<?> targetClass) {
        this.shadowClass = shadowClass;
        this.targetClass = targetClass;
    }

    public Class<? extends Shadow> getShadowClass() {
        return this.shadowClass;
    }

    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    public MethodHandle findTargetMethod(Method shadowMethod, Class<?>[] argumentTypes) {
        return this.methods.computeIfAbsent(shadowMethod, m -> {
            String methodName = getMethodName(m);
            Method method = BeanUtils.getMatchingMethod(this.targetClass, methodName, argumentTypes);

            if (method == null) {
                throw new RuntimeException(new NoSuchMethodException(this.targetClass.getName() + "." + methodName));
            }

            if (!method.isAccessible()) {
                method.setAccessible(true);
            }

            return MoreMethodHandles.unreflect(method);
        });
    }

    public FieldMethodHandle findTargetField(Method shadowMethod) {
        return this.fields.computeIfAbsent(shadowMethod, m -> {
            String fieldName = getFieldName(m);

            Field field = null;
            Class searchClass = this.targetClass;
            do {
                try {
                    field = searchClass.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignored) {
                    searchClass = searchClass.getSuperclass();
                }
            } while (field == null && searchClass != Object.class);


            if (field == null) {
                throw new RuntimeException(new NoSuchFieldException(this.targetClass.getName() + "#" + fieldName));
            }

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            if (Modifier.isFinal(field.getModifiers())) {
                try {
                    Field modifierField = Field.class.getDeclaredField("modifiers");
                    modifierField.setAccessible(true);
                    modifierField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }

            try {
                return new FieldMethodHandle(field);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public MethodHandle findTargetConstructor(Class<?>[] argumentTypes) {
        return this.constructors.computeIfAbsent(argumentTypes, m -> {
            Constructor<?> constructor = BeanUtils.getMatchingConstructor(this.targetClass, argumentTypes);
            if (constructor == null) {
                throw new RuntimeException(new NoSuchMethodException(this.targetClass.getName() + ".<init>" + " - " + Arrays.toString(argumentTypes)));
            }

            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }

            return MoreMethodHandles.unreflect(constructor);
        });
    }

    private static String getMethodName(Method method) {
        Name name = method.getAnnotation(Name.class);
        if (name != null) {
            return name.value();
        }

        ObfuscatedName obfuscatedName = method.getAnnotation(ObfuscatedName.class);
        if (obfuscatedName != null) {
            NmsVersion nmsVersion = ServerReflection.getNmsVersion();
            for (ObfuscationMapping mapping : obfuscatedName.value()) {
                if (mapping.version() == nmsVersion) {
                    return mapping.name();
                }
            }
        }

        return method.getName();
    }

    private static final Pattern GET_PATTERN = Pattern.compile("(get)[A-Z].*");
    private static final Pattern GET_IS_PATTERN = Pattern.compile("(is)[A-Z].*");
    private static final Pattern SET_PATTERN = Pattern.compile("(set)[A-Z].*");

    private static String getFieldName(Method method) {
        Name name = method.getAnnotation(Name.class);
        if (name != null) {
            return name.value();
        }

        ObfuscatedName obfuscatedName = method.getAnnotation(ObfuscatedName.class);
        if (obfuscatedName != null) {
            NmsVersion nmsVersion = ServerReflection.getNmsVersion();
            for (ObfuscationMapping mapping : obfuscatedName.value()) {
                if (mapping.version() == nmsVersion) {
                    return mapping.name();
                }
            }
        }

        String methodName = method.getName();
        Matcher matcher = GET_PATTERN.matcher(methodName);
        if (matcher.matches()) {
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        }

        matcher = GET_IS_PATTERN.matcher(methodName);
        if (matcher.matches()) {
            return methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
        }

        matcher = SET_PATTERN.matcher(methodName);
        if (matcher.matches()) {
            return methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        }

        return methodName;
    }

}
