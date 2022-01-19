package io.github.fisher2911.hmccosmetics.inventory;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.util.builder.ColorBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class PlayerArmor {

    private ArmorItem hat;
    private ArmorItem backpack;
    private int dye;

    public PlayerArmor(ArmorItem hat, final ArmorItem backpack, final int dye) {
        this.dye = dye;
        this.setHat(hat);
        this.backpack = backpack;
    }

    public static PlayerArmor empty() {
        return new PlayerArmor(
                new ArmorItem(
                        new ItemStack(Material.AIR),
                        "",
                        new ArrayList<>(),
                        "",
                        ArmorItem.Type.HAT
                ),
                new ArmorItem(
                        new ItemStack(Material.AIR),
                        "",
                        new ArrayList<>(),
                        "",
                        ArmorItem.Type.BACKPACK
                ),
                -1);
    }

    public ArmorItem getHat() {
        return hat;
    }

    public void setHat(final ArmorItem hat) {
        this.hat = this.color(hat);
    }

    public ArmorItem getBackpack() {
        return backpack;
    }

    public void setBackpack(final ArmorItem backpack) {
        this.backpack = this.color(backpack);
    }

    private ArmorItem color(final ArmorItem armorItem) {
        if (this.dye == -1 || !ColorBuilder.canBeColored(armorItem.getItemStack())) {
            return armorItem;
        }

        final ColorBuilder colorBuilder =
                ColorBuilder.from(armorItem.getItemStack()).
                        color(Color.fromRGB(this.dye));
        return new ArmorItem(
                colorBuilder.build(),
                armorItem.getAction(),
                armorItem.getId(),
                armorItem.getLockedLore(),
                armorItem.getPermission(),
                armorItem.getType(),
                armorItem.isDyeable()
        );
    }

    public int getDye() {
        return this.dye;
    }

    public void setDye(final int dye) {
        this.dye = dye;
    }
}
