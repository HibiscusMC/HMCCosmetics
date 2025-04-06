package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionCosmeticToggle extends Action {

    public ActionCosmeticToggle() {
        super("toggle");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        if (!(cosmeticHolder instanceof CosmeticUser user)) return;
        if (user.isHidden()) {
            if (!user.isHidden(CosmeticUser.HiddenReason.ACTION) && !user.isHidden(CosmeticUser.HiddenReason.COMMAND)) return;
            user.showCosmetics(CosmeticUser.HiddenReason.ACTION);
            return;
        }

        user.hideCosmetics(CosmeticUser.HiddenReason.ACTION);
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}
