package com.hibiscusmc.hmccosmetics.api.events;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents an event related to a {@link org.bukkit.entity.Player}.
 */
public abstract class PlayerEvent extends Event {
    protected final UUID player;

    public PlayerEvent(@NotNull UUID uuid) {
        this.player = uuid;
    }

    /**
     * Returns the {@link UUID} of the player involved in this event.
     *
     * @return the UUID of the player who is involved in this event
     */
    public final @NotNull UUID getUniqueId() {
        return player;
    }
}
