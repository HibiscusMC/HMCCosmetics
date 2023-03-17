package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a menu related event
 */
public abstract class PlayerMenuEvent extends PlayerCosmeticEvent {
    protected Menu menu;

    public PlayerMenuEvent(@NotNull CosmeticUser who, @NotNull Menu menu) {
        super(who);
        this.menu = menu;
    }

    /**
     * Gets the {@link Menu} involved with this event
     *
     * @return The {@link Menu} which is involved with the event
     */
    @NotNull
    public final Menu getMenu() {
        return menu;
    }
}
