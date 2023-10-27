package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.mineinabyss.geary.addons.GearyPhase;
import com.mineinabyss.geary.modules.GearyModuleKt;
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingKt;
import com.mineinabyss.geary.prefabs.PrefabKey;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link com.mineinabyss.geary.papermc.GearyPlugin Geary} to provide custom items
 */
public class HookGeary extends Hook {
    private boolean enabled = false;

    public HookGeary() {
        super("geary");
        setEnabledItemHook(true);
    }

    @Override
    public void load() {
        if (!enabled) GearyModuleKt.getGeary().getPipeline().runOnOrAfter(GearyPhase.INIT_ENTITIES, () -> {
            enabled = true;
            HMCCosmeticsPlugin.setup();
            return null;
        });
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link com.mineinabyss.geary.papermc.GearyPlugin Geary}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        if (enabled) {
            PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(itemId);
            if (prefabKey == null) return null;
            return ItemTrackingKt.getGearyItems().createItem(prefabKey, null);
        } else return new ItemStack(Material.AIR);
    }
}
