package com.hibiscusmc.hmccosmetics.api.events;

import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when an attempt is made to register a cosmetic type that is not part of the default HMCC cosmetics.
 * <p>
 * For example, if a user specifies "test" in the config slot, and it is not a default cosmetic, this event will be
 * triggered.
 * </p>
 */
public class CosmeticTypeRegisterEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String id;
    private final ConfigurationNode config;

    public CosmeticTypeRegisterEvent(@NotNull String id, @NotNull ConfigurationNode config) {
        this.id = id;
        this.config = config;
    }

    /**
     * Returns the id of the cosmetic attempting to be registered.
     *
     * @return the id of the cosmetic. This is the key in the cosmetic configuration.
     */
    public @NotNull String getId() {
        return id;
    }

    /**
     * Retrieves the {@link ConfigurationNode} for the cosmetic that was attempted to be registered.
     * <p>
     * This node is nested below the id in the configuration.
     * </p>
     *
     * @return the configuration node for the cosmetic in the cosmetic configuration
     */
    public @NotNull ConfigurationNode getConfig() {
        return config;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
