package com.hibiscusmc.hmccosmetics.hooks.worldguard;

import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import java.util.logging.Level;

/**
 * A hook that integrates the plugin {@link com.sk89q.worldguard.WorldGuard WorldGuard}
 */
public class WGHook {
    /**
     * @implNote Please use {@link #getCosmeticEnableFlag()} instead
     */
    private static StateFlag COSMETIC_ENABLE_FLAG;

    /**
     * @implNote Please use {@link #getCosmeticWardrobeFlag()} instead
     */
    private static StateFlag COSMETIC_WARDROBE_FLAG;

    public WGHook() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag cosmeticFlag = new StateFlag("cosmetic-enable", false);
            StateFlag wardrobeFlag = new StateFlag("cosmetic-wardrobe", false);
            registry.register(cosmeticFlag);
            registry.register(wardrobeFlag);
            COSMETIC_ENABLE_FLAG = cosmeticFlag;
            COSMETIC_WARDROBE_FLAG = wardrobeFlag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("cosmetic-enable");
            if (existing instanceof StateFlag) {
                COSMETIC_ENABLE_FLAG = (StateFlag) existing;
            } else {
                MessagesUtil.sendDebugMessages("WorldGuard Unable to be hooked!", Level.SEVERE);
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
            }
        }
    }

    /**
     * Gets the cosmetic enable {@link StateFlag}
     * @return The cosmetic enable {@link StateFlag}
     */
    public static StateFlag getCosmeticEnableFlag() {
        return COSMETIC_ENABLE_FLAG;
    }

    /**
     * Gets the cosmetic wardrobe {@link StateFlag}
     * @return The cosmetic wardrobe {@link StateFlag}
     */
    public static StateFlag getCosmeticWardrobeFlag() {
        return COSMETIC_WARDROBE_FLAG;
    }

}
