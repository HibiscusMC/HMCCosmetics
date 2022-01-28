package io.github.fisher2911.hmccosmetics.api;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class HMCCosmeticsAPI {

    private static final HMCCosmetics plugin;

    static {
        plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
    }

    /**
     *
     * This will attempt to get the {@link io.github.fisher2911.hmccosmetics.api.CosmeticItem} that the
     * user is wearing. It returns an empty {@link io.github.fisher2911.hmccosmetics.api.CosmeticItem} if the user
     * is not found, or if the user is not wearing a cosmetic
     * @param uuid the uuid of the user
     * @param type the type of cosmetic being retrieved
     * @return the current cosmetic of the player
     */
    public static CosmeticItem getUserCurrentItem(final UUID uuid, final ArmorItem.Type type) {
        final Optional<User> userOptional = plugin.getUserManager().get(uuid);
        if (userOptional.isEmpty()) return new CosmeticItem(ArmorItem.empty(type));
        return new CosmeticItem(userOptional.get().getPlayerArmor().getItem(type));
    }

    /**
     *
     * @param uuid the uuid of the user whose cosmetic is being set
     * @param cosmeticItem the cosmetic being set
     * @return true if the cosmetic was set, or else false
     */
    public static boolean setCosmeticItem(final UUID uuid, final CosmeticItem cosmeticItem) {
        final UserManager userManager = plugin.getUserManager();
        final Optional<User> userOptional = userManager.get(uuid);
        if (userOptional.isEmpty()) return false;

        userManager.setItem(userOptional.get(), cosmeticItem.getArmorItem());
        return true;
    }

    /**
     *
     * @param id the id of the cosmetic item being retrieved
     * @return null if the cosmetic was not found, or a copy of the cosmetic item
     */

    @Nullable
    public static CosmeticItem getCosmeticFromId(final String id) {
        final ArmorItem armorItem = plugin.getCosmeticManager().getArmorItem(id);
        if (armorItem == null) return null;
        return new CosmeticItem(new ArmorItem(armorItem));
    }

    /**
     *
     * @param uuid the uuid of the user whose armor stand id is being retrieved
     * @return the armor stand id, or -1 if the user is not found
     */

    public static int getUserArmorStandId(final UUID uuid) {
        final Optional<User> userOptional = plugin.getUserManager().get(uuid);
        if (userOptional.isEmpty()) return -1;
        return userOptional.get().getArmorStandId();
    }
}
