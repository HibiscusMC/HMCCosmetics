package io.github.fisher2911.nms;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PacketHelper {

    PacketContainer getPlayerSpawnPacket(final Location location, UUID uuid, final int entityId);
    PacketContainer getPlayerInfoPacket(final Player player, final UUID uuid);
    PacketContainer getPlayerRemovePacket(final Player player, final UUID uuid, final int entityId);
    PacketContainer getPlayerOverlayPacket(final int entityId);
    PacketContainer getDestroyPacket(final int entityId);
    PacketContainer getArmorStandMeta(final int armorStandId);
//    PacketContainer getGuiOpenPacket(final Player player);

}
