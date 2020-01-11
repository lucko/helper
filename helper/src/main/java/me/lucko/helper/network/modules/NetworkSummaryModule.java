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
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.event.filter.EventFilters;
import me.lucko.helper.messaging.InstanceData;
import me.lucko.helper.network.Network;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.time.DurationFormatter;
import me.lucko.helper.time.Time;
import me.lucko.helper.utils.Players;
import me.lucko.helper.utils.Tps;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;
import java.util.Map;

import javax.annotation.Nonnull;

public class NetworkSummaryModule implements TerminableModule {
    private final Network network;
    private final InstanceData instanceData;
    private final String[] commandAliases;

    public NetworkSummaryModule(Network network, InstanceData instanceData) {
        this(network, instanceData, new String[]{"networksummary", "netsum"});
    }

    public NetworkSummaryModule(Network network, InstanceData instanceData, String[] commandAliases) {
        this.network = network;
        this.instanceData = instanceData;
        this.commandAliases = commandAliases;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Commands.create()
                .assertPermission("helper.networksummary")
                .handler(c -> sendSummary(c.sender()))
                .registerAndBind(consumer, this.commandAliases);

        Events.subscribe(PlayerJoinEvent.class, EventPriority.MONITOR)
                .filter(EventFilters.playerHasPermission("helper.networksummary.onjoin"))
                .handler(e -> Schedulers.sync().runLater(() -> sendSummary(e.getPlayer()), 1))
                .bindWith(consumer);
    }

    public void sendSummary(CommandSender sender) {
        Players.msg(sender, "&7[&anetwork&7] &f< Network summary >.");
        Players.msg(sender, "&7[&anetwork&7] &7" + this.network.getOverallPlayerCount() + " total players online.");
        Players.msg(sender, "&7[&anetwork&7]");

        this.network.getServers().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .forEach(server -> {
                    String id = (server.getId().equals(this.instanceData.getId()) ? "&a" : "&b") + server.getId();
                    if (!server.isOnline()) {
                        long lastPing = server.getLastPing();
                        if (lastPing == 0) {
                            return;
                        }

                        String lastSeen = DurationFormatter.CONCISE.format(Time.diffToNow(Instant.ofEpochMilli(lastPing)));
                        Players.msg(sender, "&7[&anetwork&7] " + id + " &7- last online " + lastSeen + " ago");
                    } else {
                        Tps tps = server.getMetadata("tps", Tps.class);
                        String tpsInfo = "";
                        if (tps != null) {
                            tpsInfo = " &7- " + tps.toFormattedString();
                        }
                        Players.msg(sender, "&7[&anetwork&7] " + id + " &7- &b" + server.getOnlinePlayers().size() + "&7/" + server.getMaxPlayers() + tpsInfo);
                    }
                });
    }
}
