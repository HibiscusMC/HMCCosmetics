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
        if (dye == -1 || !ColorBuilder.canBeColored(hat.getItemStack())) {
            this.hat = hat;
            return;
        }

        final ColorBuilder colorBuilder =
                ColorBuilder.from(hat.getItemStack()).
                        color(Color.fromRGB(this.dye));
        this.hat = new ArmorItem(
                colorBuilder.build(),
                hat.getAction(),
                hat.getId(),
                hat.getLockedLore(),
                hat.getPermission(),
                hat.getType(),
                hat.isDyeable()
        );
    }

    public ArmorItem getBackpack() {
        return backpack;
    }

    public void setBackpack(final ArmorItem backpack) {
        this.backpack = backpack;
    }

    public int getDye() {
        return this.dye;
    }

    public void setDye(final int dye) {
        this.dye = dye;
    }
}
