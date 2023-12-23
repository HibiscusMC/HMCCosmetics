package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class HMCCosmeticsAPI {

    /**
     * Attempts to get a cosmetic from HMCCosmetics
     *
     * @param cosmetic Cosmetic Id
     * @return A {@link Cosmetic} if exists or null if it does not
     */
    @Nullable
    public static Cosmetic getCosmetic(@NotNull String cosmetic) {
        return Cosmetics.getCosmetic(cosmetic);
    }

    /**
     * Attempts to get the CosmeticUser from an online user. If a player is offline it will return null.
     * A player maybe online but not have a CosmeticUser attached to them, either from delay specified in the config
     * or from a /reload. Always check if it's null!
     *
     * @param uuid Player Unique ID
     * @return A {@link CosmeticUser} if exists or null if it does not
     */
    @Nullable
    public static CosmeticUser getUser(@NotNull UUID uuid) {
        return CosmeticUsers.getUser(uuid);
    }

    /**
     * Attempts to get a HMCCosmetics Menu. Returns null if no menu exists under that id.
     *
     * @param id Menu ID
     * @return A {@link Menu} if exists or null if it does not
     */
    @Nullable
    public static Menu getMenu(@NotNull String id) {
        return Menus.getMenu(id);
    }

    /**
     * Equips a cosmetic to a player. You can use getUser and getCosmetic to get the CosmeticUser and Cosmetic to equip.
     * @param user CosmeticUser to equip cosmetic to
     * @param cosmetic Cosmetic to equip
     */
    public static void equipCosmetic(@NotNull CosmeticUser user, @NotNull Cosmetic cosmetic) {
        equipCosmetic(user, cosmetic, null);
    }

    /**
     * Equips a cosmetic to a player with a color. You can use getUser and getCosmetic to get the CosmeticUser and Cosmetic to equip.
     * @param user CosmeticUser to equip cosmetic to
     * @param cosmetic Cosmetic to equip
     * @param color Color to apply to cosmetic
     */
    public static void equipCosmetic(@NotNull CosmeticUser user, @NotNull Cosmetic cosmetic, @Nullable Color color) {
        user.addPlayerCosmetic(cosmetic, color);
    }

    /**
     * Removes a cosmetic in cosmeticslot.
     * @param user The user to remove the cosmetic from
     * @param slot The slot to remove the cosmetic from
     */
    public static void unequipCosmetic(@NotNull CosmeticUser user, @NotNull CosmeticSlot slot) {
        user.removeCosmeticSlot(slot);
    }

    /**
     * Gets all Cosmetics that are currently registered with HMCC. This list is immutable!
     * @return A list of all registered cosmetics
     */
    public static List<Cosmetic> getAllCosmetics() {
        return List.copyOf(Cosmetics.values());
    }

    /**
     * Gets all CosmeticUsers that are currently registered with HMCC. This list is immutable!
     * @return A list of all registered CosmeticUsers
     */
    public static List<CosmeticUser> getAllCosmeticUsers() {
        return List.copyOf(CosmeticUsers.values());
    }

    /**
     * This returns the NMS version of the server as recognized by HMCCosmetics. This will be null until HMCC setup has been completed.
     * @return The NMS version of the server in String format
     */
    @Nullable
    public static String getNMSVersion() {
        return NMSHandlers.getVersion();
    }

    /**
     * This returns the HMCCosmetics version.
     * @return The HMCCosmetics version in String format
     */
    @NotNull
    public static String getHMCCVersion() {
        return HMCCosmeticsPlugin.getInstance().getDescription().getVersion();
    }
}
