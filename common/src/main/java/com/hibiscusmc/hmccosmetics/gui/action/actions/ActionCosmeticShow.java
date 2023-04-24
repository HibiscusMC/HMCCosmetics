package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

public class ActionCosmeticShow extends Action {

    public ActionCosmeticShow() {
        super("show");
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        if (!user.getHidden()) return;

        // Do not hide if it's already off for WG
        if (user.getHiddenReason() != CosmeticUser.HiddenReason.ACTION && user.getHiddenReason() != CosmeticUser.HiddenReason.COMMAND) return;
        user.showCosmetics();
    }
}
