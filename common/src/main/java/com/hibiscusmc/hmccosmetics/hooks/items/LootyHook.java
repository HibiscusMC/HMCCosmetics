package com.hibiscusmc.hmccosmetics.hooks.items;

import com.mineinabyss.geary.prefabs.PrefabKey;
import com.mineinabyss.looty.LootyFactory;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class LootyHook extends ItemHook implements Listener {

    public LootyHook() {
        super("looty");
    }

    @Override
    public ItemStack get(String itemid) {
        if (itemid.split(":").length != 2) return null;
        return LootyFactory.INSTANCE.createFromPrefab(PrefabKey.Companion.of(itemid));
    }
}
