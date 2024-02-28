package com.hibiscusmc.hmccosmetics.api.events;

import lombok.Getter;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CosmeticTypeRegisterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final String id;
    @Getter
    private final ConfigurationNode config;

    public CosmeticTypeRegisterEvent(String id, ConfigurationNode config) {
        this.id = id;
        this.config = config;
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
