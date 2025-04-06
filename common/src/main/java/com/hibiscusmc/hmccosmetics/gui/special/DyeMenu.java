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
import me.lojosho.hibiscuscommons.util.ColorBuilder;
import me.lojosho.hibiscuscommons.util.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DyeMenu {
    public static void openMenu(@NotNull Player viewer, @NotNull CosmeticHolder cosmeticHolder, Cosmetic cosmetic) {
        if (!Hooks.isActiveHook("HMCColor")) {
            addCosmetic(viewer, cosmeticHolder, cosmetic, null);
            return;
        }
        ItemStack originalItem = cosmetic.getItem();
        if (originalItem == null || !cosmetic.isDyable()) return;

        Gui gui = HMCColorApi.createColorMenu(viewer);
        gui.updateTitle(Hooks.processPlaceholders(viewer, StringUtils.parseStringToString(Settings.getDyeMenuName())));
        gui.setItem(Settings.getDyeMenuInputSlot(), new GuiItem(originalItem));
        gui.setDefaultTopClickAction(event -> {
            if (event.getSlot() == Settings.getDyeMenuOutputSlot()) {
                ItemStack item = event.getInventory().getItem(Settings.getDyeMenuOutputSlot());
                if (item == null) return;
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;

                Color color = null;
                if (meta instanceof LeatherArmorMeta leatherMeta) {
                    color = leatherMeta.getColor();
                } else if (meta instanceof PotionMeta potionMeta) {
                    color = potionMeta.getColor();
                } else if (meta instanceof MapMeta mapMeta) {
                    color = mapMeta.getColor();
                } else if (meta instanceof FireworkEffectMeta fireworkEffectMeta) {
                    FireworkEffect effect = fireworkEffectMeta.getEffect();
                    if (effect != null) {
                        color = effect.getColors().stream().findFirst().isPresent() ? effect.getColors().stream().findFirst().get() : null;
                    }
                }
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
