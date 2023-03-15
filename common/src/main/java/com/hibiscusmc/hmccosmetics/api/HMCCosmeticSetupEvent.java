package com.hibiscusmc.hmccosmetics.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when the plugin is enabled
 */
public class HMCCosmeticSetupEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

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
