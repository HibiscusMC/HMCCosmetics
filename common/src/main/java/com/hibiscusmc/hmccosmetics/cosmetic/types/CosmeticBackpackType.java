package com.hibiscusmc.hmccosmetics.cosmetic.types;

import com.hibiscusmc.hmccosmetics.config.serializer.ItemSerializer;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Level;

public class CosmeticBackpackType extends Cosmetic {

    ConfigurationNode config;

    public CosmeticBackpackType(String id, ConfigurationNode config) {
        super(id, config);

        this.config = config;
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = Bukkit.getPlayer(user.getUniqueId());
        Location loc = player.getLocation().clone();

        if (loc.getWorld() != user.getBackpackEntity().getWorld()) {
            user.getBackpackEntity().teleport(loc);
        }

        user.getBackpackEntity().teleport(loc);

        PacketManager.sendRidingPacket(player.getEntityId(), user.getBackpackEntity().getEntityId(), loc);

        user.getBackpackEntity().setRotation(loc.getYaw(), loc.getPitch());
        user.showBackpack();
    }
}
