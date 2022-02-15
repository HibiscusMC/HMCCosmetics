package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityMountEvent;

public class CosmeticFixListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    public CosmeticFixListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onEntityMount(final EntityMountEvent event) {
        if (!(event.getEntity() instanceof final Player player)) {
            return;
        }
        this.fixCosmetics(player);
    }

    @EventHandler
    public void onOffhandSwap(final PlayerSwapHandItemsEvent event) {
        final ItemStack offHand = event.getOffHandItem();
        if (offHand != null && offHand.getType() != Material.AIR) {
            return;
        }
        this.fixCosmetics(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        final ItemStack itemStack = event.getItemInHand();

        if (itemStack.getAmount() > 1) {
            return;
        }

        this.fixCosmetics(event.getPlayer());
    }

    private void fixCosmetics(final Player player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin,
                () -> this.userManager.updateCosmetics(player.getUniqueId()), 2);
    }

}
