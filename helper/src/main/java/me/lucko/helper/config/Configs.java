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

package me.lucko.helper.config;

import org.yaml.snakeyaml.DumperOptions;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;

/**
 * Misc utilities for working with Configurate
 */
public final class Configs {

    @Nonnull
    public static YAMLConfigurationLoader yaml(@Nonnull Path path) {
        return YAMLConfigurationLoader.builder()
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .setIndent(2)
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }

    @Nonnull
    public static ConfigurationNode yamlLoad(@Nonnull Path path) {
        try {
            return yaml(path).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void yamlSave(@Nonnull Path path, @Nonnull ConfigurationNode node) {
        try {
            yaml(path).save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static YAMLConfigurationLoader yaml(@Nonnull File file) {
        Path path = file.toPath();
        return YAMLConfigurationLoader.builder()
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .setIndent(2)
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }

    @Nonnull
    public static ConfigurationNode yamlLoad(@Nonnull File file) {
        try {
            return yaml(file).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void yamlSave(@Nonnull File file, @Nonnull ConfigurationNode node) {
        try {
            yaml(file).save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static GsonConfigurationLoader gson(@Nonnull Path path) {
        return GsonConfigurationLoader.builder()
                .setIndent(2)
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }

    @Nonnull
    public static ConfigurationNode gsonLoad(@Nonnull Path path) {
        try {
            return gson(path).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void gsonSave(@Nonnull Path path, @Nonnull ConfigurationNode node) {
        try {
            gson(path).save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static GsonConfigurationLoader gson(@Nonnull File file) {
        Path path = file.toPath();
        return GsonConfigurationLoader.builder()
                .setIndent(2)
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }

    @Nonnull
    public static ConfigurationNode gsonLoad(@Nonnull File file) {
        try {
            return gson(file).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void gsonSave(@Nonnull File file, @Nonnull ConfigurationNode node) {
        try {
            gson(file).save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static HoconConfigurationLoader hocon(@Nonnull Path path) {
        return HoconConfigurationLoader.builder()
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }

    @Nonnull
    public static CommentedConfigurationNode hoconLoad(@Nonnull Path path) {
        try {
            return hocon(path).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void hoconSave(@Nonnull Path path, @Nonnull ConfigurationNode node) {
        try {
            hocon(path).save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static HoconConfigurationLoader hocon(@Nonnull File file) {
        Path path = file.toPath();
        return HoconConfigurationLoader.builder()
                .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8))
                .build();
    }

    @Nonnull
    public static CommentedConfigurationNode hoconLoad(@Nonnull File file) {
        try {
            return hocon(file).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void hoconSave(@Nonnull File file, @Nonnull ConfigurationNode node) {
        try {
            hocon(file).save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static <T> ObjectMapper<T> classMapper(@Nonnull Class<T> clazz) {
        try {
            return ObjectMapper.forClass(clazz);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static <T> ObjectMapper<T>.BoundInstance objectMapper(@Nonnull T object) {
        try {
            return ObjectMapper.forObject(object);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static <T> T generate(@Nonnull Class<T> clazz, @Nonnull ConfigurationNode node) {
        try {
            return ObjectMapper.forClass(clazz).bindToNew().populate(node);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    public static <T> T populate(@Nonnull T object, @Nonnull ConfigurationNode node) {
        try {
            return objectMapper(object).populate(node);
        } catch (ObjectMappingException e) {
            throw new RuntimeException(e);
        }
    }

    private Configs() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}
