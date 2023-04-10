package com.hibiscusmc.hmccosmetics.util.builder;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

public class ColorBuilder {
    /**
     * Checks if a provided material has a {@link PotionMeta} or {@link LeatherArmorMeta} item meta
     * @param material The provided material
     * @return true if the provided material has a {@link PotionMeta} or {@link LeatherArmorMeta} item meta, false otherwise
     * @since 2.2.6+
     */
    public static boolean isColorable(final Material material) {
        return isColorable(new ItemStack(material));
    }

    /**
     * Checks if a provided item stack has a {@link PotionMeta} or {@link LeatherArmorMeta} item meta
     * @param itemStack The provided item stack
     * @return true if the provided item stack has a {@link PotionMeta} or {@link LeatherArmorMeta} item meta, false otherwise
     * @since 2.2.6+
     */
    public static boolean isColorable(final @NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta instanceof LeatherArmorMeta || itemMeta instanceof PotionMeta;
    }

    /**
     * @deprecated Use {@link #isColorable(Material)} instead
     */
    @Deprecated
    public static boolean canBeColored(final Material material) {
        return canBeColored(new ItemStack(material));
    }

    /**
     * @deprecated Use {@link #isColorable(ItemStack)} instead
     */
    @Deprecated
    public static boolean canBeColored(final @NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta instanceof LeatherArmorMeta || itemMeta instanceof PotionMeta;
    }

    /**
     * Applies a {@link Color color} to a {@link PotionMeta} or {@link LeatherArmorMeta} item meta
     * @param itemMeta The item meta to apply the color to
     * @param color The color to apply to the item meta
     * @return The item meta with the color applied, or just the {@code itemMeta} provided if
     * it wasn't an instanceof {@code PotionMeta} or {@code LeatherArmorMeta}
     * @see PotionMeta
     * @see LeatherArmorMeta
     */
    public static @NotNull ItemMeta color(@NotNull ItemMeta itemMeta, @NotNull Color color) {
        if (itemMeta instanceof final PotionMeta meta) {
            meta.setColor(color);
            return itemMeta;
        }

        if (itemMeta instanceof final LeatherArmorMeta meta) {
            meta.setColor(color);
            return itemMeta;
        }

        return itemMeta;
    }
}
