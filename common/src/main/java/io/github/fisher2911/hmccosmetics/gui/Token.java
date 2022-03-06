package io.github.fisher2911.hmccosmetics.gui;

import io.github.fisher2911.hmccosmetics.util.Keys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class Token {

    private final ItemStack itemStack;
    private final ArmorItem armorItem;

    public Token(final ItemStack itemStack, final ArmorItem armorItem) {
        this.itemStack = itemStack;
        this.armorItem = armorItem;
        Keys.setKey(this.itemStack, Keys.TOKEN_KEY, PersistentDataType.STRING, this.armorItem.getId());
    }

    public String getId() {
        return this.armorItem.getId();
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ArmorItem getArmorItem() {
        return armorItem;
    }
}
