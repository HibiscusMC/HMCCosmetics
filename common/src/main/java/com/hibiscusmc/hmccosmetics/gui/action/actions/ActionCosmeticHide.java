package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class ActionCosmeticHide extends Action {

    public ActionCosmeticHide() {
        super("hide");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        if (!user.getHidden()) {
            user.hideCosmetics(CosmeticUser.HiddenReason.ACTION);
            return;
        }
    }
}
