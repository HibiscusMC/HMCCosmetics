package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when cosmetics are hidden from a player
 */
public class PlayerCosmeticHideEvent extends CosmeticUserEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final CosmeticUser.HiddenReason reason;

    public PlayerCosmeticHideEvent(@NotNull CosmeticUser who, @NotNull CosmeticUser.HiddenReason reason) {
        super(who);
        this.reason = reason;
    }

    /**
     * Gets the {@link CosmeticUser.HiddenReason} as to why cosmetics are being hidden for the player
     *
     * @return The {@link CosmeticUser.HiddenReason} why cosmetics are being hidden for the player
     */
    @NotNull
    public CosmeticUser.HiddenReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this event.
     *
     * <p>
     * Canceling this event will prevent the player from hiding cosmetics.
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
