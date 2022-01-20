package io.github.fisher2911.hmccosmetics.hook.item;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderHook implements ItemHook, Listener {

    public static final String ID = "ITEM_ADDER";
    private static final String IDENTIFIER = "itemsadder";

    @EventHandler
    public void onItemsAdderLoad(final ItemsAdderLoadDataEvent event) {
        final HMCCosmetics plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, plugin::load);
    }

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
        final CustomStack stack = CustomStack.getInstance(id);
        if (stack == null) return null;
        return stack.getItemStack().clone();
    }
}
