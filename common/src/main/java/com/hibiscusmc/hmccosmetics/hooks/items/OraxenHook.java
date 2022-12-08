package com.hibiscusmc.hmccosmetics.hooks.items;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class OraxenHook extends ItemHook implements Listener {

    public OraxenHook() {
        super("oraxen");
    }

    @Override
    public ItemStack get(String itemid) {
        return OraxenItems.getItemById(itemid).build();
    }
}
