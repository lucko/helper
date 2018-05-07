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

package me.lucko.helper.config.typeserializers;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;

import me.lucko.helper.datatree.ConfigurateDataTree;
import me.lucko.helper.datatree.DataTree;
import me.lucko.helper.datatree.GsonDataTree;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

public final class JsonTreeTypeSerializer implements TypeSerializer<DataTree> {
    private static final TypeToken<JsonElement> JSON_ELEMENT_TYPE = TypeToken.of(JsonElement.class);

    public static final JsonTreeTypeSerializer INSTANCE = new JsonTreeTypeSerializer();

    @Override
    public DataTree deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
        return DataTree.from(node.getValue(JSON_ELEMENT_TYPE));
    }

    @Override
    public void serialize(TypeToken<?> typeToken, DataTree dataTree, ConfigurationNode node) throws ObjectMappingException {
        if (dataTree instanceof GsonDataTree) {
            node.setValue(JSON_ELEMENT_TYPE, ((GsonDataTree) dataTree).getElement());
        } else if (dataTree instanceof ConfigurateDataTree) {
            node.setValue(((ConfigurateDataTree) dataTree).getNode());
        } else {
            throw new ObjectMappingException("Unknown type: " + dataTree.getClass().getName());
        }
    }
}
