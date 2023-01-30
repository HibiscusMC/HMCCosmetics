package com.hibiscusmc.hmccosmetics.hooks.items;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class MythicHook extends ItemHook implements Listener {
    public MythicHook() {
        super("mythicmobs");
    }

    @Override
    public ItemStack get(String itemid) {
        return MythicBukkit.inst().getItemManager().getItemStack(itemid);
    }
}
