package com.hibiscusmc.hmccosmetics.util.packets;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;

public class BasePacket {

    public static void sendPacket(Player player, Packet<?> packet) {
        NMSHandlers.getHandler().sendPacket(player, packet);
    }

    public static void sendPacket(Player player, PacketContainer packet) {
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
    }
}
