package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ActionMenu extends Action {

    public ActionMenu() {
        super("menu");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        boolean ignorePermission = false;

        raw = raw.replaceAll(" ", ""); // Removes all spaces

        if (raw.contains("-o")) {
            raw = raw.replaceAll("-o", "");
            ignorePermission = true;
        }

        if (!Menus.hasMenu(raw)) {
            MessagesUtil.sendDebugMessages("Invalid Action Menu -> " + raw, Level.WARNING);
            return;
        }

        Menu menu = Menus.getMenu(raw);
        MessagesUtil.sendDebugMessages(raw + " | " + ignorePermission);
        menu.openMenu(viewer, cosmeticHolder, ignorePermission);
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}
