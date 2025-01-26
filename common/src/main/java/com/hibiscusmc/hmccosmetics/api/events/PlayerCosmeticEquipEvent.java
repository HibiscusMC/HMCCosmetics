package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player equips a {@link Cosmetic}.
 */
public class PlayerCosmeticEquipEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private Cosmetic cosmetic;

    private boolean cancel = false;

    public PlayerCosmeticEquipEvent(@NotNull CosmeticUser who, @NotNull Cosmetic cosmetic) {
        super(who);
        this.cosmetic = cosmetic;
    }

    /**
     * Gets the {@link Cosmetic} being equipped in this event.
     *
     * @return the cosmetic which is being equipped in this event
     */
    @NotNull
    public Cosmetic getCosmetic() {
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
