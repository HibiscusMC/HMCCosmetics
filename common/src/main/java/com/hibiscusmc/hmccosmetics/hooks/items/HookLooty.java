package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.mineinabyss.geary.prefabs.PrefabKey;
import com.mineinabyss.looty.LootyFactory;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class HookLooty extends Hook implements Listener {

    public HookLooty() {
        super("looty");
        setEnabledItemHook(true);
    }

    @Override
    public ItemStack getItem(String itemid) {
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(itemid);
        if (prefabKey == null) return null;
        return LootyFactory.INSTANCE.createFromPrefab(prefabKey);
    }
}
