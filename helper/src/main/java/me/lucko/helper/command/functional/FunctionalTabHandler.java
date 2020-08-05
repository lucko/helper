package me.lucko.helper.command.functional;

import me.lucko.helper.command.CommandInterruptException;
import me.lucko.helper.command.context.CommandContext;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface FunctionalTabHandler<T extends CommandSender> {

    @Nonnull
    List<String> handle(CommandContext<T> c) throws CommandInterruptException;

}