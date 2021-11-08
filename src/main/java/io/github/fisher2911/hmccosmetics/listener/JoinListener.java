package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public JoinListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        this.userManager.add(event.getPlayer());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        this.userManager.remove(event.getPlayer().getUniqueId());
    }
}
