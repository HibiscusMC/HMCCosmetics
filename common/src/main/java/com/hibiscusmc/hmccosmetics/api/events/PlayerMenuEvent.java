package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event related to a player's interaction with a {@link Menu}.
 */
public abstract class PlayerMenuEvent extends PlayerCosmeticEvent {
    protected final Menu menu;

    public PlayerMenuEvent(@NotNull CosmeticUser who, @NotNull Menu menu) {
        super(who);
        this.menu = menu;
    }

    /**
     * Gets the {@link Menu} involved with this event.
     *
     * @return the menu involved in this event
     */
    public @NotNull final Menu getMenu() {
        return menu;
    }
}
