package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Level;

public class CosmeticMainhandType extends Cosmetic {

    private ItemStack itemStack;

    public CosmeticMainhandType(String id, ConfigurationNode config) {
        super(id, config);

        this.itemStack = generateItemStack(config.node("item"));

    }

    @Override
    public void update(CosmeticUser user) {
        Player player = user.getPlayer();

        PacketManager.equipmentSlotUpdate(player.getEntityId(), user, getSlot(), PlayerUtils.getNearbyPlayers(player));

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

    @Override
    public ItemStack getItem() {
        return itemStack.clone();
    }
}
