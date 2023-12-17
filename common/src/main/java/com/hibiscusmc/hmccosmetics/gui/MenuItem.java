package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.gui.type.Type;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.List;

public record MenuItem(List<Integer> slots, ItemStack item, Type type, int priority, ConfigurationNode itemConfig) {

}
