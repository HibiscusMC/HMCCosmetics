package com.hibiscusmc.hmccosmetics.hooks;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.hooks.items.*;
import com.hibiscusmc.hmccosmetics.hooks.misc.*;
import com.hibiscusmc.hmccosmetics.hooks.placeholders.HookPlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Hooks {

    private static final HashMap<String, Hook> hooks = new HashMap<>();
    private static final HookOraxen ORAXEN_HOOK = new HookOraxen();
    private static final HookItemAdder ITEMADDER_HOOK = new HookItemAdder();
    private static final HookGeary GEARY_HOOK = new HookGeary();
    private static final HookMythic MYTHIC_HOOK = new HookMythic();
    private static final HookDenizen DENIZEN_HOOK = new HookDenizen();
    private static final HookHMCCosmetics HMCCOSMETIC_HOOK = new HookHMCCosmetics();
    private static final HookPlaceholderAPI PAPI_HOOK = new HookPlaceholderAPI();
    private static final HookPremiumVanish PREMIUM_VANISH_HOOK = new HookPremiumVanish();
    private static final HookSuperVanish SUPER_VANISH_HOOK = new HookSuperVanish();
    private static final HookHMCColor HMC_COLOR_HOOK = new HookHMCColor();
    private static final HookCMI CMI_HOOK = new HookCMI();
    private static final HookLibsDisguises LIBS_DISGUISES_HOOK = new HookLibsDisguises();
    private static final HookModelEngine MODEL_ENGINE_HOOK = new HookModelEngine();
    private static final HookMMOItems MMO_ITEMS_HOOK = new HookMMOItems();
    private static final HookEco ECO_ITEMS_HOOK = new HookEco();

    public static Hook getHook(@NotNull String id) {
        return hooks.get(id.toLowerCase());
    }

    public static boolean isItemHook(@NotNull String id) {
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

    @Nullable
    public static ItemStack getItem(@NotNull String raw) {
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
        if (!hook.isActive()) return null;
        return hook.getItem(split[1]);
    }

    @NotNull
    public static String processPlaceholders(OfflinePlayer player, String raw) {
        if (getHook("PlaceholderAPI").isActive()) return PlaceholderAPI.setPlaceholders(player, raw);
        return raw;
    }

    public static boolean isActiveHook(String id) {
        Hook hook = getHook(id);
        if (hook == null) return false;
        return hook.isActive();
    }
}
