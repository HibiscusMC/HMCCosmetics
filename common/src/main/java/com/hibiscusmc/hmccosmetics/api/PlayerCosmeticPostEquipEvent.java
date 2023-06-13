package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerCosmeticPostEquipEvent extends PlayerCosmeticEvent {
    private static final HandlerList handlers = new HandlerList();
    private Cosmetic cosmetic;

    public PlayerCosmeticPostEquipEvent(@NotNull CosmeticUser who, @NotNull Cosmetic cosmetic) {
        super(who);
        this.cosmetic = cosmetic;
    }

    /**
     * Gets the {@link Cosmetic} being equipped in this event
     *
     * @return The {@link Cosmetic} which is being equipped in this event
     */
    @NotNull
    public Cosmetic getCosmetic() {
        return cosmetic;
    }

    /**
     * Sets the {@link Cosmetic} that the player will equip
     *
     * @param cosmetic The {@link Cosmetic} that the player will equip
     */
    public void setCosmetic(@NotNull Cosmetic cosmetic) {
        this.cosmetic = cosmetic;
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
