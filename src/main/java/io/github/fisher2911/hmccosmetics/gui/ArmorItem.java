package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.util.builder.ColorBuilder;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArmorItem extends GuiItem {

    private final String id;
    private final List<String> lockedLore;
    private final GuiAction<InventoryClickEvent> action;
    private final String permission;
    private final Type type;
    private boolean dyeable;
    private final int dye;

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final GuiAction<InventoryClickEvent> action,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final int dye) {
        super(itemStack, action);

        this.id = id;
        this.lockedLore = lockedLore;
        this.action = action;
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final int dye) {
        super(itemStack);
        this.id = id;
        this.lockedLore = lockedLore;
        this.action = null;
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final int dye) {
        super(material);
        this.id = id;
        this.lockedLore = lockedLore;
        this.action = null;
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            @Nullable final GuiAction<InventoryClickEvent> action,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final int dye) {
        super(material, action);
        this.id = id;
        this.lockedLore = lockedLore;
        this.action = action;
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final GuiAction<InventoryClickEvent> action,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(itemStack, action);
        this.id = id;
        this.lockedLore = lockedLore;
        this.action = action;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(itemStack);
        this.id = id;
        this.lockedLore = lockedLore;
        this.action = null;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(material);
        this.id = id;
        this.lockedLore = lockedLore;
        this.action = null;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            @Nullable final GuiAction<InventoryClickEvent> action,
            final String id,
            final List<String> lockedLore,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(material, action);
        this.id = id;
        this.lockedLore = lockedLore;
        this.action = action;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    public static ArmorItem empty(final Type type) {
        return new ArmorItem(
                new ItemStack(Material.AIR),
                "",
                new ArrayList<>(),
                "",
                type,
                -1
        );
    }

    public String getId() {
        return id;
    }

    public List<String> getLockedLore() {
        return lockedLore;
    }

    public GuiAction<InventoryClickEvent> getAction() {
        return this.action;
    }

    public String getPermission() {
        return permission;
    }

    public Type getType() {
        return type;
    }

    public boolean isDyeable() {
        return dyeable;
    }

    @Override
    public ItemStack getItemStack() {
        return this.color(super.getItemStack());
    }

    public ItemStack getItemStack(final boolean allowed) {
        final ItemStack itemStack;

        if (allowed) {
            itemStack = super.getItemStack();
        } else {
            itemStack = ItemBuilder.from(this.getItemStack()).
                    lore(this.lockedLore).
                    build();
        }

        return this.color(itemStack);
    }

    private ItemStack color(final ItemStack itemStack) {
        if (this.dye == -1 || !ColorBuilder.canBeColored(itemStack)) {
            return itemStack;
        }

        return ColorBuilder.from(itemStack).
                color(Color.fromRGB(this.dye)).
                build();
    }

    public boolean isEmpty() {
        return this.getItemStack().getType() == Material.AIR;
    }

    public enum Type {

        HAT,

        BACKPACK,

        OFF_HAND
    }
}
