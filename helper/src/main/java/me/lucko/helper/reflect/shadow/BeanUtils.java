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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class BeanUtils {

    private BeanUtils() {}

    /**
     * <p>Find an accessible method that matches the given name and has compatible parameters.
     * Compatible parameters mean that every method parameter is assignable from
     * the given parameters.
     * In other words, it finds a method with the given name
     * that will take the parameters given.</p>
     *
     * <p>This method is slightly undeterministic since it loops
     * through methods names and return the first matching method.</p>
     *
     * <p>This method can match primitive parameter by passing in wrapper classes.
     * For example, a <code>Boolean</code> will match a primitive <code>boolean</code>
     * parameter.
     *
     * @param clazz find method in this class
     * @param methodName find method with this name
     * @param parameterTypes find method with compatible parameters
     * @return The accessible method
     */
    public static Method getMatchingMethod(
            final Class<?> clazz,
            final String methodName,
            final Class<?>[] parameterTypes) {

        // see if we can find the method directly
        // most of the time this works and it's much faster
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            return method;

        } catch (NoSuchMethodException e) {
            // ignore
        }

        // search through all methods
        final int paramSize = parameterTypes.length;
        Method bestMatch = null;
        float bestMatchCost = Float.MAX_VALUE;

        for (final Method method : clazz.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            // compare parameters
            final Class<?>[] methodsParams = method.getParameterTypes();
            final int methodParamSize = methodsParams.length;
            if (methodParamSize != paramSize) {
                continue;
            }

            boolean match = true;
            for (int n = 0 ; n < methodParamSize; n++) {
                if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
                    match = false;
                    break;
                }
            }

            if (!match) {
                continue;
            }

            float myCost = getTotalTransformationCost(parameterTypes,method.getParameterTypes());
            if (myCost < bestMatchCost ) {
                bestMatch = method;
                bestMatchCost = myCost;
            }
        }

        if (bestMatch == null && clazz.getSuperclass() != null) {
            bestMatch = getMatchingMethod(clazz.getSuperclass(), methodName, parameterTypes);
        }

        if (bestMatch == null && clazz.getInterfaces() != null) {
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> i : interfaces) {
                bestMatch = getMatchingMethod(i, methodName, parameterTypes);
                if (bestMatch != null) {
                    break;
                }
            }
        }

        return bestMatch;
    }

    public static Constructor<?> getMatchingConstructor(
            final Class<?> clazz,
            final Class<?>[] parameterTypes) {

        // see if we can find the method directly
        // most of the time this works and it's much faster
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return constructor;
        } catch (NoSuchMethodException e) {
            // ignore
        }

        // search through all methods
        final int paramSize = parameterTypes.length;
        Constructor<?> bestMatch = null;
        float bestMatchCost = Float.MAX_VALUE;

        for (final Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            // compare parameters
            final Class<?>[] methodsParams = constructor.getParameterTypes();
            final int methodParamSize = methodsParams.length;
            if (methodParamSize != paramSize) {
                continue;
            }

            boolean match = true;
            for (int n = 0 ; n < methodParamSize; n++) {
                if (!isAssignmentCompatible(methodsParams[n], parameterTypes[n])) {
                    match = false;
                    break;
                }
            }

            if (!match) {
                continue;
            }

            float myCost = getTotalTransformationCost(parameterTypes,constructor.getParameterTypes());
            if (myCost < bestMatchCost ) {
                bestMatch = constructor;
                bestMatchCost = myCost;
            }
        }

        return bestMatch;
    }

    /**
     * Returns the sum of the object transformation cost for each class in the source
     * argument list.
     * @param srcArgs The source arguments
     * @param destArgs The destination arguments
     * @return The total transformation cost
     */
    private static float getTotalTransformationCost(final Class<?>[] srcArgs, final Class<?>[] destArgs) {
        float totalCost = 0.0f;
        for (int i = 0; i < srcArgs.length; i++) {
            Class<?> srcClass, destClass;
            srcClass = srcArgs[i];
            destClass = destArgs[i];
            totalCost += getObjectTransformationCost(srcClass, destClass);
        }

        return totalCost;
    }

    /**
     * Gets the number of steps required needed to turn the source class into the
     * destination class. This represents the number of steps in the object hierarchy
     * graph.
     * @param srcClass The source class
     * @param destClass The destination class
     * @return The cost of transforming an object
     */
    private static float getObjectTransformationCost(Class<?> srcClass, final Class<?> destClass) {
        float cost = 0.0f;
        while (srcClass != null && !destClass.equals(srcClass)) {
            if (destClass.isPrimitive()) {
                final Class<?> destClassWrapperClazz = getPrimitiveWrapper(destClass);
                if (destClassWrapperClazz != null && destClassWrapperClazz.equals(srcClass)) {
                    cost += 0.25f;
                    break;
                }
            }
            if (destClass.isInterface() && isAssignmentCompatible(destClass,srcClass)) {
                // slight penalty for interface match.
                // we still want an exact match to override an interface match, but
                // an interface match should override anything where we have to get a
                // superclass.
                cost += 0.25f;
                break;
            }
            cost++;
            srcClass = srcClass.getSuperclass();
        }

        /*
         * If the destination class is null, we've travelled all the way up to
         * an Object match. We'll penalize this by adding 1.5 to the cost.
         */
        if (srcClass == null) {
            cost += 1.5f;
        }

        return cost;
    }


    /**
     * <p>Determine whether a type can be used as a parameter in a method invocation.
     * This method handles primitive conversions correctly.</p>
     *
     * <p>In order words, it will match a <code>Boolean</code> to a <code>boolean</code>,
     * a <code>Long</code> to a <code>long</code>,
     * a <code>Float</code> to a <code>float</code>,
     * a <code>Integer</code> to a <code>int</code>,
     * and a <code>Double</code> to a <code>double</code>.
     * Now logic widening matches are allowed.
     * For example, a <code>Long</code> will not match a <code>int</code>.
     *
     * @param parameterType the type of parameter accepted by the method
     * @param parameterization the type of parameter being tested
     *
     * @return true if the assignment is compatible.
     */
    private static boolean isAssignmentCompatible(final Class<?> parameterType, final Class<?> parameterization) {
        // try plain assignment
        if (parameterType.isAssignableFrom(parameterization)) {
            return true;
        }

        if (parameterType.isPrimitive()) {
            // this method does *not* do widening - you must specify exactly
            // is this the right behaviour?
            final Class<?> parameterWrapperClazz = getPrimitiveWrapper(parameterType);
            if (parameterWrapperClazz != null) {
                return parameterWrapperClazz.equals(parameterization);
            }
        }

        return false;
    }

    /**
     * Gets the wrapper object class for the given primitive type class.
     * For example, passing <code>boolean.class</code> returns <code>Boolean.class</code>
     * @param primitiveType the primitive type class for which a match is to be found
     * @return the wrapper type associated with the given primitive
     * or null if no match is found
     */
    private static Class<?> getPrimitiveWrapper(final Class<?> primitiveType) {
        // does anyone know a better strategy than comparing names?
        if (boolean.class.equals(primitiveType)) {
            return Boolean.class;
        } else if (float.class.equals(primitiveType)) {
            return Float.class;
        } else if (long.class.equals(primitiveType)) {
            return Long.class;
        } else if (int.class.equals(primitiveType)) {
            return Integer.class;
        } else if (short.class.equals(primitiveType)) {
            return Short.class;
        } else if (byte.class.equals(primitiveType)) {
            return Byte.class;
        } else if (double.class.equals(primitiveType)) {
            return Double.class;
        } else if (char.class.equals(primitiveType)) {
            return Character.class;
        } else {
            return null;
        }
    }

}