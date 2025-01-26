package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserEmoteManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stops playing an emote.
 */
public class PlayerEmoteStopEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final UserEmoteManager.StopEmoteReason reason;

    private boolean cancel = false;

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
    @Deprecated(forRemoval = true)
    @NotNull
    public UserEmoteManager.StopEmoteReason getStopEmoteReason() {
        return reason;
    }

    /**
     * Gets the {@link UserEmoteManager.StopEmoteReason} as to why the emote has stopped playing.
     *
     * @return the reason why the emote has stopped playing
     * @since 2.2.5
     */
    public @NotNull UserEmoteManager.StopEmoteReason getReason() {
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
