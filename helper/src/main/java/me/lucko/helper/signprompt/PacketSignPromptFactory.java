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

package me.lucko.helper.signprompt;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.ComponentConverter;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import me.lucko.helper.Schedulers;
import me.lucko.helper.protocol.Protocol;
import me.lucko.helper.reflect.MinecraftVersion;
import me.lucko.helper.reflect.MinecraftVersions;
import me.lucko.helper.utils.Players;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Implementation of {@link SignPromptFactory} using ProtocolLib.
 */
public class PacketSignPromptFactory implements SignPromptFactory {

    @Override
    public void openPrompt(@Nonnull Player player, @Nonnull List<String> lines, @Nonnull ResponseHandler responseHandler) {
        Location location = player.getLocation().clone();
        location.setY(255);
        Players.sendBlockChange(player, location, Material.WALL_SIGN);

        BlockPosition position = new BlockPosition(location.toVector());
        PacketContainer writeToSign = new PacketContainer(PacketType.Play.Server.TILE_ENTITY_DATA);
        writeToSign.getBlockPositionModifier().write(0, position);
        writeToSign.getIntegers().write(0, 9);
        NbtCompound compound = NbtFactory.ofCompound("");

        for (int i = 0; i < 4; i++) {
            compound.put("Text" + (i + 1), "{\"text\":\"" + (lines.size() > i ? lines.get(i) : "") + "\"}");
        }

        compound.put("id", "minecraft:sign");
        compound.put("x", position.getX());
        compound.put("y", position.getY());
        compound.put("z", position.getZ());

        writeToSign.getNbtModifier().write(0, compound);
        Protocol.sendPacket(player, writeToSign);

        PacketContainer openSign = new PacketContainer(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        openSign.getBlockPositionModifier().write(0, position);
        Protocol.sendPacket(player, openSign);

        // we need to ensure that the callback is only called once.
        final AtomicBoolean active = new AtomicBoolean(true);

        Protocol.subscribe(PacketType.Play.Client.UPDATE_SIGN)
                .filter(e -> e.getPlayer().getUniqueId().equals(player.getUniqueId()))
                .biHandler((sub, event) -> {
                    if (!active.getAndSet(false)) {
                        return;
                    }

                    PacketContainer container = event.getPacket();

                    List<String> input = MinecraftVersion.getRuntimeVersion().isAfterOrEq(MinecraftVersions.v1_9)
                            ? new ArrayList<>(Arrays.asList(container.getStringArrays().read(0)))
                            : getInputForLegacyVersions(container);
                    Response response = responseHandler.handleResponse(input);

                    if (response == Response.TRY_AGAIN) {
                        // didn't pass, re-send the sign and request another input
                        Schedulers.sync().runLater(() -> {
                            if (player.isOnline()) {
                                openPrompt(player, lines, responseHandler);
                            }
                        }, 1L);
                    }

                    // cleanup this instance
                    sub.close();
                    Players.sendBlockChange(player, location, Material.AIR);
                });
    }

    private List<String> getInputForLegacyVersions(PacketContainer container) {
        WrappedChatComponent[] components = container.getChatComponentArrays().read(0);

        return Arrays.stream(components)
                .map(ComponentConverter::fromWrapper)
                .map(BaseComponent::toPlainText)
                .collect(Collectors.toList());
    }
}
