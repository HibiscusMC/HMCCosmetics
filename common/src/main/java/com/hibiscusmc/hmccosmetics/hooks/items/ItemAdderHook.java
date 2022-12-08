package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ItemAdderHook extends ItemHook implements Listener {

    // I hate IA, this overcomplicate stuff is so unneeded if it just did its stuff when its needed.

    private boolean enabled = false;

    public ItemAdderHook() {
        super("itemsadder");
    }

    @Override
    public ItemStack get(String itemid) {
        if (enabled) {
            return CustomStack.getInstance(itemid).getItemStack();
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    @EventHandler
    public void onItemAdderDataLoad(ItemsAdderLoadDataEvent event) {
        this.enabled = true;
        HMCCosmeticsPlugin.setup();
    }
}
