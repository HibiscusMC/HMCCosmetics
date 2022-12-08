package com.hibiscusmc.hmccosmetics.hooks.items;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

public class ItemAdderHook extends ItemHook {

    public ItemAdderHook() {
        super("itemsadder");
    }

    @Override
    public ItemStack get(String itemid) {
        return CustomStack.getInstance(itemid).getItemStack();
    }

}
