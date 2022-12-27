package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Level;

public class CosmeticArmorType extends Cosmetic {

    private ItemStack itemStack;
    private EquipmentSlot equipSlot;

    public CosmeticArmorType(String id, ConfigurationNode config) {
        super(id, config);

        this.itemStack = generateItemStack(config.node("item"));
        this.equipSlot = InventoryUtils.getEquipmentSlot(getSlot());

        setEquipable(true);
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        PacketManager.equipmentSlotUpdate(player, getSlot(), PlayerUtils.getNearbyPlayers(player));
    }

    public ItemStack getCosmeticItem() {
        return this.itemStack.clone();
    }

    public EquipmentSlot getEquipSlot() {
        return this.equipSlot;
    }

    private ItemStack generateItemStack(ConfigurationNode config) {
        try {
            ItemStack item = ItemSerializer.INSTANCE.deserialize(ItemStack.class, config);
            if (item == null) {
                MessagesUtil.sendDebugMessages("Unable to create item for " + getId(), Level.SEVERE);
                return new ItemStack(Material.AIR);
            }
            return item;
        } catch (SerializationException e) {
            MessagesUtil.sendDebugMessages("Fatal error encountered for " + getId() + " regarding Serialization of item", Level.SEVERE);
            throw new RuntimeException(e);
        }
    }
}
