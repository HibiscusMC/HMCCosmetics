package io.github.fisher2911.hmccosmetics.gui;

import dev.triumphteam.gui.components.GuiAction;
import io.github.fisher2911.hmccosmetics.config.CosmeticGuiAction;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BalloonItem extends ArmorItem {

    private final String modelId;

    public BalloonItem(final BalloonItem item) {
        this(
                item.getItemStack(),
                item.getActions(),
                item.getId(),
                item.getLockedItem(),
                item.getAppliedItem(),
                item.getPermission(),
                item.getType(),
                item.getDye(),
                item.getModelId()
        );
    }

    public BalloonItem(final @NotNull ItemStack itemStack, final List<CosmeticGuiAction> actions, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final int dye, final String modelId) {
        super(itemStack, actions, id, lockedItem, appliedItem, permission, type, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final @NotNull ItemStack itemStack, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final int dye, final String modelId) {
        super(itemStack, id, lockedItem, appliedItem, permission, type, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final @NotNull Material material, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final int dye, final String modelId) {
        super(material, id, lockedItem, appliedItem, permission, type, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final @NotNull Material material, final List<CosmeticGuiAction> actions, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final int dye, final String modelId) {
        super(material, actions, id, lockedItem, appliedItem, permission, type, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final @NotNull ItemStack itemStack, final List<CosmeticGuiAction> actions, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final boolean dyeable, final int dye, final String modelId) {
        super(itemStack, actions, id, lockedItem, appliedItem, permission, type, dyeable, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final @NotNull ItemStack itemStack, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final boolean dyeable, final int dye, final String modelId) {
        super(itemStack, id, lockedItem, appliedItem, permission, type, dyeable, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final @NotNull Material material, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final boolean dyeable, final int dye, final String modelId) {
        super(material, id, lockedItem, appliedItem, permission, type, dyeable, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final @NotNull Material material, final @Nullable GuiAction<InventoryClickEvent> action, final String id, final ItemStack lockedItem, final ItemStack appliedItem, final String permission, final Type type, final boolean dyeable, final int dye, final String modelId) {
        super(material, action, id, lockedItem, appliedItem, permission, type, dyeable, dye);
        this.modelId = modelId;
    }

    public BalloonItem(final ArmorItem armorItem, final String modelId) {
        super(armorItem);
        this.modelId = modelId;
    }

    public String getModelId() {
        if (this.modelId == null) {
            return Strings.EMPTY;
        }
        return this.modelId;
    }
}
