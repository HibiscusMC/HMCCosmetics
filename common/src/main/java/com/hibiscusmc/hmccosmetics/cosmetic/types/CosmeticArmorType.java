package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
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
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        if (entity == null) return;
        if (user.getUserEmoteManager().isPlayingEmote()) return; // There has to be a better way of doing this...
        ItemStack cosmeticItem = user.getUserCosmeticItem(this);
        if (!(entity instanceof HumanEntity humanEntity)) return;
        ItemStack equippedItem = humanEntity.getInventory().getItem(equipSlot);
        if (Settings.getShouldAddEnchants(equipSlot)) {
            cosmeticItem.addUnsafeEnchantments(equippedItem.getEnchantments());
        }
        // Basically, if force offhand is off AND there is no item in an offhand slot, then the equipment packet to add the cosmetic
        if (!Settings.isCosmeticForceOffhandCosmeticShow()
                && equipSlot.equals(EquipmentSlot.OFF_HAND)
                && ((entity instanceof Player) && !user.getPlayer().getInventory().getItemInOffHand().getType().isAir())) return;
        NMSHandlers.getHandler().equipmentSlotUpdate(entity.getEntityId(), equipSlot, cosmeticItem, PacketManager.getViewers(entity.getLocation()));
    }

    public EquipmentSlot getEquipSlot() {
        return this.equipSlot;
    }
}
