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

package me.lucko.helper.messaging.reqresp;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;

import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.messaging.conversation.ConversationChannel;
import me.lucko.helper.messaging.conversation.ConversationReply;
import me.lucko.helper.messaging.conversation.ConversationReplyListener;
import me.lucko.helper.promise.Promise;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;

/**
 * Implements a {@link ReqRespChannel} using {@link ConversationChannel}s.
 *
 * @param <Req> the request type
 * @param <Resp> the response type
 */
public class SimpleReqRespChannel<Req, Resp> implements ReqRespChannel<Req, Resp> {
    private final ConversationChannel<ReqResMessage<Req>, ReqResMessage<Resp>> channel;

    public SimpleReqRespChannel(Messenger messenger, String name, TypeToken<Req> reqType, TypeToken<Resp> respType) {
        TypeToken<ReqResMessage<Req>> reqMsgType = new TypeToken<ReqResMessage<Req>>(){}.where(new TypeParameter<Req>(){}, reqType);
        TypeToken<ReqResMessage<Resp>> respMsgType = new TypeToken<ReqResMessage<Resp>>(){}.where(new TypeParameter<Resp>(){}, respType);
        this.channel = messenger.getConversationChannel(name, reqMsgType, respMsgType);
    }

    @Override
    public Promise<Resp> request(Req req) {
        ReqResMessage<Req> msg = new ReqResMessage<>(UUID.randomUUID(), req);
        Promise<Resp> promise = Promise.empty();
        this.channel.sendMessage(msg, new ConversationReplyListener<ReqResMessage<Resp>>() {
            @Nonnull
            @Override
            public RegistrationAction onReply(@Nonnull ReqResMessage<Resp> reply) {
                promise.supply(reply.getBody());
                return RegistrationAction.STOP_LISTENING;
            }

            @Override
            public void onTimeout(@Nonnull List<ReqResMessage<Resp>> replies) {
                promise.supplyException(new TimeoutException("Request timed out"));
            }
        }, 5, TimeUnit.SECONDS);
        return promise;
    }

    @Override
    public void responseHandler(ResponseHandler<Req, Resp> handler) {
        this.channel.newAgent((agent, message) -> {
            UUID id = message.getConversationId();
            Req req = message.getBody();

            Resp resp = handler.response(req);
            if (resp != null) {
                return ConversationReply.of(new ReqResMessage<>(id, resp));
            } else {
                return ConversationReply.noReply();
            }
        });
    }

    @Override
    public void asyncResponseHandler(AsyncResponseHandler<Req, Resp> handler) {
        this.channel.newAgent((agent, message) -> {
            UUID id = message.getConversationId();
            Req req = message.getBody();

            Promise<Resp> promise = handler.response(req);
            if (promise != null) {
                Promise<ReqResMessage<Resp>> composedPromise = promise.thenApplyAsync(resp -> resp == null ? null : new ReqResMessage<>(id, resp));
                return ConversationReply.ofPromise(composedPromise);
            } else {
                return ConversationReply.noReply();
            }
        });
    }

    @Override
    public void close() {
        this.channel.close();
    }
}
