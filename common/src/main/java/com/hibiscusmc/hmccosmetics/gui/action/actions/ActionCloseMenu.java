package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionCloseMenu extends Action {

    public ActionCloseMenu() {
        super("close");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        viewer.closeInventory();
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}
