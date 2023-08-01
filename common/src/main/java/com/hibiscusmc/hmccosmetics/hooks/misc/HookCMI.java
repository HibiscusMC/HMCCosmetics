package com.hibiscusmc.hmccosmetics.hooks.misc;

import com.Zrips.CMI.events.CMIPlayerUnVanishEvent;
import com.Zrips.CMI.events.CMIPlayerVanishEvent;
import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link com.Zrips.CMI.CMI CMI}
 */
public class HookCMI extends Hook {
    public HookCMI() {
        super("CMI");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerVanish(@NotNull CMIPlayerVanishEvent event) {
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerShow(@NotNull CMIPlayerUnVanishEvent event) {
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.showCosmetics();
    }
}
