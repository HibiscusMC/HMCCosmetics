package com.hibiscusmc.hmccosmetics.api.events;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player starts playing an emote.
 */
public class PlayerEmoteStartEvent extends PlayerCosmeticEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final String animationId;

    private boolean cancel = false;

    public PlayerEmoteStartEvent(@NotNull CosmeticUser who, @NotNull String animationId) {
        super(who);
        this.animationId = animationId;
    }

    /**
     * Gets the animation id of the emote the player started playing.
     *
     * @implNote The returned string of this method may be an invalid animation id.
     * Make sure to validate it before use by calling {@link com.hibiscusmc.hmccosmetics.emotes.EmoteManager#get(String)}.
     *
     * @return the animation id of the emote which the player started playing
     */
    @NotNull
    public String getAnimationId() {
        return animationId;
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