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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Utility class for handling storage file i/o.
 * Saves backups of the data files on each save.
 *
 * @param <T> the type being stored
 */
public abstract class FileStorageHandler<T> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

    public static <T> FileStorageHandler<T> build(String fileName, String fileExtension, File dataFolder, Function<Path, T> loadingFunc, BiConsumer<Path, T> savingFunc) {
        return new FileStorageHandler<T>(fileName, fileExtension, dataFolder) {

            @Override
            protected T readFromFile(Path path) {
                return loadingFunc.apply(path);
            }

            @Override
            protected void saveToFile(Path path, T t) {
                savingFunc.accept(path, t);
            }
        };
    }

    private final String fileName;
    private final String fileExtension;
    private final File dataFolder;

    public FileStorageHandler(String fileName, String fileExtension, File dataFolder) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.dataFolder = dataFolder;
    }

    protected abstract T readFromFile(Path path);

    protected abstract void saveToFile(Path path, T t);

    public Optional<T> load() {
        File file = new File(this.dataFolder, this.fileName + this.fileExtension);
        if (file.exists()) {
            return Optional.ofNullable(readFromFile(file.toPath()));
        } else {
            return Optional.empty();
        }
    }

    public void saveAndBackup(T data) {
        this.dataFolder.mkdirs();
        File file = new File(this.dataFolder, this.fileName + this.fileExtension);
        if (file.exists()) {
            File backupDir = new File(this.dataFolder, "backups");
            backupDir.mkdirs();

            File backupFile = new File(backupDir, this.fileName + "-" + DATE_FORMAT.format(new Date(System.currentTimeMillis())) + this.fileExtension);

            try {
                Files.move(file.toPath(), backupFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveToFile(file.toPath(), data);
    }

    public void save(T data) {
        this.dataFolder.mkdirs();
        File file = new File(this.dataFolder, this.fileName + this.fileExtension);
        if (file.exists()) {
            file.delete();
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        saveToFile(file.toPath(), data);
    }
}
