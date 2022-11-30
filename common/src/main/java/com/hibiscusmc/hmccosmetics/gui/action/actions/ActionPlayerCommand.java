package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class ActionPlayerCommand extends Action {

    public ActionPlayerCommand() {
        super("player-command");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        user.getPlayer().performCommand(raw);
    }
}