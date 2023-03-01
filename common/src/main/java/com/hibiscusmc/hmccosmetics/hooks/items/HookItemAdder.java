package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.hooks.Hook;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HookItemAdder extends Hook implements Listener {

    private boolean enabled = false;

    public HookItemAdder() {
        super("itemsadder");
        setEnabledItemHook(true);
    }

    @Override
    public ItemStack getItem(String itemId) {
        if (enabled) {
            CustomStack stack = CustomStack.getInstance(itemId);
            if (stack == null) return null;
            return stack.getItemStack();
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    @EventHandler
    public void onItemAdderDataLoad(ItemsAdderLoadDataEvent event) {
        if (enabled && !Settings.getItemsAdderReloadChange()) return; // Defaultly it will only run once at startup. If hook setting is enable
        this.enabled = true;
        HMCCosmeticsPlugin.setup();
    }
}
