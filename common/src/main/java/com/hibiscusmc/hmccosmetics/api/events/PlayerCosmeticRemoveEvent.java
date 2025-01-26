package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player removes a {@link Cosmetic}.
 */
public class PlayerCosmeticRemoveEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Cosmetic cosmetic;

    private boolean cancel = false;

    public PlayerCosmeticRemoveEvent(@NotNull CosmeticUser who, @NotNull Cosmetic cosmetic) {
        super(who);
        this.cosmetic = cosmetic;
    }

    /**
     * Gets the {@link Cosmetic} being removed in this event.
     *
     * @return the cosmetic which is being removed in this event
     */
    public Cosmetic getCosmetic() {
        return cosmetic;
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