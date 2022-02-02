package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public RespawnListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            final Player player = event.getPlayer();
            final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());
            optionalUser.ifPresent(user -> {
                user.despawnAttached();
                this.userManager.updateCosmetics(user);
                this.userManager.setItem(user, user.getPlayerArmor().getHat());
            });
        }, 1);
    }

}
