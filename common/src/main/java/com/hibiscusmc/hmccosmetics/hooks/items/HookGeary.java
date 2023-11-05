package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingKt;
import com.mineinabyss.geary.prefabs.PrefabKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link com.mineinabyss.geary.papermc.GearyPlugin Geary} to provide custom items
 */
public class HookGeary extends Hook {

    public HookGeary() {
        super("geary");
        setEnabledItemHook(true);
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link com.mineinabyss.geary.papermc.GearyPlugin Geary}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(itemId);
        if (prefabKey == null) return null;
        return ItemTrackingKt.getGearyItems().createItem(prefabKey, null);
    }
}
