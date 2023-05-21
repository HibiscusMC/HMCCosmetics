package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.mineinabyss.geary.addons.GearyPhase;
import com.mineinabyss.geary.modules.GearyModuleKt;
import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingKt;
import com.mineinabyss.geary.prefabs.PrefabKey;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link com.mineinabyss.geary.papermc.GearyPlugin Geary} to provide custom items
 */
@SuppressWarnings("SpellCheckingInspection")
public class HookGeary extends Hook {

    public HookGeary() {
        super("geary");
        GearyModuleKt.getGeary().getPipeline().intercept(GearyPhase.ENABLE, () -> {
            setEnabledItemHook(true);
            HMCCosmeticsPlugin.setup();
            return null;
        });
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link com.mineinabyss.geary.papermc.GearyPlugin Geary}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(itemId);
        if (prefabKey == null) return null;
        return ItemTrackingKt.getItemTracking().getProvider().serializePrefabToItemStack(prefabKey, null);
    }
}
