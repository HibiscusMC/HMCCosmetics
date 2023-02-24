package com.hibiscusmc.hmccosmetics.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HMCCosmeticSetupEvent extends Event {

    public HMCCosmeticSetupEvent() {
        // Empty
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
