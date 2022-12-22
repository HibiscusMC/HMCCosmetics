package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class CosmeticMainhandType extends Cosmetic {

    private ItemStack itemStack;

    public CosmeticMainhandType(String id, ConfigurationNode config) {
        super(id, config);

        this.itemStack = generateItemStack(config.node("item"));

        setEquipable(true);
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
                HMCCosmeticsPlugin.getInstance().getLogger().severe("Unable to create item for " + getId());
                return new ItemStack(Material.AIR);
            }
            return item;
        } catch (SerializationException e) {
            HMCCosmeticsPlugin.getInstance().getLogger().severe("Fatal error encountered for " + getId() + " regarding Serialization of item");
            throw new RuntimeException(e);
        }
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
