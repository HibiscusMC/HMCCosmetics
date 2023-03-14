package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player equips a cosmetic
 */
public class PlayerCosmeticEquipEvent extends CosmeticUserEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancel = false;
    private Cosmetic cosmetic;

    public PlayerCosmeticEquipEvent(@NotNull CosmeticUser who, @NotNull Cosmetic cosmetic) {
        super(who);
        this.cosmetic = cosmetic;
    }

    /**
     * Gets the cosmetic being equipped in this event.
     *
     * @return The {@link Cosmetic} which is being equipped in this event.
     */
    @NotNull
    public Cosmetic getCosmetic() {
        return cosmetic;
    }

    /**
     * Sets the cosmetic that the player will equip
     *
     * @param cosmetic The cosmetic that the player will equip
     */
    public void setCosmetic(@NotNull Cosmetic cosmetic) {
        this.cosmetic = cosmetic;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the cancellation state of this event.
     *
     * <p>
     * Canceling this event will prevent the player from equipping the cosmetic.
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
