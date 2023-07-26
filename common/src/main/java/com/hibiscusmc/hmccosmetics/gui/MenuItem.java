package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.gui.type.Type;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public class MenuItem {

    private List<Integer> slots;
    private ItemStack item;
    private Type type;
    private ConfigurationNode itemConfig;

    public MenuItem(List<Integer> slots, ItemStack item, Type type, ConfigurationNode itemConfig) {
        this.slots = slots;
        this.item = item;
        this.type = type;
        this.itemConfig = itemConfig;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public ItemStack getItem() {
        return item;
    }

    public Type getType() {
        return type;
    }

    public ConfigurationNode getItemConfig() {
        return itemConfig;
    }

}
