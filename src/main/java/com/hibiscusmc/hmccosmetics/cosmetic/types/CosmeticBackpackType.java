package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class CosmeticBackpackType extends Cosmetic {

    private ItemStack backpackItem;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        this.backpackItem = generateItemStack(config.node("item"));
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        Location loc = player.getLocation().clone();

        if (loc.getBlock().getType().equals(Material.NETHER_PORTAL)) {
            user.hideBackpack();
            return;
        }

        if (loc.getWorld() != user.getBackpackEntity().getBukkitLivingEntity().getWorld()) {
            user.getBackpackEntity().getBukkitLivingEntity().teleport(loc);
        }

        user.getBackpackEntity().moveTo(loc.getX(), loc.getY(), loc.getZ());

        if (player.getPassengers().isEmpty()) {
            user.getBackpackEntity().getBukkitLivingEntity().teleport(loc);
            //player.addPassenger(user.getBackpackEntity().getBukkitEntity());
        }

        user.showBackpack();
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
