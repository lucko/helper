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

package me.lucko.helper.network.event;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Called when a server disconnects from the network
 */
public class ServerDisconnectEvent implements NetworkEvent {
    private final String id;
    private final String reason;

    public ServerDisconnectEvent(String id) {
        this(id, null);
    }

    public ServerDisconnectEvent(String id, String reason) {
        this.id = Objects.requireNonNull(id, "id");
        this.reason = reason;
    }

    /**
     * Gets the id of the server that disconnected.
     *
     * @return the server id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Gets the reason for the disconnection.
     *
     * @return the reason, nullable
     */
    @Nullable
    public String getReason() {
        return this.reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerDisconnectEvent that = (ServerDisconnectEvent) o;
        return this.id.equals(that.id) &&
                Objects.equals(this.reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.reason);
    }

    @Override
    public String toString() {
        return "ServerDisconnectEvent{id=" + this.id + ", reason=" + this.reason + '}';
    }
}
