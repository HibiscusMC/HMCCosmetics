package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class ActionConsoleCommand extends Action {

    public ActionConsoleCommand() {
        super("console-command");
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        HMCCosmeticsPlugin.getInstance().getServer().dispatchCommand(Bukkit.getConsoleSender(), Hooks.processPlaceholders(user.getPlayer(), raw));
    }
}
