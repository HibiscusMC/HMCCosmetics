package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

public class MoveListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public MoveListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Optional<User> optional = this.userManager.get(player.getUniqueId());
        if (optional.isEmpty()) return;
        if (optional.get().getWardrobe().isCameraLocked()) event.setCancelled(true);
    }
}
