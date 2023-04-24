package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.mineinabyss.geary.prefabs.PrefabKey;
import com.mineinabyss.looty.LootyFactory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link com.mineinabyss.looty.LootyPlugin Looty} to provide custom items
 */
@SuppressWarnings("SpellCheckingInspection")
public class HookLooty extends Hook {
    public HookLooty() {
        super("looty");
        setEnabledItemHook(true);
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link com.mineinabyss.looty.LootyPlugin Looty}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(itemId);
        if (prefabKey == null) return null;
        return LootyFactory.INSTANCE.createFromPrefab(prefabKey);
    }
}
