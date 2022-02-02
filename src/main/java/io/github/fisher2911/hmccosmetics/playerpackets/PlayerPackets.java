package io.github.fisher2911.hmccosmetics.playerpackets;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface PlayerPackets {

    PacketContainer[] getSpawnPacket(final Location location, final Player player, final UUID uuid, final int entityId);
    PacketContainer getRemovePacket(final Player player, final UUID uuid, final int entityId);

}
