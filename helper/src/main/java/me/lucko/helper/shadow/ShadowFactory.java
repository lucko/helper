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

import me.lucko.helper.reflect.proxy.Proxies;
import me.lucko.helper.shadow.model.Shadow;
import me.lucko.helper.shadow.model.ShadowClass;
import me.lucko.helper.shadow.model.transformer.NmsTransformer;
import me.lucko.helper.shadow.model.transformer.ObcTransformer;
import me.lucko.helper.shadow.model.transformer.ShadowTransformer;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates instances of {@link Shadow} interfaces.
 */
public final class ShadowFactory {

    private static final ShadowFactory INSTANCE = new ShadowFactory();

    /**
     * Creates a shadow for the given object.
     *
     * @param shadowClass the class of the shadow definition
     * @param handle the handle object
     * @param <T> the shadow type
     * @return the shadow instance
     */
    public static <T extends Shadow> T shadow(Class<T> shadowClass, Object handle) {
        return INSTANCE.createShadowProxy(shadowClass, handle);
    }

    /**
     * Creates a static shadow for the given class.
     *
     * @param shadowClass the class of the shadow definition
     * @param <T> the shadow type
     * @return the shadow instance
     */
    public static <T extends Shadow> T staticShadow(Class<T> shadowClass) {
        return INSTANCE.createStaticShadowProxy(shadowClass);
    }

    /**
     * Creates a shadow for the given object, by invoking a constructor on the shadows
     * target.
     *
     * @param shadowClass the class of the shadow definition
     * @param arguments the arguments to pass to the constructor
     * @param <T> the shadow type
     * @return the shadow instance
     */
    public static <T extends Shadow> T constructShadow(Class<T> shadowClass, Object... arguments) {
        return INSTANCE.constructShadowInstance(shadowClass, arguments);
    }

    private final Map<Class<? extends Shadow>, ShadowDefinition> shadows = new ConcurrentHashMap<>();

    private ShadowFactory() {

    }

    public <T extends Shadow> T createShadowProxy(Class<T> shadowClass, Object handle) {
        Objects.requireNonNull(shadowClass, "shadowClass");
        Objects.requireNonNull(handle, "handle");

        // register the shadow first
        ShadowDefinition shadowDefinition = registerShadow(shadowClass);

        // ensure the target class of the shadow is assignable from the handle class
        Class<?> targetClass = this.shadows.get(shadowClass).getTargetClass();
        if (!targetClass.isAssignableFrom(handle.getClass())) {
            throw new IllegalArgumentException("Target class " + targetClass.getName() + " is not assignable from handle class " + handle.getClass().getName());
        }

        // return a proxy instance
        return Proxies.create(shadowClass, new ShadowInvocationHandler(this, shadowDefinition, handle));
    }

    public <T extends Shadow> T createStaticShadowProxy(Class<T> shadowClass) {
        Objects.requireNonNull(shadowClass, "shadowClass");

        // register the shadow first
        ShadowDefinition shadowDefinition = registerShadow(shadowClass);

        // return a proxy instance
        return Proxies.create(shadowClass, new ShadowInvocationHandler(this, shadowDefinition, null));
    }

    public <T extends Shadow> T constructShadowInstance(Class<T> shadowClass, Object... args) {
        Objects.requireNonNull(shadowClass, "shadowClass");

        // register the shadow first
        ShadowDefinition shadowDefinition = registerShadow(shadowClass);

        Object[] unwrappedArguments = unwrapShadows(args);
        Class[] unwrappedArgumentTypes = Arrays.stream(unwrappedArguments).map(Object::getClass).toArray(Class[]::new);

        MethodHandle targetConstructor = shadowDefinition.findTargetConstructor(unwrappedArgumentTypes);

        Object newInstance;
        try {
            newInstance = targetConstructor.invokeWithArguments(unwrappedArguments);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

        // create a shadow for the new instance
        return createShadowProxy(shadowClass, newInstance);
    }

    private ShadowDefinition registerShadow(Class<? extends Shadow> c) {
        return this.shadows.computeIfAbsent(c, shadowClass -> {
            // work out the real class name
            ShadowClass annotation = shadowClass.getAnnotation(ShadowClass.class);
            if (annotation == null) {
                throw new IllegalStateException("Shadow class " + shadowClass.getName() + " does not have a @ShadowClass annotation present.");
            }

            Class<? extends ShadowTransformer> transformerClass = annotation.transformer();
            ShadowTransformer transformer;

            if (transformerClass == ShadowTransformer.class) {
                transformer = null;
            } else if (transformerClass == NmsTransformer.class) {
                transformer = NmsTransformer.INSTANCE;
            } else if (transformerClass == ObcTransformer.class) {
                transformer = ObcTransformer.INSTANCE;
            } else {
                try {
                    transformer = transformerClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Unable to init transformer " + transformerClass.getName(), e);
                }
            }

            // apply transformer to the class name
            String targetClassName = annotation.className();
            if (transformer != null) {
                targetClassName = transformer.transformClassName(targetClassName);
            }

            Class<?> targetClass;
            try {
                targetClass = Class.forName(targetClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class " + targetClassName + " not found for shadow " + shadowClass.getName());
            }

            return new ShadowDefinition(shadowClass, targetClass);
        });
    }

    Object[] unwrapShadows(Object[] objects) {
        if (objects == null) {
            return new Object[0];
        }

        return Arrays.stream(objects).map(this::unwrapShadow).toArray(Object[]::new);
    }

    Object unwrapShadow(Object object) {
        if (object == null) {
            return null;
        }

        if (object instanceof Shadow) {
            //noinspection unchecked
            registerShadow((Class<? extends Shadow>) object.getClass());
            return ((Shadow) object).getShadowTarget();
        }

        return object;
    }

}
