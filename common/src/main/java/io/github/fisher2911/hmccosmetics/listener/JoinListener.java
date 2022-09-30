package io.github.fisher2911.hmccosmetics.listener;

import com.comphenix.protocol.ProtocolLibrary;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.WardrobeSettings;
import io.github.fisher2911.hmccosmetics.database.Database;
import io.github.fisher2911.hmccosmetics.task.TaskChain;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Optional;

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
        this.database.loadUser(
                player,
                user -> new TaskChain(this.plugin).chain(
                        () -> this.userManager.add(user)
                ).chain(() -> {
                    final WardrobeSettings settings = this.plugin.getSettings().getWardrobeSettings();
                    if (settings.isAlwaysDisplay() && settings.getWardrobeLocation() != null) {
                        final Wardrobe wardrobe = user.getWardrobe();
                        wardrobe.setCurrentLocation(settings.getWardrobeLocation());
                        wardrobe.spawnFakePlayer(player);
                    }
                    // and finally, resend the cosmetics
                    Bukkit.getScheduler().runTaskLaterAsynchronously(HMCCosmetics.getPlugin(HMCCosmetics.class),
                            () -> {
                                this.userManager.updateCosmetics(user);
                            }, 20);
                }, true).execute());
    }

    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());
        optionalUser.ifPresent(user -> {
            final Wardrobe wardrobe = user.getWardrobe();

            user.despawnAttached();
            user.despawnBalloon();

            if (wardrobe.isActive()) {
                Bukkit.getScheduler().runTaskAsynchronously(
                        this.plugin,
                        () -> wardrobe.despawnFakePlayer(player, userManager)
                );
            }
        });
        this.userManager.remove(player.getUniqueId());
    }

}
