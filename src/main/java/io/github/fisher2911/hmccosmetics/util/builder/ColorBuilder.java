package io.github.fisher2911.hmccosmetics.util.builder;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.EnumSet;
import java.util.Set;

public class ColorBuilder extends ItemBuilder{

    /**
     *
     * @param material ItemStack material
     */

    ColorBuilder(final Material material) {
        super(material);
    }

    /**
     *
     * @param itemStack ItemStack
     */

    ColorBuilder(final ItemStack itemStack) {
        super(itemStack);
    }

    /**
     *
     * @param material ItemStack material
     * @return this
     * @throws IllegalArgumentException thrown if itemStack's type can not change color
     */

    public static ColorBuilder from(final Material material) throws IllegalArgumentException {
        if (!canBeColored(material)) {
            throw new IllegalArgumentException(material.name() + " is not leather armor!");
        }
        return new ColorBuilder(material);
    }

    /**
     *
     * @param itemStack ItemStack
     * @return this
     * @throws IllegalArgumentException thrown if itemStack's type can not change color
     */

    public static ColorBuilder from(final ItemStack itemStack) throws IllegalArgumentException {
        final Material material = itemStack.getType();
        if (!canBeColored(itemStack)) {
            throw new IllegalArgumentException(material.name() + " is not leather armor!");
        }
        return new ColorBuilder(itemStack);
    }

    /**
     *
     * @param color armor color
     * @return this
     */

    public ColorBuilder color(final Color color) {
        if (this.itemMeta instanceof final PotionMeta meta) {
            meta.setColor(color);
        }
        if (this.itemMeta instanceof final LeatherArmorMeta meta) {
            meta.setColor(color);
        }
        return this;
    }

    public static boolean canBeColored(final Material material) {
        return canBeColored(new ItemStack(material));
    }

    public static boolean canBeColored(final ItemStack itemStack) {
        final ItemMeta itemMeta = itemStack.getItemMeta();

        return (itemMeta instanceof LeatherArmorMeta ||
                itemMeta instanceof PotionMeta);
    }
}
