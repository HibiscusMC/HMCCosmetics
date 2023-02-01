package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class ActionCosmeticShow extends Action {

    public ActionCosmeticShow() {
        super("show");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        if (user.getHidden()) {
            if (user.getHiddenReason() != CosmeticUser.HiddenReason.ACTION && user.getHiddenReason() != CosmeticUser.HiddenReason.COMMAND) return; // Do not hide if its already off for WG
            user.showCosmetics();
        }
    }
}
