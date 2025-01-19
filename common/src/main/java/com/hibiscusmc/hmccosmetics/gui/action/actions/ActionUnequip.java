package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.apache.commons.lang3.EnumUtils;

public class ActionUnequip extends Action {

    public ActionUnequip() {
        super("unequip");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        if (!CosmeticSlot.contains(raw)) return;

        CosmeticSlot slot = CosmeticSlot.valueOf(raw);
        user.removeCosmeticSlot(slot);
    }
}
