package io.github.fisher2911.hmccosmetics.api;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper for ArmorItem used internally for convenience and safety
 */
public class CosmeticItem {

    private final ArmorItem armorItem;

    public CosmeticItem(final ArmorItem armorItem) {
        this.armorItem = armorItem;
    }

    /**
     * @param itemStack the {@link org.bukkit.inventory.ItemStack} display item
     * @param id the id of the item
     * @param type the cosmetic item type
     * @param dyeable whether the item can be dyed
     * @param rgb from Bukkit's {@link Color#asRGB()}
     */

    public CosmeticItem(
            final ItemStack itemStack,
            final String name,
            final String id,
            final ItemStack locked,
            final ItemStack applied,
            final String permission,
            final ArmorItem.Type type,
            final boolean dyeable,
            final int rgb) {
        this.armorItem = new ArmorItem(itemStack, name, id, locked, applied, permission, type, dyeable, rgb);
    }

    /**
     * @param material the {@link org.bukkit.Material} display item
     * @param id the id of the item
     * @param type the cosmetic item type
     * @param dyeable whether the item can be dyed
     * @param rgb from Bukkit's {@link Color#asRGB()}
     */

    public CosmeticItem(
            final Material material,
            final String name,
            final String id,
            final Material locked,
            final Material applied,
            final String permission,
            final ArmorItem.Type type,
            final boolean dyeable,
            final int rgb
    ) {
        this.armorItem = new ArmorItem(material, name, id, new ItemStack(locked), new ItemStack(applied), permission, type, dyeable, rgb);
    }

    /**
     * @param itemStack the {@link org.bukkit.inventory.ItemStack} display item
     * @param id the id of the item
     * @param type the cosmetic item type
     */

    public CosmeticItem(final ItemStack itemStack, final String name, final String id, final ItemStack locked, final ItemStack applied, final ArmorItem.Type type) {
        this(itemStack, name, id, locked, applied, "", type, false, -1);
    }

    /**
     * @param material the {@link org.bukkit.Material} display item
     * @param id the id of the item
     * @param type the cosmetic item type
     */

    public CosmeticItem(final Material material, final Material locked, final Material applied, final String name, final String id, final ArmorItem.Type type) {
        this(material, name, id, locked, applied, "", type, false, -1);
    }

    public ItemStack getItemStack(final ArmorItem.Status status) {
        return this.armorItem.getItemStack(status);
    }

    public ItemStack getItemStack() {
        return this.armorItem.getItemStack();
    }

    public String getId() {
        return this.armorItem.getId();
    }

    public ArmorItem.Type getType() {
        return this.armorItem.getType();
    }

    public boolean isDyeable() {
        return this.armorItem.isDyeable();
    }

    public int getColor() {
        return this.armorItem.getDye();
    }

    public ArmorItem getArmorItem() {
        return this.armorItem;
    }

}
