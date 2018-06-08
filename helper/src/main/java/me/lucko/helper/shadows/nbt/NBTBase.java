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
import me.lucko.helper.reflect.NmsVersion;
import me.lucko.helper.shadow.model.Shadow;
import me.lucko.helper.shadow.model.ShadowClass;
import me.lucko.helper.shadow.model.ShadowField;
import me.lucko.helper.shadow.model.ShadowMethod;
import me.lucko.helper.shadow.model.Static;
import me.lucko.helper.shadow.model.name.ObfuscatedName;
import me.lucko.helper.shadow.model.name.ObfuscationMapping;
import me.lucko.helper.shadow.model.transformer.NmsTransformer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

@ShadowClass(className = "NBTBase", transformer = NmsTransformer.class)
public interface NBTBase extends Shadow {

    @Static
    @ShadowField
    @ObfuscatedName({
            @ObfuscationMapping(name = "a", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "a", version = NmsVersion.v1_8_R3)
    })
    String[] getTypes();

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "write", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "write", version = NmsVersion.v1_8_R3)
    })
    void write(DataOutput dataOutput) throws IOException;

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "load", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "load", version = NmsVersion.v1_8_R3)
    })
    void load(DataInput dataInput, int depth, NBTReadLimiter readLimiter) throws IOException;

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getTypeId", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getTypeId", version = NmsVersion.v1_8_R3)
    })
    byte getTypeId();

    default NBTTagType getType() {
        return NBTTagType.of(getTypeId());
    }

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "clone", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "clone", version = NmsVersion.v1_8_R3)
    })
    NBTBase copy();

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "isEmpty", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "isEmpty", version = NmsVersion.v1_8_R3)
    })
    boolean hasNoTags();

}
