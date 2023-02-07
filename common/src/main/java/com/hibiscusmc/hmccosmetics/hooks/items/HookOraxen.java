package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HookOraxen extends Hook implements Listener {

    public HookOraxen() {
        super("oraxen");
        setEnabledItemHook(true);
    }

    @Override
    public ItemStack getItem(String itemid) {
        ItemBuilder builder = OraxenItems.getItemById(itemid);
        if (builder == null) return null;
        return builder.build();
    }
}
