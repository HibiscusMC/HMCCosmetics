package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.gui.type.Type;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record MenuItem(List<Integer> slots, ItemStack item, Type type, int priority, ConfigurationNode itemConfig) {

}
