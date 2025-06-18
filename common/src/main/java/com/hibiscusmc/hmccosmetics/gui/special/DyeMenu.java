package com.hibiscusmc.hmccosmetics.gui.special;

import com.hibiscusmc.hmccolor.HMCColorApi;
import com.hibiscusmc.hmccolor.shaded.gui.guis.Gui;
import com.hibiscusmc.hmccolor.shaded.gui.guis.GuiItem;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.hibiscuscommons.util.AdventureUtils;
import me.lojosho.hibiscuscommons.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DyeMenu {
    public static void openMenu(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder, Cosmetic cosmetic) {
        if (!Hooks.isActiveHook("HMCColor")) {
            addCosmetic(viewer, cosmeticHolder, cosmetic, null);
            return;
        }
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
            } else event.setCancelled(true);
        });

        gui.setPlayerInventoryAction(event -> event.setCancelled(true));
        gui.setCloseGuiAction(event -> {});
        gui.open(viewer);
    }

    public static void openMenu(@NotNull CosmeticUser user, Cosmetic cosmetic) {
        Player player = user.getPlayer();
        if (player == null) return;
        openMenu(player, user, cosmetic);
    }

    private static void addCosmetic(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder, @NotNull Cosmetic cosmetic, @Nullable Color color) {
        cosmeticHolder.addCosmetic(cosmetic, color);
        viewer.setItemOnCursor(new ItemStack(Material.AIR));
        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            viewer.closeInventory();
            cosmeticHolder.updateCosmetic(cosmetic.getSlot());
        }, 2);
    }
}
