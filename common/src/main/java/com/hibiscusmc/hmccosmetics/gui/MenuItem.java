package com.hibiscusmc.hmccosmetics.gui;

import com.hibiscusmc.hmccosmetics.gui.type.Type;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record MenuItem(@NotNull List<Integer> slots, @NotNull ItemStack item,  Type type, int priority, ConfigurationNode itemConfig) {

}
