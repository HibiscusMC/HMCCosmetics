package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HookMythic extends Hook implements Listener {
    public HookMythic() {
        super("mythicmobs");
        setEnabledItemHook(true);
    }

    @Override
    public ItemStack getItem(String itemid) {
        return MythicBukkit.inst().getItemManager().getItemStack(itemid);
    }
}
