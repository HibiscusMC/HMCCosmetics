package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import io.github.fisher2911.hmccosmetics.config.CosmeticGuiAction;
import io.github.fisher2911.hmccosmetics.util.builder.ColorBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ArmorItem extends WrappedGuiItem {

    private final String name;
    private final String id;
    private final ItemStack lockedItem;
    private final ItemStack appliedItem;
    private final String permission;
    private final Type type;
    private List<CosmeticGuiAction> actions;
    private boolean dyeable;
    private int dye;

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final List<CosmeticGuiAction> actions,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final int dye) {
        super(itemStack, null);
        this.name = name;
        this.id = id;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.actions = actions;
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final int dye) {
        super(itemStack);
        this.id = id;
        this.name = name;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.actions = new ArrayList<>();
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final int dye) {
        super(material);
        this.id = id;
        this.name = name;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.actions = new ArrayList<>();
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            final List<CosmeticGuiAction> actions,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final int dye) {
        super(material, null);
        this.actions = actions;
        this.id = id;
        this.name = name;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.permission = permission;
        this.type = type;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final List<CosmeticGuiAction> actions,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(itemStack, null);
        this.name = name;
        this.id = id;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.actions = actions;
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final ItemStack itemStack,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(itemStack);
        this.name = name;
        this.id = id;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.actions = new ArrayList<>();
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(material);
        this.id = id;
        this.name = name;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.actions = new ArrayList<>();
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    public ArmorItem(
            @NotNull final Material material,
            @Nullable final GuiAction<InventoryClickEvent> action,
            final String name,
            final String id,
            final ItemStack lockedItem,
            final ItemStack appliedItem,
            final String permission,
            final Type type,
            final boolean dyeable,
            final int dye) {
        super(material, action);
        this.name = name;
        this.id = id;
        this.lockedItem = lockedItem;
        this.appliedItem = appliedItem;
        this.actions = new ArrayList<>();
        this.permission = permission;
        this.type = type;
        this.dyeable = dyeable;
        this.dye = dye;
    }

    protected ArmorItem(final ArmorItem armorItem) {
        super(armorItem.getItemStack().clone(), null);
        this.name = armorItem.getName();
        this.id = armorItem.getId();
        this.lockedItem = armorItem.getLockedItem().clone();
        this.appliedItem = armorItem.getAppliedItem().clone();
        this.actions = armorItem.getActions();
        this.permission = armorItem.getPermission();
        this.type = armorItem.getType();
        this.dyeable = armorItem.isDyeable();
        this.dye = armorItem.getDye();
    }

    public static ArmorItem empty(final Type type) {
        return empty(type, "");
    }

    public static ArmorItem empty(final Type type, final String id) {
        if (type == Type.BALLOON) {
            return new BalloonItem(
                    new ItemStack(Material.AIR),
                    id,
                    id,
                    new ItemStack(Material.AIR),
                    new ItemStack(Material.AIR),
                    "",
                    type,
                    -1,
                    ""
            );
        }
        return new ArmorItem(
                new ItemStack(Material.AIR),
                id,
                id,
                new ItemStack(Material.AIR),
                new ItemStack(Material.AIR),
                "",
                type,
                -1
        );
    }

    public String getId() {
        return id;
    }

    public ItemStack getLockedItem() {
        return lockedItem;
    }

    public ItemStack getAppliedItem() {
        return appliedItem;
    }

    public List<CosmeticGuiAction> getActions() {
        return actions;
    }

    public void setActions(final List<CosmeticGuiAction> actions) {
        this.actions.clear();
        this.actions.addAll(actions);
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

    public ItemStack getItemStack(final Status status) {
        return this.color(switch (status) {
            case ALLOWED -> super.getItemStack();
            case LOCKED -> this.getLockedItem();
            case APPLIED -> this.getAppliedItem();
        });
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
        if (this instanceof final BalloonItem item) {
            return new BalloonItem(item);
        }
        return new ArmorItem(this);
    }

    public String getName() {
        return this.name;
    }

    public enum Type {

        HAT(EquipmentSlot.HEAD),
        BACKPACK(null),
        BALLOON(null),
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
        public static Type fromWrapper(EquipmentSlot slot) {
            return switch (slot) {
                case HEAD -> Type.HAT;
                case CHEST -> Type.CHEST_PLATE;
                case LEGS -> Type.PANTS;
                case FEET -> Type.BOOTS;
                case OFF_HAND -> Type.OFF_HAND;
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

    public enum Status {
        ALLOWED,
        LOCKED,
        APPLIED
    }
}
