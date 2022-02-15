package io.github.fisher2911.hmccosmetics.gui;

import com.comphenix.protocol.wrappers.EnumWrappers;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.fisher2911.hmccosmetics.util.builder.ColorBuilder;
import io.github.fisher2911.hmccosmetics.util.builder.ItemBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArmorItem extends GuiItem {

    private final String id;
    private final List<String> lockedLore;
    private final String permission;
    private final Type type;
    private GuiAction<InventoryClickEvent> action;
    private boolean dyeable;
    private int dye;

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

    public ArmorItem(final ArmorItem armorItem) {
        super(armorItem.getItemStack(), armorItem.getAction());
        this.id = armorItem.getId();
        this.lockedLore = new ArrayList<>();
        Collections.copy(armorItem.getLockedLore(), this.lockedLore);
        this.action = armorItem.getAction();
        this.permission = armorItem.getPermission();
        this.type = armorItem.getType();
        this.dyeable = armorItem.isDyeable();
        this.dye = armorItem.getDye();
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

    @Override
    public void setAction(final GuiAction<InventoryClickEvent> action) {
        super.setAction(action);
        this.action = action;
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

    public int getDye() {
        return dye;
    }

    public void setDye(final int dye) {
        this.dye = dye;
    }

    public ItemStack getColored() {
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

    public ArmorItem copy() {
        return new ArmorItem(this);
    }

    public enum Type {

        HAT(EquipmentSlot.HEAD),
        BACKPACK(null),
        OFF_HAND(EquipmentSlot.OFF_HAND),
        CHEST_PLATE(EquipmentSlot.CHEST),
        PANTS(EquipmentSlot.LEGS),
        BOOTS(EquipmentSlot.FEET);

        private final EquipmentSlot slot;

        Type(final EquipmentSlot slot) {
            this.slot = slot;
        }

        public EquipmentSlot getSlot() {
            return slot;
        }

        @Nullable
        public static Type fromWrapper(EnumWrappers.ItemSlot slot) {
            return switch (slot) {
                case HEAD -> Type.HAT;
                case CHEST -> Type.CHEST_PLATE;
                case LEGS -> Type.PANTS;
                case FEET -> Type.BOOTS;
                case OFFHAND -> Type.OFF_HAND;
                default -> null;
            };
        }

        @Nullable
        public static Type fromEquipmentSlot(final EquipmentSlot slot) {
            for (final Type type : values()) {
                if (type.getSlot() == slot) return type;
            }
            return null;
        }
    }
}
