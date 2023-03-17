package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a menu is opened by a player
 */
public class PlayerMenuOpenEvent extends PlayerMenuEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;

    public PlayerMenuOpenEvent(@NotNull CosmeticUser who, @NotNull Menu menu) {
        super(who, menu);
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this event
     *
     * <p>
     * Canceling this event will prevent the player from opening a {@link Menu}
     * </p>
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
