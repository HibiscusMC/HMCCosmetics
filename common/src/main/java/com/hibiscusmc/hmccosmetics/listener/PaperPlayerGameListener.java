package com.hibiscusmc.hmccosmetics.listener;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

public class PaperPlayerGameListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CosmeticUser user = CosmeticUsers.getUser(player);
        if (user == null || user.isInWardrobe()) return;

        boolean armorChanged = false;
        for (EquipmentSlot slot : event.getEquipmentChanges().keySet()) {
            CosmeticSlot cosmeticSlot = equipmentSlotToCosmeticType(slot);
            if (cosmeticSlot == null) continue;

            armorChanged = true;
            user.updateCosmetic(cosmeticSlot);
        }

        // Selecting another hotbar slot also fires this event for the main hand. Resyncing the
        // entire inventory in that case can overwrite client-side creative inventory changes.
        if (!armorChanged) return;

        // Creative inventory actions are client-authoritative. Sending the server inventory back
        // after an armor change can overwrite the item being created or moved by the client.
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), player::updateInventory, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerPostRespawnEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        if (user.isInWardrobe()) return;
        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) user.respawnBackpack();
    }

    private CosmeticSlot equipmentSlotToCosmeticType(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> CosmeticSlot.HELMET;
            case FEET -> CosmeticSlot.BOOTS;
            case LEGS -> CosmeticSlot.LEGGINGS;
            case CHEST -> CosmeticSlot.CHESTPLATE;
            default -> null;
        };
    }

}
