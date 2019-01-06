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

package me.lucko.helper.lilypad.extended;

import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.lilypad.LilyPad;
import me.lucko.helper.network.AbstractNetwork;
import me.lucko.helper.network.event.ServerDisconnectEvent;

import org.bukkit.event.server.PluginDisableEvent;

import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.request.impl.GetPlayersRequest;
import lilypad.client.connect.api.result.impl.GetPlayersResult;

import java.util.concurrent.TimeUnit;

class LilyPadNetworkImpl extends AbstractNetwork implements LilyPadNetwork {
    private int overallPlayerCount = 0;

    LilyPadNetworkImpl(LilyPad lilyPad) {
        super(lilyPad, lilyPad);

        // register a fallback disconnect listener
        Events.subscribe(PluginDisableEvent.class)
                .filter(e -> e.getPlugin().getName().equals("LilyPad-Connect"))
                .handler(e -> postEvent(new ServerDisconnectEvent(lilyPad.getId())));

        // cache overall player count
        Schedulers.builder()
                .async()
                .afterAndEvery(3, TimeUnit.SECONDS)
                .run(() -> {
                    try {
                        GetPlayersResult result = lilyPad.getConnect().request(new GetPlayersRequest()).await();
                        this.overallPlayerCount = result.getCurrentPlayers();
                    } catch (InterruptedException | RequestException e) {
                        e.printStackTrace();
                    }
                })
                .bindWith(this.compositeTerminable);
    }

    @Override
    public int getOverallPlayerCount() {
        return this.overallPlayerCount;
    }

}
