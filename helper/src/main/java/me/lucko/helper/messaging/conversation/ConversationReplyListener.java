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

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Represents an object listening for replies sent on the conversation channel.
 *
 * @param <R> the reply type
 */
public interface ConversationReplyListener<R extends ConversationMessage> {

    static <R extends ConversationMessage> ConversationReplyListener<R> of(Function<? super R, RegistrationAction> onReply) {
        return new ConversationReplyListener<R>() {
            @Nonnull
            @Override
            public RegistrationAction onReply(@Nonnull R reply) {
                return onReply.apply(reply);
            }

            @Override
            public void onTimeout(@Nonnull List<R> replies) {

            }
        };
    }

    /**
     * Called when a message is posted to this listener.
     *
     * <p>This method is called asynchronously.</p>
     *
     * @param reply the reply message
     * @return the action to take
     */
    @Nonnull
    RegistrationAction onReply(@Nonnull R reply);

    /**
     * Called when the listener times out.
     *
     * <p>A listener times out if the "timeout wait period" passes before the listener is
     * unregistered by other means.</p>
     *
     * <p>"unregistered by other means" refers to the listener being stopped after a message was
     * passed to {@link #onReply(ConversationMessage)} and {@link RegistrationAction#STOP_LISTENING} being
     * returned.</p>
     *
     * @param replies the replies which have been received
     */
    void onTimeout(@Nonnull List<R> replies);

    /**
     * Defines the actions to take after receiving a reply in a {@link ConversationReplyListener}.
     */
    enum RegistrationAction {

        /**
         * Marks that the listener should continue listening for replies
         */
        CONTINUE_LISTENING,

        /**
         * Marks that the listener should stop listening for replies
         */
        STOP_LISTENING

    }
}
