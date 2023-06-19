package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player starts playing an emote
 */
public class PlayerEmoteStartEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private final String animationId;

    public PlayerEmoteStartEvent(@NotNull CosmeticUser who, @NotNull String animationId) {
        super(who);
        this.animationId = animationId;
    }

    /**
     * Gets the animation id of the emote the player started playing
     * @implNote The returned string of this method may be an invalid animation id. Make sure to validate it before use
     *
     * @return The animation id of the emote which the player started playing
     */
    @NotNull
    public String getAnimationId() {
        return animationId;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this event
     *
     * <p>
     * Canceling this event will prevent the player from playing the emote
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