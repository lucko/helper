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

import me.lucko.helper.reflect.NmsVersion;
import me.lucko.helper.shadow.ShadowFactory;
import me.lucko.helper.shadow.model.Shadow;
import me.lucko.helper.shadow.model.ShadowClass;
import me.lucko.helper.shadow.model.ShadowMethod;
import me.lucko.helper.shadow.model.name.ObfuscatedName;
import me.lucko.helper.shadow.model.name.ObfuscationMapping;
import me.lucko.helper.shadow.model.transformer.NmsTransformer;

@ShadowClass(className = "NBTTagList", transformer = NmsTransformer.class)
public interface NBTTagList extends Shadow, NBTBase {

    static NBTTagList create() {
        return ShadowFactory.constructShadow(NBTTagList.class);
    }

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "add", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "add", version = NmsVersion.v1_8_R3)
    })
    void appendTag(NBTBase nbt);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "a", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "a", version = NmsVersion.v1_8_R3)
    })
    void setTag(int index, NBTBase nbt);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "remove", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "a", version = NmsVersion.v1_8_R3)
    })
    void removeTag(int index);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "i", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "g", version = NmsVersion.v1_8_R3)
    })
    NBTBase getTag(int index);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "get", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "get", version = NmsVersion.v1_8_R3)
    })
    NBTTagCompound getCompoundTag(int index);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "size", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "size", version = NmsVersion.v1_8_R3)
    })
    int size();

}
