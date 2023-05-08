package com.hibiscusmc.hmccosmetics.hooks.misc;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import me.libraryaddict.disguise.events.DisguiseEvent;
import me.libraryaddict.disguise.events.UndisguiseEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

public class HookLibsDisguises extends Hook {
    public HookLibsDisguises() {
        super("LibsDisguises");
    }

    @EventHandler
    public void onPlayerVanish(@NotNull DisguiseEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    @EventHandler
    public void onPlayerShow(@NotNull UndisguiseEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.showCosmetics();
    }
}
