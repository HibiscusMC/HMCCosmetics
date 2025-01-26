package com.hibiscusmc.hmccosmetics.api.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Called before a player's data is loaded into the plugin.
 *
 * <p>
 *     If this event is cancelled, the player's data will not be loaded,
 *     and the player will not be able to interact with the plugin.
 * </p>
 */
public class PlayerPreLoadEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled = false;

    public PlayerPreLoadEvent(@NotNull UUID id) {
        super(id);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
