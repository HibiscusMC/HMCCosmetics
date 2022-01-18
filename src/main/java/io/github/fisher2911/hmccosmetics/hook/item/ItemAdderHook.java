  package io.github.fisher2911.hmccosmetics.hook.item;

import io.github.fisher2911.hmccosmetics.hook.Hook;
import io.th0rgal.oraxen.items.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import dev.lone.itemsadder.api.CustomItem;

public class ItemAdderHook implements ItemHook {

    public static final String ID = "ITEM_ADDER";
    private static final String IDENTIFIER = "itemadder";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public ItemStack getItem(final String id) {
        final CustomItem customItem = CustomItem.getInstance("id);
        if (customItem == null) return null;
        return customItem.getItemStack();
    }
}
