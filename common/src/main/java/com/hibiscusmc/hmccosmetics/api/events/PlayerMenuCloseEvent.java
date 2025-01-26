package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a {@link Menu} is closed by a player.
 */
public class PlayerMenuCloseEvent extends PlayerMenuEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final InventoryCloseEvent.Reason reason;

    public PlayerMenuCloseEvent(@NotNull CosmeticUser who, @NotNull Menu menu, @NotNull InventoryCloseEvent.Reason reason) {
        super(who, menu);
        this.reason = reason;
    }

    /**
     * Gets the {@link InventoryCloseEvent.Reason} why the menu was closed.
     * @return The reason why the menu was closed.
     */
    public InventoryCloseEvent.Reason getReason() {
        return reason;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
