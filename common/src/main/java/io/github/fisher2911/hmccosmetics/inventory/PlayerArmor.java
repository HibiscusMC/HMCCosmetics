package io.github.fisher2911.hmccosmetics.inventory;

import io.github.fisher2911.hmccosmetics.gui.ArmorItem;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PlayerArmor {

    private final Map<ArmorItem.Type, ArmorItem> armorItems;

    public PlayerArmor(ArmorItem hat, final ArmorItem backpack, final ArmorItem offHand) {
        this.armorItems = new EnumMap<>(ArmorItem.Type.class);
        this.armorItems.put(hat.getType(), hat);
        this.armorItems.put(backpack.getType(), hat);
        this.armorItems.put(offHand.getType(), offHand);
    }

    public PlayerArmor(final Map<ArmorItem.Type, ArmorItem> armorItems) {
        this.armorItems = armorItems;
    }

    public static PlayerArmor empty() {
        return new PlayerArmor(
                ArmorItem.empty(ArmorItem.Type.HAT),
                ArmorItem.empty(ArmorItem.Type.BACKPACK),
                ArmorItem.empty(ArmorItem.Type.OFF_HAND)
        );
    }

    public ArmorItem getHat() {
        return this.getItem(ArmorItem.Type.HAT);
    }

//    public ArmorItem getBackpack() {
//        return this.getItem(ArmorItem.Type.BACKPACK);
//    }

    public ArmorItem getOffHand() {
        return this.getItem(ArmorItem.Type.OFF_HAND);
    }

    public ArmorItem getItem(final ArmorItem.Type type) {
        ArmorItem armorItem = this.armorItems.get(type);
        if (armorItem == null) {
            armorItem = ArmorItem.empty(type);
            this.armorItems.put(type, armorItem);
        }
        return armorItem;
    }

    public ArmorItem setItem(final ArmorItem armorItem) {
        if (armorItem.isEmpty() && armorItem.getType() == ArmorItem.Type.BACKPACK) {
            this.armorItems.put(ArmorItem.Type.SELF_BACKPACK, armorItem);
        }
        return this.armorItems.put(armorItem.getType(), armorItem);
    }

    public Collection<ArmorItem> getArmorItems() {
        return this.armorItems.values();
    }

    public PlayerArmor copy() {
        return new PlayerArmor(new HashMap<>(this.armorItems));
    }

    public void clear() {
        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
            this.setItem(ArmorItem.empty(type));
        }
    }

}
