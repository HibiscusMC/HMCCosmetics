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

    public CosmeticMainhandType(String id, ConfigurationNode config) {
        super(id, config);
    }

    @Override
    public void update(CosmeticUser user) {
        Player player = user.getPlayer();

        PacketManager.equipmentSlotUpdate(player.getEntityId(), user, getSlot(), PlayerUtils.getNearbyPlayers(player));

    }
}
