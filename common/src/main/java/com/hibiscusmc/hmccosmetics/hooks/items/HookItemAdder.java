package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.hibiscusmc.hmccosmetics.hooks.HookFlag;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link dev.lone.itemsadder.api.ItemsAdder ItemsAdder} to provide custom items
 */
@SuppressWarnings("SpellCheckingInspection")
public class HookItemAdder extends Hook {
    private boolean enabled = false;

    public HookItemAdder() {
        super("itemsadder", HookFlag.ITEM_SUPPORT);
    }

    /**
     * Gets a cosmetic {@link ItemStack} that is associated with the provided id from the plugin {@link dev.lone.itemsadder.api.ItemsAdder ItemsAdder}
     */
    @Override
    public ItemStack getItem(@NotNull String itemId) {
        if (enabled) {
            CustomStack stack = CustomStack.getInstance(itemId);
            if (stack == null) return null;
            return stack.getItemStack();
        } else {
            return new ItemStack(Material.AIR);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemAdderDataLoad(ItemsAdderLoadDataEvent event) {
        // By default, it will only run once at startup, if hook setting is enabled
        if (enabled && !Settings.isItemsAdderChangeReload()) return;
        this.enabled = true;
        HMCCosmeticsPlugin.setup();
    }
}
