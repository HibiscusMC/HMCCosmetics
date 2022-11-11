package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.List;

public class CosmeticBackpackType extends Cosmetic {

    private ItemStack backpackItem;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        this.backpackItem = generateItemStack(config.node("item"));
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        List<Player> sendTo = PlayerUtils.getNearbyPlayers(player.getLocation());
        Location loc = player.getLocation();

        user.getBackpackEntity().getBukkitLivingEntity().setRotation(loc.getYaw(), loc.getPitch());

    }

    public ItemStack getBackpackItem() {
        return backpackItem;
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
}
