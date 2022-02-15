package io.github.fisher2911.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class ArmorStandPackets_1_18_R1 implements ArmorStandPackets {

    @Override
    public PacketContainer getArmorStandMeta(final int armorStandId) {
        final PacketContainer metaContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        WrappedDataWatcher metaData = new WrappedDataWatcher();

        final WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);

        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer), (byte) (0x20));
        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, byteSerializer), (byte) (0x10));

        metaContainer.getIntegers().write(0, armorStandId);
        metaContainer.getWatchableCollectionModifier().write(0, metaData.getWatchableObjects());
        return metaContainer;
    }
}
