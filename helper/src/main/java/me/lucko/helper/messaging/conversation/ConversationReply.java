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

package me.lucko.helper.messaging.conversation;

import me.lucko.helper.promise.Promise;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

/**
 * Encapsulates the reply to a incoming message in a {@link ConversationChannel}.
 *
 * @param <R> the reply type
 */
public final class ConversationReply<R extends ConversationMessage> {
    private static final ConversationReply<?> NO_REPLY = new ConversationReply<>(null);

    /**
     * Returns an object indicating that no reply should be sent.
     *
     * @param <R> the reply type
     * @return a "no reply" marker
     */
    public static <R extends ConversationMessage> ConversationReply<R> noReply() {
        //noinspection unchecked
        return (ConversationReply<R>) NO_REPLY;
    }

    /**
     * Creates a new {@link ConversationReply}.
     *
     * @param reply the reply message
     * @param <R> the type
     * @return the new reply encapsulation
     */
    public static <R extends ConversationMessage> ConversationReply<R> of(R reply) {
        Objects.requireNonNull(reply, "reply");
        return new ConversationReply<>(Promise.completed(reply));
    }

    /**
     * Creates a new {@link ConversationReply}.
     *
     * <p>Bear in mind the reply will only be send once the future completes. The timeout value on
     * "other other end" may need to take this into account.</p>
     *
     * @param futureReply the future reply
     * @param <R> the type
     * @return the new reply encapsulation
     */
    public static <R extends ConversationMessage> ConversationReply<R> ofCompletableFuture(CompletableFuture<R> futureReply) {
        Objects.requireNonNull(futureReply, "futureReply");
        return new ConversationReply<>(Promise.wrapFuture(futureReply));
    }

    /**
     * Creates a new {@link ConversationReply}.
     *
     * <p>Bear in mind the reply will only be send once the future completes. The timeout value on
     * "other other end" may need to take this into account.</p>
     *
     * @param promiseReply the future reply
     * @param <R> the type
     * @return the new reply encapsulation
     */
    public static <R extends ConversationMessage> ConversationReply<R> ofPromise(Promise<R> promiseReply) {
        Objects.requireNonNull(promiseReply, "promiseReply");
        return new ConversationReply<>(promiseReply);
    }

    private final Promise<R> reply;

    private ConversationReply(Promise<R> reply) {
        this.reply = reply;
    }

    /**
     * Gets if this object actually contains a reply.
     *
     * @return if the object has a reply
     */
    public boolean hasReply() {
        return this.reply != null;
    }

    /**
     * Gets the reply.
     *
     * @return the reply
     * @throws IllegalStateException if this object doesn't {@link #hasReply() have a reply}
     */
    @Nonnull
    public Promise<R> getReply() {
        if (this.reply == null) {
            throw new IllegalStateException("No reply present");
        }
        return this.reply;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ConversationReply)) {
            return false;
        }

        ConversationReply<?> other = (ConversationReply<?>) obj;
        return Objects.equals(this.reply, other.reply);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.reply);
    }

    @Override
    public String toString() {
        return this.reply != null ? String.format("ConversationReply[%s]", this.reply) : "ConversationReply.noReply";
    }

}
