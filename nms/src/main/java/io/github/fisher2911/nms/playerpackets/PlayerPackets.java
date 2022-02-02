package io.github.fisher2911.nms.playerpackets;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerPackets {

    PacketContainer getSpawnPacket(final Location location, UUID uuid, final int entityId);
    PacketContainer getPlayerInfoPacket(final Player player, final UUID uuid);
    PacketContainer getRemovePacket(final Player player, final UUID uuid, final int entityId);

}
