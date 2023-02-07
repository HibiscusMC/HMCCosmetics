package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.hooks.Hook;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HookHMCCosmetics extends Hook implements Listener {

    public HookHMCCosmetics() {
        super("HMCCosmetics");
        setEnabledItemHook(true);
    }

    @Override
    public ItemStack getItem(String itemid) {
        Cosmetic cosmetic = Cosmetics.getCosmetic(itemid);
        if (cosmetic == null) return null;
        return cosmetic.getItem();
    }
}

