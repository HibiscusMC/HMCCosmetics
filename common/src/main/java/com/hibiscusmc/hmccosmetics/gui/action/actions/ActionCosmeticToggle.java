package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

public class ActionCosmeticToggle extends Action {

    public ActionCosmeticToggle() {
        super("toggle");
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        if (user.isHidden()) {
            if (!user.isHidden(CosmeticUser.HiddenReason.ACTION) && !user.isHidden(CosmeticUser.HiddenReason.COMMAND)) return;
            user.showCosmetics(CosmeticUser.HiddenReason.ACTION);
            return;
        }

        user.hideCosmetics(CosmeticUser.HiddenReason.ACTION);
    }
}
