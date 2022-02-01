package io.github.fisher2911.hmccosmetics.hook.item;

import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.OraxenItems;
import org.bukkit.inventory.ItemStack;

public class OraxenHook implements ItemHook {

    public static final String ID = "ORAXEN";
    private static final String IDENTIFIER = "oraxen";

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
        final ItemBuilder itemBuilder = OraxenItems.getItemById(id);
        if (itemBuilder == null) {
            return null;
        }
        return itemBuilder.build();
    }

}
