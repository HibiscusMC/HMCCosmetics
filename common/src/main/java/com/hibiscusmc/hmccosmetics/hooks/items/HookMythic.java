package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.hibiscusmc.hmccosmetics.hooks.HookFlag;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link io.lumine.mythic.bukkit.MythicBukkit MythicBukkit} to provide custom items
 */
@SuppressWarnings("SpellCheckingInspection")
public class HookMythic extends Hook {
    public HookMythic() {
        super("mythicmobs", HookFlag.ITEM_SUPPORT);
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link io.lumine.mythic.bukkit.MythicBukkit MythicBukkit}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        return MythicBukkit.inst().getItemManager().getItemStack(itemId);
    }
}
