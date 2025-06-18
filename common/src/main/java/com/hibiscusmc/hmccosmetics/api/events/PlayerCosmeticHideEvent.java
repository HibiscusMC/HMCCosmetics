package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when cosmetics are hidden from a player.
 */
public class PlayerCosmeticHideEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final CosmeticUser.HiddenReason reason;

    private boolean cancel = false;

    public PlayerCosmeticHideEvent(@NotNull CosmeticUser who, @NotNull CosmeticUser.HiddenReason reason) {
        super(who);
        this.reason = reason;
    }

    /**
     * Gets the {@link CosmeticUser.HiddenReason} as to why cosmetics are being hidden for the player.
     *
     * @return the reason why cosmetics are being hidden for the player
     */
    public @NotNull CosmeticUser.HiddenReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
