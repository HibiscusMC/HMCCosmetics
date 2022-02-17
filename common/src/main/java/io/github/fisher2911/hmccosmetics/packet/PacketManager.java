package io.github.fisher2911.hmccosmetics.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.Pair;
import io.github.fisher2911.nms.ArmorStandPackets;
import io.github.fisher2911.nms.ArmorStandPackets_1_16_R3;
import io.github.fisher2911.nms.ArmorStandPackets_1_17_R1;
import io.github.fisher2911.nms.ArmorStandPackets_1_18_R1;
import io.github.fisher2911.nms.DestroyPacket;
import io.github.fisher2911.nms.DestroyPacket_1_16_R3;
import io.github.fisher2911.nms.DestroyPacket_1_17_R1;
import io.github.fisher2911.nms.DestroyPacket_1_18_R1;
import io.github.fisher2911.nms.PlayerPackets;
import io.github.fisher2911.nms.PlayerPackets_1_16_R3;
import io.github.fisher2911.nms.PlayerPackets_1_17_R1;
import io.github.fisher2911.nms.PlayerPackets_1_18_R1;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

public class PacketManager {

    private static final PlayerPackets playerPackets;
    private static final DestroyPacket destroyPacket;
    private static final ArmorStandPackets armorStandPackets;

    static {
        final String version = Bukkit.getVersion();
        System.out.println("Version: " + Bukkit.getVersion());
        if (version.contains("1.16")) {
            playerPackets = new PlayerPackets_1_16_R3();
            destroyPacket = new DestroyPacket_1_16_R3();
            armorStandPackets = new ArmorStandPackets_1_16_R3();
        } else if (version.contains("1.17")) {
            playerPackets = new PlayerPackets_1_17_R1();
            destroyPacket = new DestroyPacket_1_17_R1();
            armorStandPackets = new ArmorStandPackets_1_17_R1();
        } else if (version.contains("1.18")) {
            playerPackets = new PlayerPackets_1_18_R1();
            destroyPacket = new DestroyPacket_1_18_R1();
            armorStandPackets = new ArmorStandPackets_1_18_R1();
        } else {
            playerPackets = null;
            destroyPacket = null;
            armorStandPackets = null;
        }

    }

    public static PacketContainer getArmorStandMetaContainer(final int armorStandId) {
        if (armorStandPackets == null)
            throw new IllegalStateException("This cannot be used in version: " + Bukkit.getVersion());
        return armorStandPackets.getArmorStandMeta(armorStandId);
    }

    public static PacketContainer getEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType) {
        return getEntitySpawnPacket(location, entityId, entityType, UUID.randomUUID());
    }

    public static PacketContainer getEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid) {
        final PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);

        // Entity ID
        packet.getIntegers().write(0, entityId);
        // Entity Type
//        packet.getIntegers().write(6, 78);
        // Set location
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        // Set yaw pitch
        packet.getIntegers().write(4, (int) location.getPitch());
        packet.getIntegers().write(5, (int) location.getYaw());
        // Set UUID
        packet.getUUIDs().write(0, uuid);

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

    public static PacketContainer getLookPacket(final int entityId, final Location location) {
        final PacketContainer rotationPacket = new PacketContainer(
                PacketType.Play.Server.ENTITY_LOOK);

        rotationPacket.getIntegers().write(0, entityId);
        rotationPacket.getBytes().write(0, (byte) (location.getYaw() * 256 / 360));
//        rotationPacket.getBytes().write(1, (byte) (location.getPitch() * 256 / 360));

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
        return destroyPacket.get(entityId);
    }

    public static PacketContainer getSoundPacket(
            final Player player,
            final Location location,
            final MinecraftKey name,
            final float volume,
            final float pitch,
            final EnumWrappers.SoundCategory soundCategory
    ) {
        final var manager = ProtocolLibrary.getProtocolManager();
        final var packet = manager.createPacket(PacketType.Play.Server.CUSTOM_SOUND_EFFECT);

        packet.getMinecraftKeys()
                .write(
                        0,
                        name
                );

        packet.getSoundCategories()
                .write(0, EnumWrappers.SoundCategory.valueOf(soundCategory.name()));

        packet.getIntegers()
                .write(0, location.getBlockX() * 8)
                .write(
                        1, location.getBlockY() * 8
                )
                .write(2, location.getBlockZ() * 8);

        packet.getFloat()
                .write(0, volume)
                .write(1, pitch);

        return packet;
    }

    public static PacketContainer getFakePlayerSpawnPacket(final Location location, final UUID uuid, final int entityId) throws IllegalStateException {
        if (playerPackets == null)
            throw new IllegalStateException("This cannot be used in version: " + Bukkit.getVersion());
        return playerPackets.getSpawnPacket(location, uuid, entityId);
    }

    public static PacketContainer getFakePlayerInfoPacket(final Player player, final UUID uuid) throws IllegalStateException {
        if (playerPackets == null)
            throw new IllegalStateException("This cannot be used in version: " + Bukkit.getVersion());
        return playerPackets.getPlayerInfoPacket(player, uuid);
    }

    public static PacketContainer getPlayerOverlayPacket(final int playerId) throws IllegalStateException {
        if (playerPackets == null)
            throw new IllegalStateException("This cannot be used in version: " + Bukkit.getVersion());
        return playerPackets.getOverlayPacket(playerId);
    }

    public static PacketContainer getRemovePlayerPacket(final Player player, final UUID uuid, final int entityId) {
        if (playerPackets == null)
            throw new IllegalStateException("This cannot be used in version: " + Bukkit.getVersion());
        return playerPackets.getRemovePacket(player, uuid, entityId);
    }

    public static PacketContainer getSpectatePacket(final int entityId) {
        final PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.CAMERA);
        packetContainer.getIntegers().write(0, entityId);
        return packetContainer;
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
