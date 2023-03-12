package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerEmoteStartEvent extends Event implements Cancellable {

    private final CosmeticUser user;
    private String animationId; // Animation id can be invalid!
    private boolean isCancelled;

    public PlayerEmoteStartEvent(CosmeticUser user, String animationId) {
        this.user = user;
        this.animationId = animationId;
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

    public String getAnimationId() {
        return animationId;
    }
}