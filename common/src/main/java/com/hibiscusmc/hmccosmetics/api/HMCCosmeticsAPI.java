package com.hibiscusmc.hmccosmetics.api;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
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
}
