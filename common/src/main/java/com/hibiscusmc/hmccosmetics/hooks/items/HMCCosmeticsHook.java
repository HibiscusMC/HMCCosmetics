package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HMCCosmeticsHook extends ItemHook implements Listener {

    public HMCCosmeticsHook() {
        super("HMCCosmetics");
    }

    @Override
    public ItemStack get(String itemid) {
        return Cosmetics.getCosmetic(itemid).getItem();
    }
}

