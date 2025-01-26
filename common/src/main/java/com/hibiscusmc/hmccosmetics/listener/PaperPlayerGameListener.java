package com.hibiscusmc.hmccosmetics.listener;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class PaperPlayerGameListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerArmorEquip(PlayerArmorChangeEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        if (user.isInWardrobe()) return;
        user.updateCosmetic(slotTypeToCosmeticType(event.getSlotType()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerPostRespawnEvent event) {
        CosmeticUser user = CosmeticUsers.getUser(event.getPlayer());
        if (user == null) return;
        if (user.isInWardrobe()) return;
        if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) user.respawnBackpack();
    }

    private CosmeticSlot slotTypeToCosmeticType(PlayerArmorChangeEvent.SlotType slotType) {
        return switch (slotType) {
            case HEAD -> CosmeticSlot.HELMET;
            case FEET -> CosmeticSlot.BOOTS;
            case LEGS -> CosmeticSlot.LEGGINGS;
            case CHEST -> CosmeticSlot.CHESTPLATE;
            default -> null;
        };
    }

}
