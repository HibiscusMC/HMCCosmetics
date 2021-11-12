package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;

import java.util.Optional;

public class ClickListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public ClickListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onHelmetClick(final InventoryClickEvent event) {
        final HumanEntity player = event.getWhoClicked();
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        if (event.getClickedInventory() instanceof final PlayerInventory inventory) {
            final User user = optionalUser.get();
            if (event.getSlot() == 39) {
                Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                    this.userManager.updateHat(user);
                }, 1);
            }
        }
    }

}
