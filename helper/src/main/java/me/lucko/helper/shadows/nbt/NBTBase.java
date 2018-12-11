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

package me.lucko.helper.shadows.nbt;

import me.lucko.helper.nbt.NBTTagType;
import me.lucko.shadow.Field;
import me.lucko.shadow.Shadow;
import me.lucko.shadow.Static;
import me.lucko.shadow.bukkit.Mapping;
import me.lucko.shadow.bukkit.NmsClassTarget;
import me.lucko.shadow.bukkit.ObfuscatedTarget;
import me.lucko.shadow.bukkit.PackageVersion;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@NmsClassTarget("NBTBase")
public interface NBTBase extends Shadow {

    @Static
    @Field
    @ObfuscatedTarget({
            @Mapping(value = "a", version = PackageVersion.v1_12_R1),
            @Mapping(value = "a", version = PackageVersion.v1_8_R3)
    })
    String[] getTypes();

    @ObfuscatedTarget({
            @Mapping(value = "write", version = PackageVersion.v1_12_R1),
            @Mapping(value = "write", version = PackageVersion.v1_8_R3)
    })
    void write(DataOutput dataOutput) throws IOException;

    @ObfuscatedTarget({
            @Mapping(value = "load", version = PackageVersion.v1_12_R1),
            @Mapping(value = "load", version = PackageVersion.v1_8_R3)
    })
    void load(DataInput dataInput, int depth, NBTReadLimiter readLimiter) throws IOException;

    @ObfuscatedTarget({
            @Mapping(value = "getTypeId", version = PackageVersion.v1_12_R1),
            @Mapping(value = "getTypeId", version = PackageVersion.v1_8_R3)
    })
    byte getTypeId();

    default NBTTagType getType() {
        return NBTTagType.of(getTypeId());
    }

    @ObfuscatedTarget({
            @Mapping(value = "clone", version = PackageVersion.v1_12_R1),
            @Mapping(value = "clone", version = PackageVersion.v1_8_R3)
    })
    NBTBase copy();

    @ObfuscatedTarget({
            @Mapping(value = "isEmpty", version = PackageVersion.v1_12_R1),
            @Mapping(value = "isEmpty", version = PackageVersion.v1_8_R3)
    })
    boolean hasNoTags();

}
