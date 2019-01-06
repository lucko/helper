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

package me.lucko.helper.network.modules;

import me.lucko.helper.Commands;
import me.lucko.helper.network.Network;
import me.lucko.helper.profiles.Profile;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class FindCommandModule implements TerminableModule {
    private final Network network;
    private final String[] commandAliases;

    public FindCommandModule(Network network) {
        this(network, new String[]{"find"});
    }

    public FindCommandModule(Network network, String[] commandAliases) {
        this.network = network;
        this.commandAliases = commandAliases;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Commands.create()
                .assertPermission("helper.find")
                .handler(c -> {
                    String player = c.arg(0).parseOrFail(String.class).toLowerCase();

                    Map<String, List<Profile>> matches = this.network.getServers().entrySet().stream()
                            .sorted(Map.Entry.comparingByKey())
                            .collect(Collectors.toMap(
                                    Map.Entry::getKey,
                                    s -> s.getValue().getOnlinePlayers().values().stream()
                                            .filter(p -> p.getName().isPresent() && p.getName().get().toLowerCase().contains(player))
                                            .sorted(Comparator.comparing(p -> p.getName().get()))
                                            .collect(Collectors.toList()),
                                    (l1, l2) -> Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList()),
                                    LinkedHashMap::new
                            ));
                    matches.values().removeIf(Collection::isEmpty);

                    if (matches.isEmpty()) {
                        Players.msg(c.sender(), "&7[&anetwork&7] &7No players found matching '&f" + player + "&7'.");
                    } else {
                        Players.msg(c.sender(), "&7[&anetwork&7] &7Player search results for '&f" + player + "&7':");
                        matches.forEach((server, profiles) -> {
                            profiles.forEach(profile -> {
                                Players.msg(c.sender(), "&7[&anetwork&7] &f> &2" + profile.getName().orElse(profile.getUniqueId().toString()) + " &7connected to &2" + server);
                            });
                        });
                    }
                })
                .registerAndBind(consumer, this.commandAliases);
    }
}
