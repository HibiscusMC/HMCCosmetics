package com.hibiscusmc.hmccosmetics.hooks;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticMainhandType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.TranslationUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PAPIHook extends PlaceholderExpansion {

    private static boolean papiEnabled = false;

    public PAPIHook() {
        papiEnabled = true;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "HMCCosmetics";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HibiscusMC";
    }

    @Override
    public @NotNull String getVersion() {
        return HMCCosmeticsPlugin.getInstance().getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (!player.isOnline()) return null;
        CosmeticUser user = CosmeticUsers.getUser(player.getPlayer());
        if (user == null) return null;

        List<String> placeholderArgs = Arrays.asList(params.split("_"));

        switch (placeholderArgs.get(0).toLowerCase()) {
            case "using":
                if (placeholderArgs == null) {
                    return null;
                }
                if (placeholderArgs.get(1) != null) {
                    Cosmetic cosmetic = Cosmetics.getCosmetic(placeholderArgs.get(1));
                    if (user.getCosmetic(cosmetic.getSlot()).getId() == cosmetic.getId()) return "true";
                    return "false";
                }
            case "current":
                if (placeholderArgs == null) {
                    return null;
                }
                if (placeholderArgs.get(1) != null) {
                    CosmeticSlot slot = CosmeticSlot.valueOf(placeholderArgs.get(1).toUpperCase());
                    if (slot == null) return null;
                    if (placeholderArgs.size() == 2) return user.getCosmetic(slot).getId();
                    switch (placeholderArgs.get(2).toLowerCase()) {
                        case "material" -> {
                            return getMaterial(user.getCosmetic(slot));
                        }
                        case "custommodeldata" -> {
                            return getModelData(user.getCosmetic(slot));
                        }
                        case "name" -> {
                            return getItemName(user.getCosmetic(slot));
                        }
                        case "lore" -> {
                            return getItemLore(user.getCosmetic(slot));
                        }
                        case "permission" -> {
                            return user.getCosmetic(slot).getPermission();
                        }
                        default -> {
                            return user.getCosmetic(slot).getId();
                        }
                    }
                }
            case "unlocked":
                if (placeholderArgs == null) {
                    return null;
                }
                if (placeholderArgs.get(1) != null) {
                    Cosmetic cosmetic = Cosmetics.getCosmetic(placeholderArgs.get(1));
                    if (cosmetic == null) return "INVALID_COSMETIC";
                    return TranslationUtil.getTranslation("unlockedCosmetic", String.valueOf(user.canEquipCosmetic(cosmetic)));
                }
            case "equipped":
                if (placeholderArgs == null) {
                    return null;
                }
                if (placeholderArgs.get(1) != null) {
                    Cosmetic cosmetic = Cosmetics.getCosmetic(placeholderArgs.get(1));
                    if (cosmetic == null) return "INVALID_COSMETIC";
                    if (user.getCosmetic(cosmetic.getSlot()) == null) return "false";
                    if (cosmetic.getId() == user.getCosmetic(cosmetic.getSlot()).getId()) {
                        return "true";
                    } else {
                        return "false";
                    }
                }
            case "wardrobe-enabled":
                return String.valueOf(user.isInWardrobe());
        }
        return null;
    }

    public String getMaterial(Cosmetic cosmetic) {
        ItemStack item = cosmetic.getItem();
        if (item == null) return null;
        return cosmetic.getItem().getType().toString();
    }

    public String getModelData(Cosmetic cosmetic) {
        ItemStack item = cosmetic.getItem();
        if (item == null) return null;
        if (!item.hasItemMeta()) return null;
        ItemMeta itemMeta = item.getItemMeta();
        return String.valueOf(itemMeta.getCustomModelData());
    }

    public String getItemName(Cosmetic cosmetic) {
        ItemStack item = cosmetic.getItem();
        if (item == null) return null;
        if (!item.hasItemMeta()) return null;
        ItemMeta itemMeta = item.getItemMeta();
        return itemMeta.getDisplayName();
    }

    public String getItemLore(Cosmetic cosmetic) {
        ItemStack item = cosmetic.getItem();
        if (item == null) return null;
        if (item.hasItemMeta()) {
            return String.valueOf(item.getItemMeta().getLore());
        }
        return null;
    }

    public static boolean isPAPIEnabled() {
        return papiEnabled;
    }
}
