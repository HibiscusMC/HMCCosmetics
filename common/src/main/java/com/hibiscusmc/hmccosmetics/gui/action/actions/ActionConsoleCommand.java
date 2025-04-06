package com.hibiscusmc.hmccosmetics.gui.action.actions;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticHolder;
import com.hibiscusmc.hmccosmetics.gui.action.Action;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import me.lojosho.hibiscuscommons.hooks.Hooks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionConsoleCommand extends Action {

    public ActionConsoleCommand() {
        super("console-command");
    }

    @Override
    public void run(Player viewer, CosmeticHolder cosmeticHolder, String raw) {
        // todo: some of these generalizations may require more specific implementations, for example,
        // todo: what if we wanted to execute console commands only for the cosmetic holder?
        HMCCosmeticsPlugin.getInstance().getServer().dispatchCommand(Bukkit.getConsoleSender(), Hooks.processPlaceholders(viewer, raw));
    }

    @Override
    public void run(@NotNull CosmeticUser user, String raw) {
        run(user.getPlayer(), user, raw);
    }
}
