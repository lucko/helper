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
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Processes the {@link Plugin} annotation and generates a plugin.yml file.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"me.lucko.helper.plugin.ap.Plugin", "me.lucko.helper.plugin.ap.PluginDependency"})
public class PluginProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> annotatedElements = env.getElementsAnnotatedWith(Plugin.class);
        if (annotatedElements.isEmpty()) {
            return false;
        }

        if (annotatedElements.size() > 1) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "More than one @Plugin element found.");
            return false;
        }

        Element element = annotatedElements.iterator().next();

        if (!(element instanceof TypeElement)) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Plugin element is not instance of TypeElement");
            return false;
        }

        TypeElement type = ((TypeElement) element);
        Map<String, Object> data = new LinkedHashMap<>();
        Plugin annotation = type.getAnnotation(Plugin.class);

        data.put("name", annotation.name());

        String version = annotation.version();
        if (!version.isEmpty()) {
            data.put("version", version);
        } else {
            data.put("version", new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date(System.currentTimeMillis())));
        }

        data.put("main", type.getQualifiedName().toString());

        String description = annotation.description();
        if (!description.isEmpty()) {
            data.put("description", description);
        }

        PluginLoadOrder order = annotation.load();
        if (order != PluginLoadOrder.POSTWORLD) {
            data.put("load", order.name());
        }

        String apiVersion = annotation.apiVersion();
        if (!apiVersion.isEmpty()) {
            data.put("api-version", apiVersion);
        }

        String[] authors = annotation.authors();
        if (authors.length == 1) {
            data.put("author", authors[0]);
        } else if (authors.length > 1) {
            data.put("authors", new ArrayList<>(Arrays.asList(authors)));
        }

        String website = annotation.website();
        if (!website.isEmpty()) {
            data.put("website", website);
        }

        PluginDependency[] depends = annotation.depends();
        List<String> hard = new ArrayList<>();
        List<String> soft = new ArrayList<>();

        for (PluginDependency depend : depends) {
            if (depend.soft()) {
                soft.add(depend.value());
            } else {
                hard.add(depend.value());
            }
        }

        hard.addAll(Arrays.asList(annotation.hardDepends()));
        soft.addAll(Arrays.asList(annotation.softDepends()));

        if (!hard.isEmpty()) {
            data.put("depend", hard);
        }

        if (!soft.isEmpty()) {
            data.put("softdepend", soft);
        }

        String[] loadBefore = annotation.loadBefore();
        if (loadBefore.length != 0) {
            data.put("loadbefore", new ArrayList<>(Arrays.asList(loadBefore)));
        }

        String[] libraries = annotation.libraries();
        if (libraries.length != 0) {
            data.put("libraries", new ArrayList<>(Arrays.asList(libraries)));
        }

        try {
            Yaml yaml = new Yaml();
            FileObject resource = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml");

            try (Writer writer = resource.openWriter(); BufferedWriter bw = new BufferedWriter(writer)) {
                yaml.dump(data, bw);
                bw.flush();
            }

            return true;
        } catch (IOException e) {
            throw new RuntimeException("Cannot serialize plugin descriptor: " + e.getMessage(), e);
        }
    }

}
