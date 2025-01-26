package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.config.Wardrobe;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player enters their {@link Wardrobe}.
 */
public class PlayerWardrobeEnterEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private boolean cancel = false;

    private Wardrobe wardrobe;

    public PlayerWardrobeEnterEvent(@NotNull CosmeticUser who, @NotNull Wardrobe wardrobe) {
        super(who);
        this.wardrobe = wardrobe;
    }

    /**
     * Get the {@link Wardrobe} the player is entering.
     * @return The wardrobe being entered
     */
    public Wardrobe getWardrobe() {
        return wardrobe;
    }

    /**
     * Set the {@link Wardrobe} the player is entering.
     * @param wardrobe the wardrobe being entered
     */
    public void setWardrobe(Wardrobe wardrobe) {
        this.wardrobe = wardrobe;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
