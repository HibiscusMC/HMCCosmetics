package com.hibiscusmc.hmccosmetics.util.builder;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;

public class ColorBuilder {

    public static boolean canBeColored(final Material material) {
        return canBeColored(new ItemStack(material));
    }

    public static boolean canBeColored(final @NotNull ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        return (itemMeta instanceof LeatherArmorMeta ||
                itemMeta instanceof PotionMeta);
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
    public static ItemMeta color(ItemMeta itemMeta, final Color color) {
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
