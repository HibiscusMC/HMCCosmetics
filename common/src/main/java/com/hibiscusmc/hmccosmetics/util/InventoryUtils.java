package com.hibiscusmc.hmccosmetics.util;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InventoryUtils {

    /**
     * Converts from the Bukkit item slots to ProtocolLib item slots. Will produce a null if an improper bukkit item slot is sent through
     * @param slot The BUKKIT item slot to convert.
     * @return The ProtocolLib item slot that is returned
     */
    public static EnumWrappers.ItemSlot itemBukkitSlot(final EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EnumWrappers.ItemSlot.HEAD;
            case CHEST -> EnumWrappers.ItemSlot.CHEST;
            case LEGS -> EnumWrappers.ItemSlot.LEGS;
            case FEET -> EnumWrappers.ItemSlot.FEET;
            case HAND -> EnumWrappers.ItemSlot.MAINHAND;
            case OFF_HAND -> EnumWrappers.ItemSlot.OFFHAND;
        };
    }

    public static int getPacketArmorSlot(final EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            case OFF_HAND -> 45;
            default -> -1;
        };
    }

    @Nullable
    public static EquipmentSlot getPacketArmorSlot(final int slot) {
        return switch (slot) {
            case 5 -> EquipmentSlot.HEAD;
            case 6 -> EquipmentSlot.CHEST;
            case 7 -> EquipmentSlot.LEGS;
            case 8 -> EquipmentSlot.FEET;
            case 45 -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    public static CosmeticSlot BukkitCosmeticSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> CosmeticSlot.MAINHAND;
            case OFF_HAND -> CosmeticSlot.OFFHAND;
            case FEET -> CosmeticSlot.BOOTS;
            case LEGS -> CosmeticSlot.LEGGINGS;
            case CHEST -> CosmeticSlot.CHESTPLATE;
            case HEAD -> CosmeticSlot.HELMET;
            default -> null;
        };
    }

    @Contract(pure = true)
    @Nullable
    public static CosmeticSlot BukkitCosmeticSlot(int slot) {
        switch (slot) {
            case 36 -> {
                return CosmeticSlot.HELMET;
            }
            case 37 -> {
                return CosmeticSlot.CHESTPLATE;
            }
            case 38 -> {
                return CosmeticSlot.LEGGINGS;
            }
            case 39 -> {
                return CosmeticSlot.BOOTS;
            }
            case 40 -> {
                return CosmeticSlot.OFFHAND;
            }
            default -> {
                return null;
            }
        }
    }

    @Contract(pure = true)
    @Nullable
    public static CosmeticSlot NMSCosmeticSlot(int slot) {
        switch (slot) {
            case 5 -> {
                return CosmeticSlot.HELMET;
            }
            case 6 -> {
                return CosmeticSlot.CHESTPLATE;
            }
            case 7 -> {
                return CosmeticSlot.LEGGINGS;
            }
            case 8 -> {
                return CosmeticSlot.BOOTS;
            }
            case 45 -> {
                return CosmeticSlot.OFFHAND;
            }
            default -> {
                return null;
            }
        }
    }

    @Contract(pure = true)
    @Nullable
    public static EquipmentSlot getEquipmentSlot(@NotNull CosmeticSlot slot) {
        switch (slot) {
            case HELMET -> {
                return EquipmentSlot.HEAD;
            }
            case CHESTPLATE -> {
                return EquipmentSlot.CHEST;
            }
            case LEGGINGS -> {
                return EquipmentSlot.LEGS;
            }
            case BOOTS -> {
                return EquipmentSlot.FEET;
            }
            case OFFHAND -> {
                return EquipmentSlot.OFF_HAND;
            }
            case MAINHAND -> {
                return EquipmentSlot.HAND;
            }
            default -> {
                return null;
            }
        }
    }

    public static boolean isCosmeticItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        itemStack = itemStack.clone();
        if (!itemStack.hasItemMeta()) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(getCosmeticKey(), PersistentDataType.STRING);
    }

    public static NamespacedKey getCosmeticKey() {
        return new NamespacedKey(HMCCosmeticsPlugin.get(), "cosmetic");
    }

    public static NamespacedKey getOwnerKey() {
        return new NamespacedKey(HMCCosmeticsPlugin.get(), "owner");
    }
}
