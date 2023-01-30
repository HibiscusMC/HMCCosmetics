package com.hibiscusmc.hmccosmetics.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HMCCosmeticSetupEvent extends Event {

    public HMCCosmeticSetupEvent() {
        // Empty
    }

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
