package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CosmeticArmorType extends Cosmetic {

    private final EquipmentSlot equipSlot;

    public CosmeticArmorType(String id, ConfigurationNode config) {
        super(id, config);

        EquipmentSlot slot = HMCCInventoryUtils.getEquipmentSlot(getSlot());
        if (slot == null) {
            // Hypothetically it shouldn't be null, but it was happening on some random servers? Adding this just in case
            throw new IllegalArgumentException("Invalid slot for cosmetic armor type: " + getSlot() + " in " + id + " cosmetic config.");
        }
        this.equipSlot = slot;
    }

    @Override
    public void update(@NotNull CosmeticUser user) {
        if (user.getUserEmoteManager().isPlayingEmote() || user.isInWardrobe()) return;
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        if (entity == null) return;
        if (Settings.getSlotOption(equipSlot).isRequireEmpty() && entity instanceof HumanEntity humanEntity) {
            if (!humanEntity.getInventory().getItem(equipSlot).getType().isAir()) return;
        }
        ItemStack item = getItem(user);
        if (item == null) return;
        PacketManager.equipmentSlotUpdate(entity.getEntityId(), equipSlot, item, HMCCPacketManager.getViewers(entity.getLocation()));
    }

    public ItemStack getItem(@NotNull CosmeticUser user) {
        return getItem(user, user.getUserCosmeticItem(this));
    }

    public ItemStack getItem(@NotNull CosmeticUser user, ItemStack cosmeticItem) {
        if (!(user.getEntity() instanceof HumanEntity humanEntity)) return null;
        if (Settings.getSlotOption(equipSlot).isAddEnchantments()) {
            ItemStack equippedItem = humanEntity.getInventory().getItem(equipSlot);
            cosmeticItem.addUnsafeEnchantments(equippedItem.getEnchantments());
        }
        // Basically, if force offhand is off AND there is no item in an offhand slot, then the equipment packet to add the cosmetic
        return cosmeticItem;
    }

    @NotNull
    public EquipmentSlot getEquipSlot() {
        return this.equipSlot;
    }
}
