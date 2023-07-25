package com.hibiscusmc.hmccosmetics.nms;

import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.manager.UserBalloonManager;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface NMSHandler {

    int getNextEntityId();

    Entity getEntity(int entityId);

    Entity getHMCArmorStand(Location loc);

    ArmorStand getMEGEntity(Location loc);

    Entity spawnBackpack(CosmeticUser user, CosmeticBackpackType cosmeticBackpackType);

    Entity spawnDisplayEntity(Location location, String text);

    UserBalloonManager spawnBalloon(CosmeticUser user, CosmeticBalloonType cosmeticBalloonType);

    void equipmentSlotUpdate(
            int entityId,
            CosmeticUser user,
            CosmeticSlot cosmeticSlot,
            List<Player> sendTo
    );

    void slotUpdate(
            Player player,
            int slot
    );

    void equipmentSlotUpdate(
            int entityId,
            org.bukkit.inventory.EquipmentSlot slot,
            ItemStack item,
            List<Player> sendTo
    );

    void hideNPCName(
            Player player,
            String NPCName);

    default boolean getSupported () {
        return false;
    }
}
