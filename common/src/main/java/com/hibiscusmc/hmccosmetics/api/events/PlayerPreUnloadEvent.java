package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called before a player's data is un-loaded from the plugin.
 *
 * <p>
 *     If this event is cancelled, the player's data will not be un-loaded,
 *     and will be kept in memory.
 * </p>
 */
public class PlayerPreUnloadEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancelled = false;

    public PlayerPreUnloadEvent(@NotNull CosmeticUser who) {
        super(who);
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
