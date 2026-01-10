package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.behavior.CosmeticUpdateBehavior;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.lojosho.hibiscuscommons.HibiscusCommonsPlugin;
import me.lojosho.shaded.configurate.ConfigurationNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CosmeticArmorType extends Cosmetic implements CosmeticUpdateBehavior {
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
    public void dispatchUpdate(@NotNull CosmeticUser user) {
        if (user.isInWardrobe()) return;
        Entity entity = Bukkit.getEntity(user.getUniqueId());
        if (entity == null) return;
        if (Settings.getSlotOption(equipSlot).isRequireEmpty() && entity instanceof HumanEntity humanEntity) {
            if (!humanEntity.getInventory().getItem(equipSlot).getType().isAir()) return;
        }
        ItemStack item = getItem(user);
        if (item == null) return;
        HMCCPacketManager.equipmentSlotUpdate(entity.getEntityId(), equipSlot, item, HMCCPacketManager.getViewers(entity.getLocation()));
    }

    public ItemStack getItem(@NotNull CosmeticUser user) {
        return getItem(user, user.getUserCosmeticItem(this));
    }

    public ItemStack getItem(@NotNull CosmeticUser user, ItemStack cosmeticItem) {
        Player player = user.getPlayer();
        if (player == null) return null;

        ItemStack physicalEquippedItem = player.getInventory().getItem(equipSlot);
        if (Settings.getSlotOption(equipSlot).isAddEnchantments()) {
            cosmeticItem.addUnsafeEnchantments(physicalEquippedItem.getEnchantments());
        }
        if (Settings.getSlotOption(equipSlot).isAddElytraComponent()
                && HibiscusCommonsPlugin.isOnPaper()
                && HMCCInventoryUtils.isGlider(physicalEquippedItem)) {
            cosmeticItem.editMeta(itemMeta -> itemMeta.setGlider(true));
        }
        if (Settings.getSlotOption(equipSlot).isItemDamagePassThrough()
        && HibiscusCommonsPlugin.isOnPaper()) {
            if (physicalEquippedItem.hasData(DataComponentTypes.MAX_DAMAGE))
                cosmeticItem.setData(DataComponentTypes.MAX_DAMAGE, physicalEquippedItem.getData(DataComponentTypes.MAX_DAMAGE));
            if (physicalEquippedItem.hasData(DataComponentTypes.DAMAGE))
                cosmeticItem.setData(DataComponentTypes.DAMAGE, physicalEquippedItem.getData(DataComponentTypes.DAMAGE));
        }
        // Basically, if force offhand is off AND there is no item in an offhand slot, then the equipment packet to add the cosmetic
        return cosmeticItem;
    }

    @NotNull
    public EquipmentSlot getEquipSlot() {
        return this.equipSlot;
    }
}
