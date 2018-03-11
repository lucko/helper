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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import me.lucko.helper.gson.GsonProvider;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Extension of {@link FileStorageHandler} implemented using Gson.
 *
 * @param <T> the type being stored
 */
public class GsonStorageHandler<T> extends FileStorageHandler<T> {
    protected final Type type;
    protected final Gson gson;

    public GsonStorageHandler(String fileName, String fileExtension, File dataFolder, Class<T> clazz) {
        this(fileName, fileExtension, dataFolder, TypeToken.of(clazz));
    }

    public GsonStorageHandler(String fileName, String fileExtension, File dataFolder, Class<T> clazz, Gson gson) {
        this(fileName, fileExtension, dataFolder, TypeToken.of(clazz), gson);
    }

    public GsonStorageHandler(String fileName, String fileExtension, File dataFolder, TypeToken<T> type) {
        this(fileName, fileExtension, dataFolder, type.getType());
    }

    public GsonStorageHandler(String fileName, String fileExtension, File dataFolder, TypeToken<T> type, Gson gson) {
        this(fileName, fileExtension, dataFolder, type.getType(), gson);
    }

    public GsonStorageHandler(String fileName, String fileExtension, File dataFolder, Type type) {
        this(fileName, fileExtension, dataFolder, type, GsonProvider.prettyPrinting());
    }

    public GsonStorageHandler(String fileName, String fileExtension, File dataFolder, Type type, Gson gson) {
        super(fileName, fileExtension, dataFolder);
        this.type = type;
        this.gson = gson;
    }

    @Override
    protected T readFromFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return this.gson.fromJson(reader, this.type);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void saveToFile(Path path, T t) {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            this.gson.toJson(t, this.type, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
