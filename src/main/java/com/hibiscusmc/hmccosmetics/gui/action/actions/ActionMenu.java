package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class ActionMenu extends Action {

    public ActionMenu() {
        super("menu");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        if (!Menus.hasMenu(raw)) return;
        Menu menu = Menus.getMenu(raw);
        menu.openMenu(user);
    }
}
