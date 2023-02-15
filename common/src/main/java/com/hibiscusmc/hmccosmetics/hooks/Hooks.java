package com.hibiscusmc.hmccosmetics.hooks;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.hooks.items.*;
import com.hibiscusmc.hmccosmetics.hooks.misc.HookPremiumVanish;
import com.hibiscusmc.hmccosmetics.hooks.misc.HookSuperVanish;
import com.hibiscusmc.hmccosmetics.hooks.placeholders.HookPlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class Hooks {

    private static HashMap<String, Hook> hooks = new HashMap<>();
    private static HookOraxen ORAXEN_HOOK = new HookOraxen();
    private static HookItemAdder ITEMADDER_HOOK = new HookItemAdder();
    private static HookLooty LOOTY_HOOK = new HookLooty();
    private static HookMythic MYTHIC_HOOK = new HookMythic();
    private static HookHMCCosmetics HMCCOSMETIC_HOOK = new HookHMCCosmetics();
    private static HookPlaceholderAPI PAPI_HOOK = new HookPlaceholderAPI();
    private static HookPremiumVanish PREMIUM_VANISH_HOOK = new HookPremiumVanish();
    private static HookSuperVanish SUPER_VANISH_HOOK = new HookSuperVanish();

    public static Hook getHook(String id) {
        return hooks.get(id.toLowerCase());
    }

    public static boolean isItemHook(String id) {
        return hooks.containsKey(id.toLowerCase());
    }

    public static void addHook(Hook hook) {
        hooks.put(hook.getId().toLowerCase(), hook);
    }

    public static void setup() {
        for (Hook hook : hooks.values()) {
            if (Bukkit.getPluginManager().getPlugin(hook.getId()) != null) {
                HMCCosmeticsPlugin.getInstance().getServer().getPluginManager().registerEvents(hook, HMCCosmeticsPlugin.getInstance());
                hook.setActive(true);
                hook.load();
                HMCCosmeticsPlugin.getInstance().getLogger().info("Successfully hooked into " + hook.getId());
            }
        }
    }

    public static ItemStack getItem(String raw) {
        if (!raw.contains(":")) {
            Material mat = Material.getMaterial(raw.toUpperCase());
            if (mat == null) return null;
            return new ItemStack(mat);
        }
        // Ex. Oraxen:BigSword
        // split[0] is the plugin name
        // split[1] is the item name
        String[] split = raw.split(":", 2);

        if (!isItemHook(split[0])) return null;
        Hook hook = getHook(split[0]);
        if (!hook.hasEnabledItemHook()) return null;
        if (!hook.getActive()) return null;
        ItemStack item = hook.getItem(split[1]);
        return item;
    }

    public static boolean isActiveHook(String id) {
        Hook hook = getHook(id);
        if (hook == null) return false;
        return hook.getActive();
    }
}
