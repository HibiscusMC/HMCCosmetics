package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ColorItem extends GuiItem {

    private final Color color;

    public ColorItem(final @NotNull ItemStack itemStack, final GuiAction<InventoryClickEvent> action, final Color color) {
        super(itemStack, action);
        this.color = color;
    }

    public ColorItem(final @NotNull ItemStack itemStack, final Color color) {
        super(itemStack);
        this.color = color;
    }

    public ColorItem(final @NotNull Material material, final Color color) {
        super(material);
        this.color = color;
    }

    public ColorItem(final @NotNull Material material, final @Nullable GuiAction<InventoryClickEvent> action, final Color color) {
        super(material, action);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
