package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetics;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ActionEquipRandom extends Action {

    public ActionEquipRandom() {
        super("equip-random");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        final CosmeticSlot slot = CosmeticSlot.valueOf(raw);
        if (slot == null) {
            MessagesUtil.sendDebugMessages("Equip Random does not have a valid slot for menu action!");
            return;
        }

        final List<Cosmetic> cosmetics = Cosmetics.values().stream().filter(cosmetic -> (cosmetic.getSlot() == slot) && cosmeticHolder.canEquipCosmetic(cosmetic)).toList();
        final Cosmetic cosmetic = cosmetics.get(ThreadLocalRandom.current().nextInt(cosmetics.size()));

        cosmeticHolder.addCosmetic(cosmetic);
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}