package com.hibiscusmc.hmccosmetics.gui.special.impl;

import com.hibiscusmc.hmccolor.HMCColorApi;
import com.hibiscusmc.hmccolor.shaded.gui.guis.Gui;
import com.hibiscusmc.hmccolor.shaded.gui.guis.GuiItem;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.special.DyeMenu;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.hibiscuscommons.util.AdventureUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HMCColorDyeMenu implements DyeMenu {

    @Override
    public void openMenu(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder, @NotNull Cosmetic cosmetic) {
        ItemStack originalItem = cosmetic.getItem();
        if (originalItem == null || !cosmetic.isDyeable()) return;

        Gui gui = HMCColorApi.createColorMenu(viewer);
        gui.updateTitle(AdventureUtils.MINI_MESSAGE.deserialize(Hooks.processPlaceholders(viewer, Settings.getDyeMenuName())));
        gui.setItem(Settings.getDyeMenuInputSlot(), new GuiItem(originalItem));

        gui.setDefaultTopClickAction(event -> {
            if (event.getSlot() == Settings.getDyeMenuOutputSlot()) {
                ItemStack item = event.getInventory().getItem(Settings.getDyeMenuOutputSlot());
                if (item == null) return;

                Color color = NMSHandlers.getHandler().getUtilHandler().getColor(item);
                if (color == null) return;

                addCosmetic(viewer, cosmeticHolder, cosmetic, color);
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        });

        gui.setPlayerInventoryAction(event -> event.setCancelled(true));
        gui.setCloseGuiAction(event -> {});
        gui.open(viewer);
    }

    private void addCosmetic(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder, @NotNull Cosmetic cosmetic, @Nullable Color color) {
        cosmeticHolder.addCosmetic(cosmetic, color);
        viewer.setItemOnCursor(new ItemStack(Material.AIR));
        // Close + update a couple ticks later on the viewer's entity thread (Folia-safe)
        HMCCosmeticsPlugin.getInstance().scheduler().runForLater(viewer, 2L, () -> {
            viewer.closeInventory();
            cosmeticHolder.updateCosmetic(cosmetic.getSlot());
        });
    }
}
