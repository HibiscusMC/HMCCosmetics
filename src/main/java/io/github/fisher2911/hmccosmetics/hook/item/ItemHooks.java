package io.github.fisher2911.hmccosmetics.hook.item;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ItemHooks {

    private final Map<String, ItemHook> itemHookMap;

    public ItemHooks(final Map<String, ItemHook> itemHookMap) {
        this.itemHookMap = itemHookMap;
    }

    @Nullable
    public ItemStack getItemStack(final String item) {
        final String[] parts = item.split(":");

        if (parts.length < 2) return null;

        final String identifier = parts[0];
        final String itemId = parts[1];

        final ItemHook hook = this.itemHookMap.get(identifier);


        if (hook == null) return null;

        return hook.getItem(itemId);
    }


}
