package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

public class CosmeticArmorType extends Cosmetic {

    private final EquipmentSlot equipSlot;

    public CosmeticArmorType(String id, ConfigurationNode config) {
        super(id, config);

        this.equipSlot = InventoryUtils.getEquipmentSlot(getSlot());
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player == null) return;
        if (user.getUserEmoteManager().isPlayingEmote()) return; // There has to be a better way of doing this...
        ItemStack cosmeticItem = user.getUserCosmeticItem(this);
        if (equipSlot.equals(EquipmentSlot.OFF_HAND)) {
            if (!player.getInventory().getItemInOffHand().getType().isAir()) return;
        }
        ItemStack equippedItem = player.getInventory().getItem(equipSlot);
        if (Settings.getShouldAddEnchants(equipSlot)) {
            cosmeticItem.addUnsafeEnchantments(equippedItem.getEnchantments());
        }

        NMSHandlers.getHandler().equipmentSlotUpdate(player.getEntityId(), equipSlot, cosmeticItem, PacketManager.getViewers(player.getLocation()));
        //PacketManager.equipmentSlotUpdate(player, getSlot(), PacketManager.getViewers(player.getLocation())); Old method
    }

    public EquipmentSlot getEquipSlot() {
        return this.equipSlot;
    }
}
