package io.github.fisher2911.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class DestroyPacket_1_16_R3 implements DestroyPacket {

    @Override
    public PacketContainer get(final int entityId) {
        final PacketContainer destroyPacket = new PacketContainer(
                PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntegerArrays().write(0, new int[]{entityId});

        return destroyPacket;
    }
}
