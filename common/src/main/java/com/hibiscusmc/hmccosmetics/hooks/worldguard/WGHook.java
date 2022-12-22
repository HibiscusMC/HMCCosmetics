package com.hibiscusmc.hmccosmetics.hooks.worldguard;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class WGHook {

    public static StateFlag COSMETIC_ENABLE_FLAG;

    public WGHook() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("cosmetic-enable", false);
            registry.register(flag);
            COSMETIC_ENABLE_FLAG = flag; // only set
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("cosmetic-enable");
            if (existing instanceof StateFlag) {
                COSMETIC_ENABLE_FLAG = (StateFlag) existing;
            } else {
                HMCCosmeticsPlugin.getInstance().getLogger().severe("WorldGuard Unable to be hooked!");
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
    }

    public static StateFlag getCosmeticEnableFlag() {
        return COSMETIC_ENABLE_FLAG;
    }

}
