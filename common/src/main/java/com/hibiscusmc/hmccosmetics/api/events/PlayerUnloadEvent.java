package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a players data is unloaded from the plugin. This is called when a player leaves the server.
 */
public class PlayerUnloadEvent extends PlayerCosmeticEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public PlayerUnloadEvent(@NotNull CosmeticUser who) {
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
