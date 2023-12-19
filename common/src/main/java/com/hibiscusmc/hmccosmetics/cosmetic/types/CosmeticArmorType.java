package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
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
        if (user.getUserEmoteManager().isPlayingEmote()) return;
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        if (entity == null) return;
        if (!Settings.isCosmeticForceOffhandCosmeticShow()
                && equipSlot.equals(EquipmentSlot.OFF_HAND)
                && ((user.getEntity() instanceof Player) && !user.getPlayer().getInventory().getItemInOffHand().getType().isAir())) return;
        ItemStack item = getItem(user);
        if (item == null) return;
        NMSHandlers.getHandler().equipmentSlotUpdate(entity.getEntityId(), equipSlot, item, PacketManager.getViewers(entity.getLocation()));
    }

    public ItemStack getItem(@NotNull CosmeticUser user) {
        return getItem(user, user.getUserCosmeticItem(this));
    }

    public ItemStack getItem(@NotNull CosmeticUser user, ItemStack cosmeticItem) {
        if (!(user.getEntity() instanceof HumanEntity humanEntity)) return null;
        if (Settings.getShouldAddEnchants(equipSlot)) {
            ItemStack equippedItem = humanEntity.getInventory().getItem(equipSlot);
            cosmeticItem.addUnsafeEnchantments(equippedItem.getEnchantments());
        }
        // Basically, if force offhand is off AND there is no item in an offhand slot, then the equipment packet to add the cosmetic
        return cosmeticItem;
    }

    public EquipmentSlot getEquipSlot() {
        return this.equipSlot;
    }
}
