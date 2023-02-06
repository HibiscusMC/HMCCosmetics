package com.hibiscusmc.hmccosmetics.hooks.items;

import com.mineinabyss.geary.prefabs.PrefabKey;
import com.mineinabyss.looty.LootyFactory;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class LootyHook extends ItemHook implements Listener {

    public LootyHook() {
        super("looty");
    }

    @Override
    public ItemStack get(String itemid) {
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(itemid);
        if (prefabKey == null) return null;
        return LootyFactory.INSTANCE.createFromPrefab(prefabKey);
    }
}
