package io.github.fisher2911.hmccosmetics.hook.item;

import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemHooks {

    private final Map<String, ItemHook> itemHookMap;

    public ItemHooks(final Map<String, ItemHook> itemHookMap) {
        this.itemHookMap = itemHookMap;
    }

    @Nullable
    public ItemStack getItemStack(final String item) {
        final String[] parts = item.split(":");

        if (parts.length < 2) {
            return null;
        }

        final String identifier = parts[0];
        final StringBuilder itemId = new StringBuilder();

        for (int i = 1; i < parts.length; i++) {
            itemId.append(parts[i]);
            if (i < parts.length - 1) {
                itemId.append(":");
            }
        }

        final ItemHook hook = this.itemHookMap.get(identifier);

        if (hook == null) {
            return null;
        }

        return hook.getItem(itemId.toString());
    }

}
