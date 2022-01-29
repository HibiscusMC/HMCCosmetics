package io.github.fisher2911.hmccosmetics.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PacketManager {

    public static PacketContainer getEntitySpawnPacket(final Location location, final int entityId,
            final EntityType entityType) {
        final PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        // Entity ID
        packet.getIntegers().write(0, entityId);
        // Entity Type
//        packet.getIntegers().write(6, 78);
        // Set yaw pitch
        packet.getIntegers().write(4, (int) location.getPitch());
        packet.getIntegers().write(5, (int) location.getYaw());
        // Set location
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        // Set UUID
        packet.getUUIDs().write(0, UUID.randomUUID());

        packet.getEntityTypeModifier().write(0, entityType);

        return packet;
    }

    public static PacketContainer getEquipmentPacket(
            final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList,
            final int entityId
    ) {

        final PacketContainer armorPacket = new PacketContainer(
                PacketType.Play.Server.ENTITY_EQUIPMENT);
        armorPacket.getIntegers().write(0, entityId);
        armorPacket.getSlotStackPairLists().write(0, equipmentList);

        return armorPacket;
    }

    public static PacketContainer getRotationPacket(final int entityId, final Location location) {
        final PacketContainer rotationPacket = new PacketContainer(
                PacketType.Play.Server.ENTITY_HEAD_ROTATION);

        rotationPacket.getIntegers().write(0, entityId);
        rotationPacket.getBytes().write(0, (byte) (location.getYaw() * 256 / 360));

        return rotationPacket;
    }

    public static PacketContainer getRidingPacket(final int mountId, final int passengerId) {
        final PacketContainer ridingPacket = new PacketContainer(PacketType.Play.Server.MOUNT);
        ridingPacket.
                getIntegers().
                write(0, mountId);
        ridingPacket.getIntegerArrays().write(0, new int[]{passengerId});

        return ridingPacket;
    }

    public static PacketContainer getEntityDestroyPacket(final int entityId) {
        final PacketContainer destroyPacket = new PacketContainer(
                PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getModifier().write(0, new IntArrayList(new int[]{entityId}));

        return destroyPacket;
    }

    public static void sendPacket(final Player to, final PacketContainer... packets) {
        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        try {
            for (final PacketContainer packet : packets) {
                protocolManager.sendServerPacket(to, packet);
            }
        } catch (final InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    public static void sendPacketToOnline(final PacketContainer... packets) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            sendPacket(player, packets);
        }
    }

}
