package com.hibiscusmc.hmccosmetics.util.packets;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class BasePacket {

    public static void sendPacket(Player player, Packet<?> packet) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ServerPlayerConnection connection = serverPlayer.connection;
        connection.send(packet);
    }

    public static void sendPacket(Player player, PacketContainer packet) {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
    }
}
