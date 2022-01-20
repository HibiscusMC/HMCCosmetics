package io.github.fisher2911.hmccosmetics.inventory;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.util.builder.ColorBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class PlayerArmor {

    private final Map<ArmorItem.Type, ArmorItem> armorItems;

    public PlayerArmor(ArmorItem hat, final ArmorItem backpack, final ArmorItem offHand) {
        this.armorItems = new EnumMap<>(ArmorItem.Type.class);
        this.armorItems.put(hat.getType(), hat);
        this.armorItems.put(backpack.getType(), hat);
        this.armorItems.put(offHand.getType(), offHand);
    }

    public static PlayerArmor empty() {
        return new PlayerArmor(
                ArmorItem.empty(ArmorItem.Type.HAT),
                ArmorItem.empty(ArmorItem.Type.BACKPACK),
                ArmorItem.empty(ArmorItem.Type.OFF_HAND)
        );
    }

    public ArmorItem getHat() {
        return this.armorItems.get(ArmorItem.Type.HAT);
    }

    public void setHat(final ArmorItem hat) {
        this.armorItems.put(ArmorItem.Type.HAT, hat);
    }

    public ArmorItem getBackpack() {
        return this.armorItems.get(ArmorItem.Type.BACKPACK);
    }

    public void setBackpack(final ArmorItem backpack) {
        this.armorItems.put(ArmorItem.Type.BACKPACK, backpack);
    }

    public ArmorItem getOffHand() {
        return this.armorItems.get(ArmorItem.Type.OFF_HAND);
    }

    public void setOffHand(final ArmorItem offHand) {
        this.armorItems.put(ArmorItem.Type.OFF_HAND, offHand);
    }

    public ArmorItem getItem(final ArmorItem.Type type) {
        return this.armorItems.get(type);
    }

    public ArmorItem setItem(final ArmorItem armorItem) {
        return this.armorItems.put(armorItem.getType(), armorItem);
    }
}
