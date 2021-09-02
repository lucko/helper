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

package me.lucko.helper.plugin.ap;

import org.bukkit.plugin.PluginLoadOrder;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;

/**
 * Annotation to automatically generate plugin.yml files for helper projects
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface Plugin {

    /**
     * The name of the plugin
     *
     * @return the name of the plugin
     */
    @Nonnull
    String name();

    /**
     * The plugin version
     *
     * @return the plugin version
     */
    @Nonnull
    String version() default "";

    /**
     * A description of the plugin
     *
     * @return a description of the plugin
     */
    @Nonnull
    String description() default "";

    /**
     * The load order of the plugin
     *
     * @return the load order of the plugin
     */
    @Nonnull
    PluginLoadOrder load() default PluginLoadOrder.POSTWORLD;

    /**
     * The api version of the plugin
     *
     * @return the api version of the plugin
     */
    String apiVersion() default "";

    /**
     * The authors of the plugin
     *
     * @return the author of the plugin
     */
    @Nonnull
    String[] authors() default {};

    /**
     * A website for the plugin
     *
     * @return a website for the plugin
     */
    @Nonnull
    String website() default "";

    /**
     * A list of dependencies for the plugin
     *
     * @return a list of dependencies for the plugin
     */
    @Nonnull
    PluginDependency[] depends() default {};

    /**
     * A list of hard dependencies for the plugin
     *
     * @return a list of hard dependencies for the plugin
     */
    @Nonnull
    String[] hardDepends() default {};

    /**
     * A list of soft dependencies for the plugin
     *
     * @return a list of soft dependencies for the plugin
     */
    @Nonnull
    String[] softDepends() default {};

    /**
     * A list of plugins which should be loaded after this plugin
     *
     * @return a list of plugins which should be loaded after this plugin
     */
    @Nonnull
    String[] loadBefore() default {};

    /**
     * Libraries from maven central which are loaded at runtime.
     */
    @Nonnull
    String[] libraries() default {};
}
