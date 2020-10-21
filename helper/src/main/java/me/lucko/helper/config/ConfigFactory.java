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

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

import me.lucko.helper.config.typeserializers.BukkitTypeSerializer;
import me.lucko.helper.config.typeserializers.ColoredStringTypeSerializer;
import me.lucko.helper.config.typeserializers.GsonTypeSerializer;
import me.lucko.helper.config.typeserializers.HelperTypeSerializer;
import me.lucko.helper.config.typeserializers.JsonTreeTypeSerializer;
import me.lucko.helper.config.typeserializers.Text3TypeSerializer;
import me.lucko.helper.config.typeserializers.TextTypeSerializer;
import me.lucko.helper.datatree.DataTree;
import me.lucko.helper.gson.GsonSerializable;

import net.kyori.text.Component;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.yaml.snakeyaml.DumperOptions;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
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
public abstract class ConfigFactory<N extends ConfigurationNode, L extends ConfigurationLoader<N>> {

    private static final ConfigFactory<ConfigurationNode, YAMLConfigurationLoader> YAML = new ConfigFactory<ConfigurationNode, YAMLConfigurationLoader>() {
        @Nonnull
        @Override
        public YAMLConfigurationLoader loader(@Nonnull Path path) {
            YAMLConfigurationLoader.Builder builder = YAMLConfigurationLoader.builder()
                    .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                    .setIndent(2)
                    .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                    .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8));

            builder.setDefaultOptions(builder.getDefaultOptions().withSerializers(TYPE_SERIALIZERS));
            return builder.build();
        }
    };

    private static final ConfigFactory<ConfigurationNode, GsonConfigurationLoader> GSON = new ConfigFactory<ConfigurationNode, GsonConfigurationLoader>() {
        @Nonnull
        @Override
        public GsonConfigurationLoader loader(@Nonnull Path path) {
            GsonConfigurationLoader.Builder builder = GsonConfigurationLoader.builder()
                    .setIndent(2)
                    .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                    .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8));

            builder.setDefaultOptions(builder.getDefaultOptions().withSerializers(TYPE_SERIALIZERS));
            return builder.build();
        }
    };

    private static final ConfigFactory<CommentedConfigurationNode, HoconConfigurationLoader> HOCON = new ConfigFactory<CommentedConfigurationNode, HoconConfigurationLoader>() {
        @Nonnull
        @Override
        public HoconConfigurationLoader loader(@Nonnull Path path) {
            HoconConfigurationLoader.Builder builder = HoconConfigurationLoader.builder()
                    .setSource(() -> Files.newBufferedReader(path, StandardCharsets.UTF_8))
                    .setSink(() -> Files.newBufferedWriter(path, StandardCharsets.UTF_8));

            builder.setDefaultOptions(builder.getDefaultOptions().withSerializers(TYPE_SERIALIZERS));
            return builder.build();
        }
    };

    private static final TypeSerializerCollection TYPE_SERIALIZERS;
    static {
        TypeSerializerCollection helperSerializers = TypeSerializerCollection.create();
        helperSerializers.register(TypeToken.of(JsonElement.class), GsonTypeSerializer.INSTANCE);
        helperSerializers.register(TypeToken.of(GsonSerializable.class), HelperTypeSerializer.INSTANCE);
        helperSerializers.register(TypeToken.of(ConfigurationSerializable.class), BukkitTypeSerializer.INSTANCE);
        helperSerializers.register(TypeToken.of(DataTree.class), JsonTreeTypeSerializer.INSTANCE);
        helperSerializers.register(TypeToken.of(String.class), ColoredStringTypeSerializer.INSTANCE);
        helperSerializers.register(TypeToken.of(me.lucko.helper.text.Component.class), TextTypeSerializer.INSTANCE);
        helperSerializers.register(TypeToken.of(Component.class), Text3TypeSerializer.INSTANCE);

        TYPE_SERIALIZERS = helperSerializers.newChild();
    }

    @Nonnull
    public static TypeSerializerCollection typeSerializers() {
        return TYPE_SERIALIZERS;
    }

    @Nonnull
    public static ConfigFactory<ConfigurationNode, YAMLConfigurationLoader> yaml() {
        return YAML;
    }

    @Nonnull
    public static ConfigFactory<ConfigurationNode, GsonConfigurationLoader> gson() {
        return GSON;
    }

    @Nonnull
    public static ConfigFactory<CommentedConfigurationNode, HoconConfigurationLoader> hocon() {
        return HOCON;
    }

    private ConfigFactory() {

    }

    @Nonnull
    public abstract L loader(@Nonnull Path path);

    @Nonnull
    public N load(@Nonnull Path path) {
        try {
            return loader(path).load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(@Nonnull Path path, @Nonnull ConfigurationNode node) {
        try {
            loader(path).save(node);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void load(@Nonnull Path path, T object) {
        try {
            L loader = loader(path);
            ObjectMapper<T>.BoundInstance mapper = objectMapper(object);

            if (!Files.exists(path)) {
                // create a new empty node
                N node = loader.createEmptyNode();
                // write the content of the object to the node
                mapper.serialize(node);
                // save the node
                loader.save(node);
            } else {
                // load the node from the file
                N node = loader.load();
                // populate the config object
                mapper.populate(node);
            }
        } catch (ObjectMappingException | IOException e) {
            throw new ConfigurationException(e);
        }
    }

    @Nonnull
    public L loader(@Nonnull File file) {
        return loader(file.toPath());
    }

    @Nonnull
    public N load(@Nonnull File file) {
        return load(file.toPath());
    }

    public void save(@Nonnull File file, @Nonnull ConfigurationNode node) {
        save(file.toPath(), node);
    }

    public <T> void load(@Nonnull File file, T object) {
        load(file.toPath(), object);
    }

    @Nonnull
    public static <T> ObjectMapper<T> classMapper(@Nonnull Class<T> clazz) {
        try {
            return ObjectMapper.forClass(clazz);
        } catch (ObjectMappingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Nonnull
    public static <T> ObjectMapper<T>.BoundInstance objectMapper(@Nonnull T object) {
        try {
            return ObjectMapper.forObject(object);
        } catch (ObjectMappingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Nonnull
    public static <T> T generate(@Nonnull Class<T> clazz, @Nonnull ConfigurationNode node) {
        try {
            return classMapper(clazz).bindToNew().populate(node);
        } catch (ObjectMappingException e) {
            throw new ConfigurationException(e);
        }
    }

    @Nonnull
    public static <T> T populate(@Nonnull T object, @Nonnull ConfigurationNode node) {
        try {
            return objectMapper(object).populate(node);
        } catch (ObjectMappingException e) {
            throw new ConfigurationException(e);
        }
    }
}
