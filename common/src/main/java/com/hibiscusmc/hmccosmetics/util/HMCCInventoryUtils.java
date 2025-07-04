package com.hibiscusmc.hmccosmetics.util;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HMCCInventoryUtils {

    private static final Map<CosmeticSlot, EquipmentSlot> SLOT_MAP = new HashMap<>();
    static {
        SLOT_MAP.put(CosmeticSlot.HELMET, EquipmentSlot.HEAD);
        SLOT_MAP.put(CosmeticSlot.CHESTPLATE, EquipmentSlot.CHEST);
        SLOT_MAP.put(CosmeticSlot.LEGGINGS, EquipmentSlot.LEGS);
        SLOT_MAP.put(CosmeticSlot.BOOTS, EquipmentSlot.FEET);
        SLOT_MAP.put(CosmeticSlot.OFFHAND, EquipmentSlot.OFF_HAND);
        SLOT_MAP.put(CosmeticSlot.MAINHAND, EquipmentSlot.HAND);
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
        return SLOT_MAP.get(slot);
    }

    public static boolean isCosmeticItem(ItemStack itemStack) {
        if (itemStack == null) return false;
        itemStack = itemStack.clone();
        if (!itemStack.hasItemMeta()) return false;
        return itemStack.getItemMeta().getPersistentDataContainer().has(getCosmeticKey(), PersistentDataType.STRING);
    }

    public static NamespacedKey getCosmeticKey() {
        return new NamespacedKey(HMCCosmeticsPlugin.getInstance(), "cosmetic");
    }

    /**
     * This returns all the slots a player can have on them. In 1.20.6+, the enum includes BODY, which is not a valid slot for a player.
     * @return A list of all the slots a player can have on them
     */
    public static List<EquipmentSlot> getPlayerArmorSlots() {
        return Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.OFF_HAND, EquipmentSlot.HAND);
    }
}
