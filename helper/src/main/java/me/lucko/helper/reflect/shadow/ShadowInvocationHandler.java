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

package me.lucko.helper.reflect.shadow;

import me.lucko.helper.reflect.proxy.MoreMethodHandles;
import me.lucko.helper.reflect.shadow.model.Shadow;
import me.lucko.helper.reflect.shadow.model.ShadowField;
import me.lucko.helper.reflect.shadow.model.ShadowMethod;
import me.lucko.helper.reflect.shadow.model.Static;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.annotation.Nullable;

/**
 * Invocation handler for {@link Shadow}s.
 */
final class ShadowInvocationHandler implements InvocationHandler {
    private final ShadowFactory shadowFactory;

    private final ShadowDefinition shadow;

    @Nullable
    private final Object handle;

    ShadowInvocationHandler(ShadowFactory shadowFactory, ShadowDefinition shadow, @Nullable Object handle) {
        this.shadowFactory = shadowFactory;
        this.shadow = shadow;
        this.handle = handle;
    }

    @Override
    public Object invoke(Object shadowInstance, Method shadowMethod, Object[] args) throws Throwable {
        // implement methods in Shadow
        if (shadowMethod.getName().equals("getShadowTarget")) {
            return this.handle;
        }

        // just execute default methods on the proxy object itself
        if (shadowMethod.isDefault()) {
            Class<?> declaringClass = shadowMethod.getDeclaringClass();
            return MoreMethodHandles.privateLookupIn(declaringClass)
                    .unreflectSpecial(shadowMethod, declaringClass)
                    .bindTo(shadowInstance)
                    .invokeWithArguments(args);
        }

        ShadowMethod methodAnnotation = shadowMethod.getAnnotation(ShadowMethod.class);
        // also proxy the methods from Object, e.g. equals, hashCode and toString
        if (methodAnnotation != null || shadowMethod.getDeclaringClass() == Object.class) {
            Object[] unwrappedArguments = this.shadowFactory.unwrapShadows(args);
            Class[] unwrappedArgumentTypes = Arrays.stream(unwrappedArguments).map(Object::getClass).toArray(Class[]::new);

            MethodHandle targetMethod = this.shadow.findTargetMethod(shadowMethod, unwrappedArgumentTypes);

            Object handle = getHandle(shadowMethod);
            Object returnObject;

            if (handle == null) {
                returnObject = targetMethod.invokeWithArguments(unwrappedArguments);
            } else {
                returnObject = targetMethod.bindTo(handle).invokeWithArguments(unwrappedArguments);
            }

            if (returnObject == null) {
                return null;
            }

            if (shadowMethod.getName().equals("toString") && shadowMethod.getParameterCount() == 0) {
                return "Shadow(shadowClass=" + this.shadow.getShadowClass() + ", targetClass=" + this.shadow.getTargetClass() + ", target=" + returnObject + ")";
            }

            if (Shadow.class.isAssignableFrom(shadowMethod.getReturnType())) {
                //noinspection unchecked
                returnObject = this.shadowFactory.createShadowProxy((Class<? extends Shadow>) shadowMethod.getReturnType(), returnObject);
            }

            return returnObject;
        }

        ShadowField fieldAnnotation = shadowMethod.getAnnotation(ShadowField.class);
        if (fieldAnnotation != null) {
            FieldMethodHandle targetField = this.shadow.findTargetField(shadowMethod);

            if (args == null || args.length == 0) {
                // getter
                MethodHandle getter = targetField.getGetter();

                Object handle = getHandle(shadowMethod);
                Object value;

                if (handle == null) {
                    value = getter.invoke();
                } else {
                    value = getter.bindTo(handle).invoke();
                }

                if (Shadow.class.isAssignableFrom(shadowMethod.getReturnType())) {
                    //noinspection unchecked
                    value = this.shadowFactory.createShadowProxy((Class<? extends Shadow>) shadowMethod.getReturnType(), value);
                }
                return value;

            } else if (args.length == 1) {
                // setter
                MethodHandle setter = targetField.getSetter();

                Object handle = getHandle(shadowMethod);
                Object value = this.shadowFactory.unwrapShadow(args[0]);

                if (handle == null) {
                    setter.invokeWithArguments(value);
                } else {
                    setter.bindTo(handle).invokeWithArguments(value);

                }

                if (shadowMethod.getReturnType() == void.class) {
                    return null;
                } else {
                    // allow chaining
                    return this.handle;
                }

            } else {
                throw new IllegalStateException("Unable to determine accessor type (getter/setter) for " + this.shadow.getTargetClass().getName() + "#" + shadowMethod.getName());
            }
        }

        throw new RuntimeException("Shadow method " + shadowMethod + " is not marked with @ShadowMethod or @ShadowField");
    }

    @Nullable
    private Object getHandle(AnnotatedElement annotatedElement) {
        return annotatedElement.getAnnotation(Static.class) != null ? null : this.handle;
    }

}
