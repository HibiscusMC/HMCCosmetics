package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserEmoteManager;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerEmoteStopEvent extends Event implements Cancellable {

    private final CosmeticUser user;
    private final UserEmoteManager.StopEmoteReason stopEmoteReason;
    private boolean isCancelled;

    public PlayerEmoteStopEvent(CosmeticUser user, UserEmoteManager.StopEmoteReason reason) {
        this.user = user;
        this.stopEmoteReason = reason;
        this.isCancelled = false;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public CosmeticUser getUser() {
        return user;
    }

    public UserEmoteManager.StopEmoteReason getStopEmoteReason() {
        return stopEmoteReason;
    }
}
