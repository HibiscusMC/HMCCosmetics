package io.github.fisher2911.hmccosmetics.hook.item;

import io.github.fisher2911.hmccosmetics.hook.Hook;
import org.bukkit.inventory.ItemStack;

public interface ItemHook extends Hook {

    String getIdentifier();

    ItemStack getItem(final String id);
}
