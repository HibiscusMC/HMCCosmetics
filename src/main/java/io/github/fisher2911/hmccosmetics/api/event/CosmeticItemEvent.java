package io.github.fisher2911.hmccosmetics.api.event;

import io.github.fisher2911.hmccosmetics.api.CosmeticItem;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class CosmeticItemEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private CosmeticItem cosmeticItem;
    private boolean cancelled;

    public CosmeticItemEvent(final CosmeticItem cosmeticItem) {
        this.cosmeticItem = cosmeticItem;
        this.cancelled = false;
    }

    public CosmeticItem getCosmeticItem() {
        return this.cosmeticItem;
    }

    public void setCosmeticItem(final CosmeticItem cosmeticItem) {
        this.cosmeticItem = cosmeticItem;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
