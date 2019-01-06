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
import me.lucko.helper.Helper;
import me.lucko.helper.Schedulers;
import me.lucko.helper.messaging.InstanceData;
import me.lucko.helper.messaging.Messenger;
import me.lucko.helper.messaging.conversation.ConversationChannel;
import me.lucko.helper.messaging.conversation.ConversationMessage;
import me.lucko.helper.messaging.conversation.ConversationReply;
import me.lucko.helper.messaging.conversation.ConversationReplyListener;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import me.lucko.helper.utils.Players;

import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class DispatchModule implements TerminableModule {
    private final Messenger messenger;
    private final InstanceData instanceData;
    private final String[] commandAliases;

    public DispatchModule(Messenger messenger, InstanceData instanceData) {
        this(messenger, instanceData, new String[]{"dispatch"});
    }

    public DispatchModule(Messenger messenger, InstanceData instanceData, String[] commandAliases) {
        this.messenger = messenger;
        this.instanceData = instanceData;
        this.commandAliases = commandAliases;
    }

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        ConversationChannel<DispatchMessage, DispatchReply> dispatchChannel = this.messenger.getConversationChannel("hlp-dispatch", DispatchMessage.class, DispatchReply.class);

        // listen for dispatches targeting this server
        dispatchChannel.newAgent((agent, message) -> {
            // notify players with the permission that the dispatch took place
            Players.stream()
                    .filter(p -> p.hasPermission("helper.dispatchalert"))
                    .filter(p -> !p.getUniqueId().equals(message.senderUuid))
                    .forEach(p -> Players.msg(p, "&7[&anetwork&7] &2" + message.senderName + "&7 on &2" + message.senderLocation + "&7 dispatched command '&f" + message.command + "&7' to '&2" + message.target + "&7'."));

            if (!(message.target.equals("all") || this.instanceData.getGroups().contains(message.target) || this.instanceData.getId().equals(message.target))) {
                return ConversationReply.noReply();
            }

            return ConversationReply.ofPromise(Schedulers.sync().supply(() -> {
                boolean success = false;
                try {
                    success = Helper.server().dispatchCommand(Helper.console(), message.command);
                } catch (CommandException e) {
                    // ignore
                }

                DispatchReply reply = new DispatchReply();
                reply.convoId = message.convoId;
                reply.server = this.instanceData.getId();
                reply.success = success;
                return reply;
            }));
        }).bindWith(consumer);

        Commands.create()
                .assertPermission("helper.dispatch")
                .assertUsage("<target> <command>")
                .handler(c -> {
                    String target = c.arg(0).parseOrFail(String.class).toLowerCase();
                    String command = c.args().stream().skip(1).collect(Collectors.joining(" "));

                    DispatchMessage dispatch = new DispatchMessage();
                    dispatch.convoId = UUID.randomUUID();
                    dispatch.command = command;
                    dispatch.target = target;
                    if (c.sender() instanceof Player) {
                        dispatch.senderUuid = ((Player) c.sender()).getUniqueId();
                    }
                    dispatch.senderName = c.sender().getName();
                    dispatch.senderLocation = this.instanceData.getId();

                    dispatchChannel.sendMessage(dispatch, new ConversationReplyListener<DispatchReply>() {
                        @Nonnull
                        @Override
                        public RegistrationAction onReply(@Nonnull DispatchReply reply) {
                            if (reply.success) {
                                Players.msg(c.sender(), "&7[&anetwork&7] Dispatched command '&f" + command + "&7' was &asuccessfully executed&7 on &2" + reply.server + "&7.");
                            } else {
                                Players.msg(c.sender(), "&7[&anetwork&7] Dispatched command '&f" + command + "&7' could &cnot be successfully executed&7 on &2" + reply.server + "&7.");
                            }
                            return RegistrationAction.CONTINUE_LISTENING;
                        }

                        @Override
                        public void onTimeout(@Nonnull List<DispatchReply> replies) {
                            if (replies.isEmpty()) {
                                Players.msg(c.sender(), "&7[&anetwork&7] Dispatched command '&f" + command + "&7' was not acknowledged by any servers.");
                            }
                        }
                    }, 3, TimeUnit.SECONDS);

                    Players.msg(c.sender(), "&7[&anetwork&7] Dispatched command '&f" + command + "&7' to '&2" + target + "&7'.");
                })
                .registerAndBind(consumer, this.commandAliases);
    }

    private static final class DispatchMessage implements ConversationMessage {
        private UUID convoId;

        private String command;
        private String target;
        private UUID senderUuid;
        private String senderName;
        private String senderLocation;

        @Nonnull
        @Override
        public UUID getConversationId() {
            return this.convoId;
        }
    }

    private static final class DispatchReply implements ConversationMessage {
        private UUID convoId;
        private String server;
        private boolean success;

        @Nonnull
        @Override
        public UUID getConversationId() {
            return this.convoId;
        }
    }
}
