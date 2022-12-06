package com.hibiscusmc.hmccosmetics.gui.special;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.misc.Adventure;
import com.hibiscusmc.hmccosmetics.util.misc.Placeholder;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DyeMenu {

    // Yes, I do know how tacted on this feels.


    public static void openMenu(CosmeticUser user, Cosmetic cosmetic) {

        ItemStack originalItem = null;

        if (cosmetic instanceof CosmeticBackpackType) originalItem = ((CosmeticBackpackType) cosmetic).getBackpackItem();
        if (cosmetic instanceof CosmeticArmorType) originalItem = ((CosmeticArmorType) cosmetic).getCosmeticItem();
        if (originalItem == null) return;

        Player player = user.getPlayer();
        final Component component = Adventure.MINI_MESSAGE.deserialize(Placeholder.applyPapiPlaceholders(player, "Dying Menu"));
        Gui gui = Gui.gui().
                title(component).
                rows(6).
                create();

        gui.setDefaultClickAction(event -> event.setCancelled(true));

        gui.setItem(1, ItemBuilder.from(originalItem).asGuiItem());
        GuiItem guiItem = ItemBuilder.from(originalItem).asGuiItem();

        gui.open(player);

    }


}
