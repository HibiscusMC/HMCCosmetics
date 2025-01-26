package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player has equipped a {@link Cosmetic}.
 */
public class PlayerCosmeticPostEquipEvent extends PlayerCosmeticEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Cosmetic cosmetic;

    public PlayerCosmeticPostEquipEvent(@NotNull CosmeticUser who, @NotNull Cosmetic cosmetic) {
        super(who);
        this.cosmetic = cosmetic;
    }

    /**
     * Gets the {@link Cosmetic} being equipped in this event.
     *
     * @return the cosmetic which is being equipped in this event
     */
    public @NotNull Cosmetic getCosmetic() {
        return cosmetic;
    }

    /**
     * Sets the {@link Cosmetic} that the player will equip.
     *
     * @param cosmetic the cosmetic that the player will equip
     */
    public void setCosmetic(@NotNull Cosmetic cosmetic) {
        this.cosmetic = cosmetic;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
