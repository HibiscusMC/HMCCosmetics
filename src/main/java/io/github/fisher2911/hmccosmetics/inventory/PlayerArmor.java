package io.github.fisher2911.hmccosmetics.inventory;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PlayerArmor {

    private ArmorItem hat;
    private ArmorItem backpack;

    public PlayerArmor(final ArmorItem hat, final ArmorItem backpack) {
        this.hat = hat;
        this.backpack = backpack;
    }

    public static PlayerArmor empty() {
        return new PlayerArmor(
                new ArmorItem(
                        new ItemStack(Material.AIR),
                        "",
                        "",
                        ArmorItem.Type.HAT
                ),
                new ArmorItem(
                        new ItemStack(Material.AIR),
                        "",
                        "",
                        ArmorItem.Type.BACKPACK
                ));
    }

    public ArmorItem getHat() {
        return hat;
    }

    public void setHat(final ArmorItem hat) {
        this.hat = hat;
    }

    public ArmorItem getBackpack() {
        return backpack;
    }

    public void setBackpack(final ArmorItem backpack) {
        this.backpack = backpack;
    }
}
