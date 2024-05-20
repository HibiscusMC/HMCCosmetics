package com.hibiscusmc.hmccosmetics.gui.special;

import com.hibiscusmc.hmccolor.HMCColorApi;
import com.hibiscusmc.hmccolor.shaded.gui.guis.Gui;
import com.hibiscusmc.hmccolor.shaded.gui.guis.GuiItem;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import me.lojosho.hibiscuscommons.util.ColorBuilder;
import me.lojosho.hibiscuscommons.util.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DyeMenu {

    public static void openMenu(@NotNull CosmeticUser user, Cosmetic cosmetic) {
        Player player = user.getPlayer();
        if (player == null) return;
        if (!Hooks.isActiveHook("HMCColor")) {
            addCosmetic(user, cosmetic, null);
            return;
        }
        ItemStack originalItem = user.getUserCosmeticItem(cosmetic);
        if (originalItem == null || !cosmetic.isDyable()) return;

        Gui gui = HMCColorApi.createColorMenu(player);
        gui.updateTitle(Hooks.processPlaceholders(player, StringUtils.parseStringToString(Settings.getDyeMenuName())));
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

                addCosmetic(user, cosmetic, color);
                event.setCancelled(true);
            } else event.setCancelled(true);
        });

        gui.setPlayerInventoryAction(event -> event.setCancelled(true));
        gui.setCloseGuiAction(event -> {});
        gui.open(player);
    }

    private static void addCosmetic(@NotNull CosmeticUser user, Cosmetic cosmetic, Color color) {
        Player player = user.getPlayer();
        user.addPlayerCosmetic(cosmetic, color);
        player.setItemOnCursor(new ItemStack(Material.AIR));
        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            player.closeInventory();
            user.updateCosmetic(cosmetic.getSlot());
        }, 2);
    }
}
