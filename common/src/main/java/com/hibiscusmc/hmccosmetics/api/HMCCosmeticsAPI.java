package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HMCCosmeticsAPI {

    /**
     * Attempts to get a cosmetic from HMCCosmetics
     *
     * @param cosmetic Cosmetic Id
     * @return A {@link Cosmetic} if exists or null if it does not
     */
    @Nullable
    public static Cosmetic getCosmetic(String cosmetic) {
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
    public static CosmeticUser getUser(UUID uuid) {
        return CosmeticUsers.getUser(uuid);
    }

    /**
     * Attempts to get a HMCCosmetics Menu. Returns null if no menu exists under that id.
     *
     * @param id Menu ID
     * @return A {@link Menu} if exists or null if it does not
     */
    @Nullable
    public static Menu getMenu(String id) {
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
    public static void unequipCosmetic(CosmeticUser user, CosmeticSlot slot) {
        user.removeCosmeticSlot(slot);
    }
}
