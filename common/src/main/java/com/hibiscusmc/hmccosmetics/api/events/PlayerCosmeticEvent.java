package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event related to a {@link CosmeticUser}.
 */
public abstract class PlayerCosmeticEvent extends PlayerEvent {
    protected final CosmeticUser user;

    public PlayerCosmeticEvent(@NotNull CosmeticUser who) {
        super(who.getUniqueId());
        user = who;
    }

    /**
     * Returns the {@link CosmeticUser} involved in this event.
     *
     * @return the user who is involved in this event
     */
    public final @NotNull CosmeticUser getUser() {
        return user;
    }
}
