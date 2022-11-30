package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;

public class ActionConsoleCommand extends Action {

    public ActionConsoleCommand() {
        super("console-command");
    }

    @Override
    public void run(CosmeticUser user, String raw) {
        HMCCosmeticsPlugin.getInstance().getServer().dispatchCommand(user.getPlayer(), raw);
    }

}
