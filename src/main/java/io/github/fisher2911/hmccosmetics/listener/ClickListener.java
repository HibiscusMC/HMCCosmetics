package io.github.fisher2911.hmccosmetics.listener;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;
import io.th0rgal.oraxen.utils.armorequipevent.ArmorEquipEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        this.fixInventory((Player) player, Set.of(event.getSlot()), event.getClickedInventory());
    }

    @EventHandler
    public void onCosmeticClick(final InventoryDragEvent event) {
        final HumanEntity player = event.getWhoClicked();
        if (!(player instanceof Player)) return;
        this.fixInventory((Player) player, event.getInventorySlots(), event.getView().getBottomInventory());
    }

    @EventHandler
    public void onArmorEquip(final PlayerArmorChangeEvent event) {
        final Player player = event.getPlayer();
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        this.doRunnable(optionalUser.get());
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        final HumanEntity player = event.getPlayer();
        this.userManager.get(player.getUniqueId()).ifPresent(this::doRunnable);
    }

    private void fixInventory(final Player player, final Set<Integer> slotsClicked, final Inventory inventory) {
        final Optional<User> optionalUser = this.userManager.get(player.getUniqueId());

        if (optionalUser.isEmpty()) {
            return;
        }

        if (inventory instanceof PlayerInventory) {
            final User user = optionalUser.get();
            for (int i : slotsClicked) {
                if (this.equipmentSlots.contains(i)) {
                    this.doRunnable(user);
                    break;
                }
            }
        }
    }

    private void doRunnable(final User user) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                this.plugin, () -> this.userManager.updateCosmetics(user),
                1);
    }

}
