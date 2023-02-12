package com.hibiscusmc.hmccosmetics.hooks.misc;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class HookSuperVanish extends Hook implements Listener {

    // NOTE: Supervanish and Premium Vanish both use the same api

    public HookSuperVanish() {
        super("SuperVanish");
    }

    @EventHandler
    public void onPlayerVanish(PlayerHideEvent event) {
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    @EventHandler
    public void onPlayerShow(PlayerShowEvent event) {
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.showCosmetics();
    }
}
