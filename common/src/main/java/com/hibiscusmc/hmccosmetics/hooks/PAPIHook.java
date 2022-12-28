package com.hibiscusmc.hmccosmetics.hooks;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticMainhandType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class PAPIHook extends PlaceholderExpansion {

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
            case "wardrobe-enabled":
                return String.valueOf(user.isInWardrobe());
        }
        return null;
    }

    public String getMaterial(Cosmetic cosmetic) {
        if (cosmetic instanceof CosmeticArmorType) {
            return ((CosmeticArmorType) cosmetic).getCosmeticItem().getType().toString();
        }
        if (cosmetic instanceof CosmeticMainhandType) {
            return ((CosmeticMainhandType) cosmetic).getItemStack().getType().toString();
        }
        return null;
    }

    public String getModelData(Cosmetic cosmetic) {
        if (cosmetic instanceof CosmeticArmorType) {
            ItemStack item = ((CosmeticArmorType) cosmetic).getCosmeticItem();
            if (item.hasItemMeta()) {
                return String.valueOf(item.getItemMeta().getCustomModelData());
            }
        }
        if (cosmetic instanceof CosmeticMainhandType) {
            ItemStack item = ((CosmeticMainhandType) cosmetic).getItemStack();
            if (item.hasItemMeta()) {
                return String.valueOf(item.getItemMeta().getCustomModelData());
            }
        }
        return null;
    }

    public String getItemName(Cosmetic cosmetic) {
        if (cosmetic instanceof CosmeticArmorType) {
            ItemStack item = ((CosmeticArmorType) cosmetic).getCosmeticItem();
            if (item.hasItemMeta()) {
                return item.getItemMeta().getDisplayName();
            }
        }
        if (cosmetic instanceof CosmeticMainhandType) {
            ItemStack item = ((CosmeticMainhandType) cosmetic).getItemStack();
            if (item.hasItemMeta()) {
                return item.getItemMeta().getDisplayName();
            }
        }
        return null;
    }

    public String getItemLore(Cosmetic cosmetic) {
        if (cosmetic instanceof CosmeticArmorType) {
            ItemStack item = ((CosmeticArmorType) cosmetic).getCosmeticItem();
            if (item.hasItemMeta()) {
                return String.valueOf(item.getItemMeta().getLore());
            }
        }
        if (cosmetic instanceof CosmeticMainhandType) {
            ItemStack item = ((CosmeticMainhandType) cosmetic).getItemStack();
            if (item.hasItemMeta()) {
                return String.valueOf(item.getItemMeta().getLore());
            }
        }
        return null;
    }
}
