package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.config.WardrobeLocation;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player enters their wardrobe
 */
public class PlayerWardrobeEnterEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private WardrobeLocation wardrobeLocation;

    public PlayerWardrobeEnterEvent(@NotNull CosmeticUser who, @NotNull WardrobeLocation wardrobeLocation) {
        super(who);
        this.wardrobeLocation = wardrobeLocation;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this event
     *
     * <p>
     * Canceling this event will prevent the player from entering their wardrobe
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

    public void setWardrobeLocation(WardrobeLocation wardrobeLocation) {
        this.wardrobeLocation = wardrobeLocation;
    }

    public WardrobeLocation getWardrobeLocation() {
        return wardrobeLocation;
    }
}
