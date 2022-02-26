package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrappedGuiItem extends GuiItem {

    private GuiAction<InventoryClickEvent> action;

    public WrappedGuiItem(final GuiItem item, final GuiAction<InventoryClickEvent> action) {
        super(item.getItemStack(), action);
        this.action = action;
    }

    public WrappedGuiItem(final @NotNull ItemStack itemStack, @Nullable final GuiAction<@NotNull InventoryClickEvent> action) {
        super(itemStack, action);
        this.action = action;
    }

    public WrappedGuiItem(final @NotNull ItemStack itemStack) {
        super(itemStack);
        this.action = null;
    }

    public WrappedGuiItem(final @NotNull Material material) {
        super(material);
        this.action = null;
    }

    public WrappedGuiItem(final @NotNull Material material, @Nullable final GuiAction<@NotNull InventoryClickEvent> action) {
        super(material, action);
        this.action = action;
    }

    @Nullable
    public GuiAction<InventoryClickEvent> getAction() {
        return action;
    }

    @Override
    public void setAction(final GuiAction<InventoryClickEvent> action) {
        super.setAction(action);
        this.action = action;
    }
}
