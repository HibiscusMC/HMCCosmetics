package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.spongepowered.configurate.ConfigurationNode;

public class CosmeticArmorType extends Cosmetic {

    private EquipmentSlot equipSlot;

    public CosmeticArmorType(String id, ConfigurationNode config) {
        super(id, config);

        this.equipSlot = InventoryUtils.getEquipmentSlot(getSlot());
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (equipSlot.equals(EquipmentSlot.OFF_HAND)) {
            if (!player.getInventory().getItemInOffHand().getType().isAir()) return;
        }
        PacketManager.equipmentSlotUpdate(player, getSlot(), PacketManager.getViewers(player.getLocation()));
    }

    public EquipmentSlot getEquipSlot() {
        return this.equipSlot;
    }


}
