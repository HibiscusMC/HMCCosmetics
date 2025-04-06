package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.entity.Player;

public class ActionUnequip extends Action {

    public ActionUnequip() {
        super("unequip");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        if (!CosmeticSlot.contains(raw)) return;

        CosmeticSlot slot = CosmeticSlot.valueOf(raw);
        cosmeticHolder.removeCosmeticSlot(slot);
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}
