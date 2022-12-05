package com.hibiscusmc.hmccosmetics.nms;

import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBackpackType;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.entities.BalloonEntity;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface NMSHandler {

    int getNextEntityId();

    Entity getEntity(int entityId);

    Entity getInvisibleArmorstand(Location loc);

    Entity getMEGEntity(Location loc);

    Entity spawnBackpack(CosmeticUser user, CosmeticBackpackType cosmeticBackpackType);

    BalloonEntity spawnBalloon(CosmeticUser user, CosmeticBalloonType cosmeticBalloonType);

    void sendPacket(Player player, Packet packet);

    default boolean getSupported () {
        return false;
    }
}
