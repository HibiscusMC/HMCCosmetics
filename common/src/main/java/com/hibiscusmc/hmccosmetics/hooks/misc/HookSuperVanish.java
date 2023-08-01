package com.hibiscusmc.hmccosmetics.hooks.misc;

import com.hibiscusmc.hmccosmetics.hooks.Hook;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;

/**
 * A hook that integrates the plugin {@link de.myzelyam.api.vanish.VanishAPI Supervanish}
 *
 * @implSpec Supervanish and Premium Vanish both use the same api
 */
public class HookSuperVanish extends Hook {
    public HookSuperVanish() {
        super("SuperVanish");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerVanish(@NotNull PlayerHideEvent event) {
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.hideCosmetics(CosmeticUser.HiddenReason.PLUGIN);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerShow(@NotNull PlayerShowEvent event) {
        Player player = event.getPlayer();
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null) return;
        user.showCosmetics();
    }
}
