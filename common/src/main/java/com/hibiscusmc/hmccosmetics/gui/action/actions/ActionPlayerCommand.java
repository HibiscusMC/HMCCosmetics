package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.hooks.Hooks;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import org.jetbrains.annotations.NotNull;

public class ActionPlayerCommand extends Action {

    public ActionPlayerCommand() {
        super("player-command");
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        user.getPlayer().performCommand(MessagesUtil.processStringNoKeyString(user.getPlayer(), Hooks.processPlaceholders(user.getPlayer(), raw)));
    }
}