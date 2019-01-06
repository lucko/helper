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

package me.lucko.helper.lilypad.plugin;

import com.google.common.reflect.TypeToken;

import me.lucko.helper.Schedulers;
import me.lucko.helper.lilypad.LilyPad;
import me.lucko.helper.messaging.AbstractMessenger;
import me.lucko.helper.messaging.Channel;

import lilypad.client.connect.api.Connect;
import lilypad.client.connect.api.event.EventListener;
import lilypad.client.connect.api.event.MessageEvent;
import lilypad.client.connect.api.request.RequestException;
import lilypad.client.connect.api.request.impl.MessageRequest;
import lilypad.client.connect.api.request.impl.RedirectRequest;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

public class HelperLilyPad implements LilyPad {

    private final Connect connect;
    private final AbstractMessenger messenger;
    private final AtomicBoolean listening = new AtomicBoolean(false);

    public HelperLilyPad(@Nonnull Connect connect) {
        this.connect = connect;

        this.messenger = new AbstractMessenger(
                (channel, message) -> new MessageRequestTask(this.connect, channel, message).run(),
                channel -> {
                    if (this.listening.getAndSet(true)) {
                        return;
                    }
                    try {
                        this.connect.registerEvents(this);
                    } catch (Exception e) {
                        this.listening.set(false);
                    }
                },
                channel -> {}
        );
    }

    private static final class MessageRequestTask implements Runnable {
        private final Connect connect;
        private final String channel;
        private final byte[] message;
        private int attempts = 0;

        MessageRequestTask(Connect connect, String channel, byte[] message) {
            this.connect = connect;
            this.channel = channel;
            this.message = message;
        }

        @Override
        public void run() {
            this.attempts++;
            try {
                this.connect.request(new MessageRequest(Collections.emptyList(), this.channel, this.message));
            } catch (RequestException e) {
                if (e.getMessage().equals("Not open") || e.getMessage().equals("Not connected")) {
                    // attempt to resend later - try 3 times.
                    if (this.attempts < 3) {
                        Schedulers.async().runLater(this, 2, TimeUnit.SECONDS);
                    }
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    @EventListener
    public void onMessage(MessageEvent event) {
        String channel = event.getChannel();
        byte[] message = event.getMessage();
        this.messenger.registerIncomingMessage(channel, message);
    }

    @Override
    public void redirectPlayer(@Nonnull String serverId, @Nonnull String playerUsername) {
        try {
            this.connect.request(new RedirectRequest(serverId, playerUsername));
        } catch (RequestException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public Connect getConnect() {
        return this.connect;
    }

    @Nonnull
    @Override
    public String getId() {
        return this.connect.getSettings().getUsername();
    }

    @Nonnull
    @Override
    public Set<String> getGroups() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public <T> Channel<T> getChannel(@Nonnull String name, @Nonnull TypeToken<T> type) {
        return this.messenger.getChannel(name, type);
    }

}
