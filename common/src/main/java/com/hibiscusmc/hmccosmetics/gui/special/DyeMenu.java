package com.hibiscusmc.hmccosmetics.gui.special;

import com.hibiscusmc.hmccolor.HMCColorApi;
import com.hibiscusmc.hmccolor.gui.guis.Gui;
import com.hibiscusmc.hmccolor.gui.guis.GuiItem;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.misc.Adventure;
import com.hibiscusmc.hmccosmetics.util.misc.Placeholder;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class DyeMenu {

    // Yes, I do know how tacted on this feels.


    public static void openMenu(CosmeticUser user, Cosmetic cosmetic) {

        ItemStack originalItem = user.getUserCosmeticItem(cosmetic);
        if (originalItem == null || !cosmetic.isDyable()) return;

        Player player = user.getPlayer();
        HMCColorApi hmcColorApi = new HMCColorApi();
        Gui gui = hmcColorApi.getColorMenu();
        gui.updateTitle(Placeholder.applyPapiPlaceholders(player, "Dyeing Menu"));
        gui.setItem(19, new GuiItem(originalItem));
        gui.setDefaultTopClickAction(event -> {
            if (event.getSlot() == 25) {
                //TODO Color the cosmetic and apply it to the player
                player.closeInventory();
            } else event.setCancelled(true);
        });

        gui.setPlayerInventoryAction(event -> event.setCancelled(true));
        gui.setCloseGuiAction(event -> player.closeInventory());
        gui.open(player);
    }
}
