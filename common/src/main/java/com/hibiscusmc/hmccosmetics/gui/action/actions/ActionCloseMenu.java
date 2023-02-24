package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

public class ActionCloseMenu extends Action {

    public ActionCloseMenu() {
        super("close");
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        user.getPlayer().closeInventory();
    }
}
