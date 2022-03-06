package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.message.Messages;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.github.fisher2911.hmccosmetics.user.Wardrobe;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Optional;

public class PlayerShiftListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public PlayerShiftListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onPlayerShift(final PlayerToggleSneakEvent event) {
        final Player player = event.getPlayer();
        final Optional<User> userOptional = this.userManager.get(player.getUniqueId());

        if (!event.isSneaking()) return;

        if (userOptional.isEmpty()) return;

        final User user = userOptional.get();
        final Wardrobe wardrobe = user.getWardrobe();

        if (!wardrobe.isActive()) return;

        wardrobe.despawnFakePlayer(player, userManager);
        this.plugin.getSettings().getWardrobeSettings().playCloseSound(player);
        this.plugin.getMessageHandler().sendMessage(
                player,
                Messages.CLOSED_WARDROBE
        );
    }
}
