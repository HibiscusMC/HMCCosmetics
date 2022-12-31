package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class ActionCloseMenu extends Action {

    public ActionCloseMenu() {
        super("close");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        user.getPlayer().closeInventory();
    }
}
