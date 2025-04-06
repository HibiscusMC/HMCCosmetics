package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.entity.Player;

public class ActionEquip extends Action {

    public ActionEquip() {
        super("equip");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        Cosmetic cosmetic = Cosmetics.getCosmetic(raw);
        if (cosmetic == null) return;

        cosmeticHolder.addCosmetic(cosmetic);
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}
