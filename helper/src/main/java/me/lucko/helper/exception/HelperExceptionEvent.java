package me.lucko.helper.exception;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HelperExceptionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private HelperException exception;

    public HelperExceptionEvent(HelperException exception) {
        super(!Bukkit.isPrimaryThread());
        this.exception = Preconditions.checkNotNull(exception, "exception");
    }

    public HelperException getException() {
        return this.exception;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
