package com.hibiscusmc.hmccosmetics.hooks.items;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemHooks {

    private static HashMap<String, ItemHook> itemHooks = new HashMap<>();
    private static OraxenHook ORAXEN_HOOK = new OraxenHook();
    private static ItemAdderHook ITEMADDER_HOOK = new ItemAdderHook();

    public static ItemHook getItemHook(String id) {
        return itemHooks.get(id.toLowerCase());
    }

    public static boolean isItemHook(String id) {
        return itemHooks.containsKey(id.toLowerCase());
    }

    public static void addItemHook(ItemHook hook) {
        itemHooks.put(hook.getId().toLowerCase(), hook);
    }

    public static void setup() {
        for (ItemHook itemHook : itemHooks.values()) {
            if (Bukkit.getPluginManager().getPlugin(itemHook.getId()) != null) {
                itemHook.setActive(true);
                HMCCosmeticsPlugin.getInstance().getLogger().info("Successfully hooked into " + itemHook.getId());
            }
        }
    }

    public static ItemStack getItem(String raw) {
        if (!raw.contains(":")) {
            Material mat = Material.getMaterial(raw);
            if (mat == null) return null;
            return new ItemStack(mat);
        }
        // Ex. Oraxen:BigSword
        // split[0] is the plugin name
        // split[1] is the item name
        String[] split = raw.split(":", 2);

        if (!isItemHook(split[0])) return null;
        ItemHook itemHook = getItemHook(split[0]);
        if (!itemHook.getActive()) return null;
        ItemStack item = itemHook.get(split[1]);
        return item;
    }
}
