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

package me.lucko.helper.messaging.http;

import com.google.common.io.ByteStreams;
import com.google.common.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import me.lucko.helper.Schedulers;
import me.lucko.helper.messaging.AbstractMessenger;
import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.Messenger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Proof of concept {@link Messenger} implementation using the HTTP protocol.
 *
 * <p>Unlike the helper-redis implementation, this class can only facilitate the communication
 * between two clients at a time. However, unlike redis, the connection is direct.</p>
 *
 * <p>The messenger works by sending/receiving HTTP POST requests. It is a simple (not necessarily
 * the most efficient) implementation, which could be quite easily optimised if necessary - pooling
 * connections would be a start!</p>
 *
 * <p>Of course, a messenger using plain TCP sockets (optionally with a library like netty)
 * could work just as well.</p>
 */
public class HttpMessenger implements Messenger {

    /** The abstract messenger implementation used as the basis for Channel construction and handling */
    private final AbstractMessenger messenger;

    /** The http server used to handle incoming messages */
    private final HttpServer httpServer;
    /** A factory which creates remote URLs for outgoing messages */
    private final Function<String, URL> remoteUrl;

    public HttpMessenger(String host, int port, String remoteHost, int remotePort) {
        this.messenger = new AbstractMessenger(this::handleOutgoing, this::subscribe, this::unsubscribe);
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(host, port), 0);
            this.httpServer.setExecutor(Schedulers.async());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.remoteUrl = channel -> {
            try {
                return new URL("http", remoteHost, remotePort, channel);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private void handleOutgoing(String channel, byte[] message) {
        // Handle outgoing messages: just send a HTTP POST request to the remote server.
        try {
            HttpURLConnection connection = (HttpURLConnection) this.remoteUrl.apply(encodeChannel(channel)).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(false);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            connection.connect();
            try (OutputStream out = connection.getOutputStream()) {
                out.write(message);
            }

            if (connection.getResponseCode() >= 400) {
                throw new IOException("Response code: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Subscribe and unsubscribe from channels by creating the corresponding handler context on the server.
    private void subscribe(String channel) {
        this.httpServer.createContext(encodeChannel(channel), new Handler(channel));
    }

    private void unsubscribe(String channel) {
        this.httpServer.removeContext(encodeChannel(channel));
    }

    /**
     * Incoming handler for POST requests
     */
    private final class Handler implements HttpHandler {
        private final String channelName;

        private Handler(String channelName) {
            this.channelName = channelName;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if (!method.equals("POST")) {
                throw new UnsupportedEncodingException("Unsupported request: " + exchange.getRequestMethod());
            }

            byte[] message;
            try (InputStream in = exchange.getRequestBody()) {
                message = ByteStreams.toByteArray(in);
            }

            // forward to the abstract Messenger impl
            HttpMessenger.this.messenger.registerIncomingMessage(this.channelName, message);

            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
            exchange.getResponseBody().close();
        }
    }

    // Encode channel ids using the URL encoder
    private static String encodeChannel(String channel) {
        try {
            return '/' + URLEncoder.encode(channel, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    @Nonnull
    @Override
    public <T> Channel<T> getChannel(@Nonnull String name, @Nonnull TypeToken<T> type) {
        return this.messenger.getChannel(name, type);
    }

}
