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
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.util.List;
import java.util.Optional;

public class ClickListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    private final List<Integer> equipmentSlots = List.of(39, 40);

    public ClickListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onCosmeticClick(final InventoryClickEvent event) {
        final HumanEntity player = event.getWhoClicked();
        if (!(player instanceof Player)) return;
        this.fixInventory((Player) player);
    }

    @EventHandler
    public void onCosmeticClick(final InventoryDragEvent event) {
        final HumanEntity player = event.getWhoClicked();
        if (!(player instanceof Player)) return;
        this.fixInventory((Player) player);
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        final HumanEntity player = event.getPlayer();
        this.userManager.get(player.getUniqueId()).ifPresent(this::doRunnable);
    }

    private void fixInventory(final Player player) {
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        this.doRunnable(optionalUser.get());
    }

    private void doRunnable(final User user) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                this.plugin, () -> this.userManager.updateCosmetics(user),
                1);
    }

}
