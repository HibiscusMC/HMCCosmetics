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
            CustomStack stack = CustomStack.getInstance(itemid);
            if (stack == null) return null;
            return stack.getItemStack();
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    @EventHandler
    public void onItemAdderDataLoad(ItemsAdderLoadDataEvent event) {
        if (enabled) return; // Only run on the first event fired; ignore all rest
        this.enabled = true;
        HMCCosmeticsPlugin.setup();
    }
}
