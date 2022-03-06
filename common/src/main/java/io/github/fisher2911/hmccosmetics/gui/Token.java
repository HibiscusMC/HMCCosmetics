package io.github.fisher2911.hmccosmetics.gui;

import io.github.fisher2911.hmccosmetics.util.Keys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Token {

    private final ItemStack itemStack;
    private final ArmorItem armorItem;
    private final List<String> commands;

    public Token(final ItemStack itemStack, final ArmorItem armorItem, final List<String> commands) {
        this.itemStack = itemStack;
        this.armorItem = armorItem;
        this.commands = commands;
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

    public List<String> getCommands() {
        return commands;
    }
}
