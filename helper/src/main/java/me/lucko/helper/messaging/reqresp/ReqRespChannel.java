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

import me.lucko.helper.messaging.Channel;
import me.lucko.helper.messaging.conversation.ConversationChannel;
import me.lucko.helper.promise.Promise;
import me.lucko.helper.terminable.Terminable;

/**
 * A generic request/response handler that can operate over the network.
 *
 * <p>This is a high-level interface, implemented in {@link SimpleReqRespChannel}
 * using lower-level {@link ConversationChannel}s and {@link Channel}s.</p>
 *
 * @param <Req> the request type
 * @param <Resp> the response type
 */
public interface ReqRespChannel<Req, Resp> extends Terminable {

    /**
     * Sends a request and returns a promise encapsulating the response.
     *
     * <p>The promise will complete exceptionally if a response is not received before the timeout
     * expires, by default after 5 seconds.</p>
     *
     * @param req the request object
     * @return a promise encapsulating the response
     */
    Promise<Resp> request(Req req);

    /**
     * Registers a response handler.
     *
     * @param handler the response handler
     */
    void responseHandler(ResponseHandler<Req, Resp> handler);

    /**
     * Registers a response handler that returns a Promise.
     *
     * @param handler the response handler
     */
    void asyncResponseHandler(AsyncResponseHandler<Req, Resp> handler);

    interface ResponseHandler<Req, Resp> {
        Resp response(Req req);
    }

    interface AsyncResponseHandler<Req, Resp> {
        Promise<Resp> response(Req req);
    }

}
