package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;

public class ActionMessage extends Action {

    public ActionMessage() {
        super("message");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        MessagesUtil.sendMessageNoKey(user.getPlayer(), raw);
    }
}
