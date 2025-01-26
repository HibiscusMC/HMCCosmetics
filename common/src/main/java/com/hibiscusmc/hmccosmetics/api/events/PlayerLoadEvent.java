package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player's cosmetic data is loaded.
 */
public class PlayerLoadEvent extends PlayerCosmeticEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PlayerLoadEvent(@NotNull CosmeticUser who) {
        super(who);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
