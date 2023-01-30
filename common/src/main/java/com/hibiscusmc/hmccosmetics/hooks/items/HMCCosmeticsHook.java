package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HMCCosmeticsHook extends ItemHook implements Listener {

    public HMCCosmeticsHook() {
        super("HMCCosmetics");
    }

    @Override
    public ItemStack get(String itemid) {
        Cosmetic cosmetic = Cosmetics.getCosmetic(itemid);
        if (cosmetic == null) return null;
        return cosmetic.getItem();
    }
}

