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

import java.util.Set;

@ShadowClass(className = "NBTTagCompound", transformer = NmsTransformer.class)
public interface NBTTagCompound extends Shadow, NBTBase {

    static NBTTagCompound create() {
        return ShadowFactory.constructShadow(NBTTagCompound.class);
    }

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "c", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "c", version = NmsVersion.v1_8_R3)
    })
    Set<String> keySet();

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "d", version = NmsVersion.v1_12_R1),
            // Not present on 1.8.8
    })
    int size();

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "set", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "set", version = NmsVersion.v1_8_R3)
    })
    void setTag(String key, NBTBase value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "remove", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "remove", version = NmsVersion.v1_8_R3)
    })
    void removeTag(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setByte", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setByte", version = NmsVersion.v1_8_R3)
    })
    void setByte(String key, byte value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setShort", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setShort", version = NmsVersion.v1_8_R3)
    })
    void setShort(String key, short value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setInt", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setInt", version = NmsVersion.v1_8_R3)
    })
    void setInteger(String key, int value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setLong", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setLong", version = NmsVersion.v1_8_R3)
    })
    void setLong(String key, long value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setFloat", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setFloat", version = NmsVersion.v1_8_R3)
    })
    void setFloat(String key, float value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setDouble", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setDouble", version = NmsVersion.v1_8_R3)
    })
    void setDouble(String key, double value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setString", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setString", version = NmsVersion.v1_8_R3)
    })
    void setString(String key, String value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setByteArray", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setByteArray", version = NmsVersion.v1_8_R3)
    })
    void setByteArray(String key, byte[] value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setIntArray", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setIntArray", version = NmsVersion.v1_8_R3)
    })
    void setIntArray(String key, int[] value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "setBoolean", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "setBoolean", version = NmsVersion.v1_8_R3)
    })
    void setBoolean(String key, boolean value);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "get", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "get", version = NmsVersion.v1_8_R3)
    })
    NBTBase getTag(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "d", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "b", version = NmsVersion.v1_8_R3)
    })
    byte getTagId(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "hasKey", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "hasKey", version = NmsVersion.v1_8_R3)
    })
    boolean hasKey(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "hasKeyOfType", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "hasKeyOfType", version = NmsVersion.v1_8_R3)
    })
    boolean hasKey(String key, int type);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getByte", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getByte", version = NmsVersion.v1_8_R3)
    })
    byte getByte(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getShort", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getShort", version = NmsVersion.v1_8_R3)
    })
    short getShort(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getInt", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getInt", version = NmsVersion.v1_8_R3)
    })
    int getInteger(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getLong", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getLong", version = NmsVersion.v1_8_R3)
    })
    long getLong(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getFloat", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getFloat", version = NmsVersion.v1_8_R3)
    })
    float getFloat(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getDouble", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getDouble", version = NmsVersion.v1_8_R3)
    })
    double getDouble(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getString", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getString", version = NmsVersion.v1_8_R3)
    })
    String getString(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getByteArray", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getByteArray", version = NmsVersion.v1_8_R3)
    })
    byte[] getByteArray(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getIntArray", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getIntArray", version = NmsVersion.v1_8_R3)
    })
    int[] getIntArray(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getCompound", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getCompound", version = NmsVersion.v1_8_R3)
    })
    NBTTagCompound getCompoundTag(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getList", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getList", version = NmsVersion.v1_8_R3)
    })
    NBTTagList getTagList(String key, int type);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "getBoolean", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "getBoolean", version = NmsVersion.v1_8_R3)
    })
    boolean getBoolean(String key);

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "a", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "a", version = NmsVersion.v1_8_R3)
    })
    void merge(NBTTagCompound other);

}
