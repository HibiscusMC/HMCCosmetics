package io.github.fisher2911.hmccosmetics.listener;

import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import io.github.fisher2911.hmccosmetics.user.User;
import io.github.fisher2911.hmccosmetics.user.UserManager;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ClickListener implements Listener {

    private final HMCCosmetics plugin;
    private final UserManager userManager;

    final Map<EquipmentSlot, Set<Material>> ARMOR_ITEMS = Map.of(
            EquipmentSlot.HEAD, EnumSet.of(
                    Material.LEATHER_HELMET,
                    Material.CHAINMAIL_HELMET,
                    Material.IRON_HELMET,
                    Material.GOLDEN_HELMET,
                    Material.DIAMOND_HELMET,
                    Material.NETHERITE_HELMET,
                    Material.TURTLE_HELMET
            ),
            EquipmentSlot.CHEST, EnumSet.of(
                    Material.LEATHER_CHESTPLATE,
                    Material.CHAINMAIL_CHESTPLATE,
                    Material.IRON_CHESTPLATE,
                    Material.GOLDEN_CHESTPLATE,
                    Material.DIAMOND_CHESTPLATE,
                    Material.NETHERITE_CHESTPLATE,
                    Material.ELYTRA
            ),
            EquipmentSlot.LEGS, EnumSet.of(
                    Material.LEATHER_LEGGINGS,
                    Material.CHAINMAIL_LEGGINGS,
                    Material.IRON_LEGGINGS,
                    Material.GOLDEN_LEGGINGS,
                    Material.DIAMOND_LEGGINGS,
                    Material.NETHERITE_LEGGINGS
            ),
            EquipmentSlot.FEET, EnumSet.of(
                    Material.LEATHER_BOOTS,
                    Material.CHAINMAIL_BOOTS,
                    Material.IRON_BOOTS,
                    Material.GOLDEN_BOOTS,
                    Material.DIAMOND_BOOTS,
                    Material.NETHERITE_BOOTS
            )
    );

    public ClickListener(final HMCCosmetics plugin) {
        this.plugin = plugin;
        this.userManager = this.plugin.getUserManager();
    }

    @EventHandler
    public void onArmorSlotClick(final InventoryClickEvent event) {
        final int slot = event.getSlot();
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        if (slot >= 36 && slot <= 40) {
            this.fixInventory(player);
        }
    }

    @EventHandler
    public void onBlockClick(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        if (block != null && block.getType().isInteractable() && !player.isSneaking()) return;
        final ItemStack clickedWith = event.getItem();
        if (clickedWith == null) return;
        this.checkFix(player, -1, clickedWith);
    }

    @EventHandler
    public void onShiftClick(final InventoryClickEvent event) {
        if (event.getClick() != ClickType.SHIFT_LEFT &&
                event.getClick() != ClickType.SHIFT_RIGHT) return;
        if (!(event.getWhoClicked() instanceof final Player player)) return;
        final ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        this.checkFix(player, -1, clicked);
    }

    @EventHandler
    public void onCosmeticClick(final InventoryDragEvent event) {
        final HumanEntity player = event.getWhoClicked();
        if (!(player instanceof Player)) {
            return;
        }
//        this.fixInventory((Player) player);
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

    private void checkFix(final Player player, final int clickedSlot, final ItemStack itemStack) {
        final EquipmentSlot slot = this.getArmorSlot(itemStack.getType());
        if (slot == null) return;
        final ItemStack wearing = player.getEquipment().getItem(slot);
        if (wearing == null) return;
        if (wearing.getType() == Material.AIR || (clickedSlot >= 39 && clickedSlot <= 40)) {
            this.fixInventory(player);
        }
    }

    @Nullable
    private EquipmentSlot getArmorSlot(final Material material) {
        for (final EquipmentSlot slot : EquipmentSlot.values()) {
            final Set<Material> armorItems = ARMOR_ITEMS.get(slot);
            if (armorItems == null) continue;
            if (material == null) continue;
            if (armorItems.contains(material)) return slot;
        }
        return null;
    }

}
