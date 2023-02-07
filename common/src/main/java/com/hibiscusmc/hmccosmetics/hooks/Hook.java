package com.hibiscusmc.hmccosmetics.hooks;

import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class Hook implements Listener {

    private String id;
    private boolean active;
    private boolean itemHook;

    public Hook(String id) {
        this.id = id;
        active = false;
        Hooks.addHook(this);
    }

    public void load() {
        // Override
    }

    public ItemStack getItem(String itemid) {
        return null;
        // Override
    }

    public String getId() {
        return id;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return this.active;
    }

    public void setEnabledItemHook(boolean enabled) {
        itemHook = enabled;
    }

    public boolean hasEnabledItemHook() {
        return itemHook;
    }
}
