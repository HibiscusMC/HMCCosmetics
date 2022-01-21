package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements Listener {

    private final HMCCosmetics plugin;
    private final Database database;
    private final UserManager userManager;

    public JoinListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.database = this.plugin.getDatabase();
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        this.database.loadUser(player.getUniqueId());

        this.userManager.resendCosmetics(player);
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {

        this.userManager.remove(event.getPlayer().getUniqueId());
        this.userManager.get(event.getPlayer().getUniqueId()).
                ifPresent(
                        this.database::saveUser
                );
    }
}
