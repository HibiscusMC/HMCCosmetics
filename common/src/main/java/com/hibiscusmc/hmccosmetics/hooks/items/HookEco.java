package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.willfp.eco.core.items.Items;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HookEco extends Hook {
    public HookEco() {
        super("Eco");
        setEnabledItemHook(true);
    }

    @Override
    public ItemStack getItem(@NotNull String itemId) {
        return Items.lookup(itemId).getItem();
    }
}
