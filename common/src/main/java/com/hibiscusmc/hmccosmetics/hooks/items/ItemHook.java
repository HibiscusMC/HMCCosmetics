package com.hibiscusmc.hmccosmetics.hooks.items;

import org.bukkit.inventory.ItemStack;

public class ItemHook {

    private String id;
    private boolean active;

    public ItemHook(String id) {
        this.id = id;
        active = false;
        ItemHooks.addItemHook(this);
    }

    public ItemStack get(String itemid) {
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
}
