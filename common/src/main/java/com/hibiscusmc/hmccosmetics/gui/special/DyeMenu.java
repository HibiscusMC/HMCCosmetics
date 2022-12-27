package com.hibiscusmc.hmccosmetics.gui.special;

import com.hibiscusmc.hmccolor.HMCColorApi;
import com.hibiscusmc.hmccolor.gui.guis.Gui;
import com.hibiscusmc.hmccolor.gui.guis.GuiItem;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.misc.Placeholder;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

public class DyeMenu {

    // Yes, I do know how tacted on this feels.


    public static void openMenu(CosmeticUser user, Cosmetic cosmetic) {
        ItemStack originalItem = user.getUserCosmeticItem(cosmetic);
        if (originalItem == null || !cosmetic.isDyable()) return;

        Player player = user.getPlayer();
        HMCColorApi hmcColorApi = new HMCColorApi();
        Gui gui = hmcColorApi.getColorMenu();
        gui.updateTitle(Placeholder.applyPapiPlaceholders(player, Settings.getDyeMenuName()));
        gui.setItem(19, new GuiItem(originalItem));
        gui.setDefaultTopClickAction(event -> {
            if (event.getSlot() == 25) {
                ItemStack item = event.getInventory().getItem(25);
                if (item == null) return;
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;
                Color color = meta instanceof LeatherArmorMeta ? ((LeatherArmorMeta) meta).getColor() :
                        meta instanceof PotionMeta ? ((PotionMeta) meta).getColor() : null;
                if (color == null) return;
                //user.removeCosmeticSlot(cosmetic);
                user.addPlayerCosmetic(cosmetic, color);
                player.closeInventory();
            } else event.setCancelled(true);
        });

        gui.setPlayerInventoryAction(event -> event.setCancelled(true));
        gui.setCloseGuiAction(event -> {});
        gui.open(player);
    }
}
