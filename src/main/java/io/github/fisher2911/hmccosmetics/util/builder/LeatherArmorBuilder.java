package io.github.fisher2911.hmccosmetics.util.builder;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.EnumSet;
import java.util.Set;

public class LeatherArmorBuilder extends ItemBuilder{

    private static final Set<Material> VALID_ARMOR = EnumSet.of(Material.LEATHER_BOOTS,
            Material.LEATHER_LEGGINGS, Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET);

    /**
     *
     * @param material ItemStack material
     */

    LeatherArmorBuilder(final Material material) {
        super(material);
    }

    /**
     *
     * @param itemStack ItemStack
     */

    LeatherArmorBuilder(final ItemStack itemStack) {
        super(itemStack);
    }

    /**
     *
     * @param material ItemStack material
     * @return this
     * @throws IllegalArgumentException thrown if material is not leather armor
     */

    public static LeatherArmorBuilder from(final Material material) throws IllegalArgumentException {
        if (!VALID_ARMOR.contains(material)) {
            throw new IllegalArgumentException(material.name() + " is not leather armor!");
        }
        return new LeatherArmorBuilder(material);
    }

    /**
     *
     * @param itemStack ItemStack
     * @return this
     * @throws IllegalArgumentException thrown if itemStack's type is not leather armor
     */

    public static LeatherArmorBuilder from(final ItemStack itemStack) throws IllegalArgumentException {
        final Material material = itemStack.getType();
        if (!VALID_ARMOR.contains(material)) {
            throw new IllegalArgumentException(material.name() + " is not leather armor!");
        }
        return new LeatherArmorBuilder(itemStack);
    }

    /**
     *
     * @param color armor color
     * @return this
     */

    public LeatherArmorBuilder color(final Color color) {
        if (itemMeta instanceof final LeatherArmorMeta meta) {
            meta.setColor(color);
            this.itemMeta = meta;
        }
        return this;
    }

    /**
     *
     * @param material checked material
     * @return true if is leather armor, else false
     */

    public static boolean isLeatherArmor(final Material material) {
        return VALID_ARMOR.contains(material);
    }
}
