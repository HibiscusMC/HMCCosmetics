package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserEmoteManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stops playing an emote
 */
public class PlayerEmoteStopEvent extends CosmeticUserEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final UserEmoteManager.StopEmoteReason reason;

    public PlayerEmoteStopEvent(@NotNull CosmeticUser who, @NotNull UserEmoteManager.StopEmoteReason reason) {
        super(who);
        this.reason = reason;
    }

    /**
     * Gets the {@link UserEmoteManager.StopEmoteReason} as to why the emote has stopped playing
     *
     * @return The {@link UserEmoteManager.StopEmoteReason} why the emote has stopped playing
     * @deprecated As of release 2.2.5+, replaced by {@link #getReason()}
     */
    @Deprecated
    @NotNull
    public UserEmoteManager.StopEmoteReason getStopEmoteReason() {
        return reason;
    }

    /**
     * Gets the {@link UserEmoteManager.StopEmoteReason} as to why the emote has stopped playing
     *
     * @return The {@link UserEmoteManager.StopEmoteReason} why the emote has stopped playing
     * @since 2.2.5
     */
    @NotNull
    public UserEmoteManager.StopEmoteReason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this event
     *
     * <p>
     * Canceling this event will prevent the player from stopping the emote
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
