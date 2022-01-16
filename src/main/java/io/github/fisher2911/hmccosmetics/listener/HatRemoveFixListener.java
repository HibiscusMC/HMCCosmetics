package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.spigotmc.event.entity.EntityMountEvent;

public class HatRemoveFixListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public HatRemoveFixListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        this.fixHat(player);
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        this.fixHat(player);
    }

    @EventHandler
    public void test(final EntityMountEvent event) {
        if (!(event.getEntity() instanceof final Player player)) return;
        this.fixHat(player);
    }

    private void fixHat(final Player player) {
        Bukkit.getScheduler().runTaskLater(
                this.plugin,
                () -> this.userManager.get(player.getUniqueId()).ifPresent(this.userManager::setFakeHelmet),
                1);
    }
}
