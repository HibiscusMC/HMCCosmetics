package com.hibiscusmc.hmccosmetics.api.events;

import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a cosmetic type not registered with HMCC default cosmetics is attempted to be registered. So if someone puts "test" in the config slot, and it's not a default cosmetic, this event will be called.
 */
public class CosmeticTypeRegisterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String id;
    private final ConfigurationNode config;

    public CosmeticTypeRegisterEvent(String id, ConfigurationNode config) {
        this.id = id;
        this.config = config;
    }

    /**
     * Returns the id of the cosmetic trying to be registered. For example, "beanie" or "test"
     * @return The id. This is the key in the cosmetic config
     */
    public String getId() {
        return id;
    }

    /**
     * This will already be in the nested node below the id in the config.
     * @return The cosmetic config node in the cosmetic config that was attempted to get registered
     */
    public ConfigurationNode getConfig() {
        return config;
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
