package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.hibiscusmc.hmccosmetics.hooks.HookFlag;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link io.th0rgal.oraxen.OraxenPlugin OraxenPlugin} to provide custom items
 */
@SuppressWarnings("SpellCheckingInspection")
public class HookOraxen extends Hook {
    public HookOraxen() {
        super("oraxen", HookFlag.ITEM_SUPPORT);
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link io.th0rgal.oraxen.OraxenPlugin OraxenPlugin}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        ItemBuilder builder = OraxenItems.getItemById(itemId);
        if (builder == null) return null;
        return builder.build();
    }
}
