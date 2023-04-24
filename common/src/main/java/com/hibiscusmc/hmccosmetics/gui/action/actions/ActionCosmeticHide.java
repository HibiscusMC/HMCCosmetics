package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

public class ActionCosmeticHide extends Action {

    public ActionCosmeticHide() {
        super("hide");
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        if (user.getHidden()) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.ACTION);
    }
}
