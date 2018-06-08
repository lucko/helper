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

import java.util.List;

@ShadowClass(className = "NBTTagIntArray", transformer = NmsTransformer.class)
public interface NBTTagIntArray extends Shadow, NBTBase {

    static NBTTagIntArray create(int[] data) {
        return ShadowFactory.constructShadow(NBTTagIntArray.class, (Object) data);
    }

    static NBTTagIntArray create(List<Integer> data) {
        return ShadowFactory.constructShadow(NBTTagIntArray.class, (Object) data);
    }

    @ShadowMethod
    @ObfuscatedName({
            @ObfuscationMapping(name = "d", version = NmsVersion.v1_12_R1),
            @ObfuscationMapping(name = "c", version = NmsVersion.v1_8_R3)
    })
    int[] getIntArray();

}
