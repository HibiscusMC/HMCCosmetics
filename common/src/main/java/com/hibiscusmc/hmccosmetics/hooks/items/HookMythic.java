package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("SpellCheckingInspection")
public class HookMythic extends Hook {
    public HookMythic() {
        super("mythicmobs");
        setEnabledItemHook(true);
    }

    @Override
    public ItemStack getItem(@NotNull String itemId) {
        return MythicBukkit.inst().getItemManager().getItemStack(itemId);
    }
}
