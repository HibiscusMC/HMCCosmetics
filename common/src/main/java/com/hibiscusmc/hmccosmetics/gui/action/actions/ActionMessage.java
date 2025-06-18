package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionMessage extends Action {

    public ActionMessage() {
        super("message");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        MessagesUtil.sendMessageNoKey(viewer, raw);
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}
