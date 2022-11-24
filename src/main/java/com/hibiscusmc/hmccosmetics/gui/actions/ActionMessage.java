package com.hibiscusmc.hmccosmetics.gui.actions;

import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class ActionMessage extends Action {

    public ActionMessage() {
        super("message");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        user.getPlayer().sendMessage(raw);
    }
}
