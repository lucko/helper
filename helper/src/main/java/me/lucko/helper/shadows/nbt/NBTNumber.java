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

import me.lucko.shadow.bukkit.Mapping;
import me.lucko.shadow.bukkit.ObfuscatedTarget;
import me.lucko.shadow.bukkit.PackageVersion;

public interface NBTNumber {

    @ObfuscatedTarget({
            @Mapping(value = "d", version = PackageVersion.v1_12_R1),
            @Mapping(value = "c", version = PackageVersion.v1_8_R3)
    })
    long asLong();

    @ObfuscatedTarget({
            @Mapping(value = "e", version = PackageVersion.v1_12_R1),
            @Mapping(value = "d", version = PackageVersion.v1_8_R3)
    })
    int asInt();

    @ObfuscatedTarget({
            @Mapping(value = "f", version = PackageVersion.v1_12_R1),
            @Mapping(value = "e", version = PackageVersion.v1_8_R3)
    })
    short asShort();

    @ObfuscatedTarget({
            @Mapping(value = "g", version = PackageVersion.v1_12_R1),
            @Mapping(value = "f", version = PackageVersion.v1_8_R3)
    })
    byte asByte();

    @ObfuscatedTarget({
            @Mapping(value = "asDouble", version = PackageVersion.v1_12_R1),
            @Mapping(value = "g", version = PackageVersion.v1_8_R3)
    })
    double asDouble();

    @ObfuscatedTarget({
            @Mapping(value = "i", version = PackageVersion.v1_12_R1),
            @Mapping(value = "h", version = PackageVersion.v1_8_R3)
    })
    float asFloat();

}
