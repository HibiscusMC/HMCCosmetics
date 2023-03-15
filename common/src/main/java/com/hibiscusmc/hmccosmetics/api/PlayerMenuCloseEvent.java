package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a menu is closed by a player
 */
public class PlayerMenuCloseEvent extends PlayerMenuEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerMenuCloseEvent(@NotNull CosmeticUser who, @NotNull Menu menu) {
        super(who, menu);
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
