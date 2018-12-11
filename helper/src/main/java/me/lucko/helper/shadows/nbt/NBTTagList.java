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

import me.lucko.shadow.Shadow;
import me.lucko.shadow.ShadowFactory;
import me.lucko.shadow.bukkit.Mapping;
import me.lucko.shadow.bukkit.NmsClassTarget;
import me.lucko.shadow.bukkit.ObfuscatedTarget;
import me.lucko.shadow.bukkit.PackageVersion;

@NmsClassTarget("NBTTagList")
public interface NBTTagList extends Shadow, NBTBase {

    static NBTTagList create() {
        return ShadowFactory.global().constructShadow(NBTTagList.class);
    }

    @ObfuscatedTarget({
            @Mapping(value = "add", version = PackageVersion.v1_12_R1),
            @Mapping(value = "add", version = PackageVersion.v1_8_R3)
    })
    void appendTag(NBTBase nbt);

    @ObfuscatedTarget({
            @Mapping(value = "a", version = PackageVersion.v1_12_R1),
            @Mapping(value = "a", version = PackageVersion.v1_8_R3)
    })
    void setTag(int index, NBTBase nbt);

    @ObfuscatedTarget({
            @Mapping(value = "remove", version = PackageVersion.v1_12_R1),
            @Mapping(value = "a", version = PackageVersion.v1_8_R3)
    })
    void removeTag(int index);

    @ObfuscatedTarget({
            @Mapping(value = "i", version = PackageVersion.v1_12_R1),
            @Mapping(value = "g", version = PackageVersion.v1_8_R3)
    })
    NBTBase getTag(int index);

    @ObfuscatedTarget({
            @Mapping(value = "get", version = PackageVersion.v1_12_R1),
            @Mapping(value = "get", version = PackageVersion.v1_8_R3)
    })
    NBTTagCompound getCompoundTag(int index);

    @ObfuscatedTarget({
            @Mapping(value = "size", version = PackageVersion.v1_12_R1),
            @Mapping(value = "size", version = PackageVersion.v1_8_R3)
    })
    int size();

}
