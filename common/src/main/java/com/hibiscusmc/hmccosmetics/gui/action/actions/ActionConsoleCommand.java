package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import org.jetbrains.annotations.NotNull;

public class ActionConsoleCommand extends Action {

    public ActionConsoleCommand() {
        super("console-command");
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        HMCCosmeticsPlugin.getInstance().getServer().dispatchCommand(user.getPlayer(), Hooks.processPlaceholders(user.getPlayer(), raw));
    }
}
