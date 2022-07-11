package io.github.fisher2911.hmccosmetics.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PacketManager {
//
//    private static final PacketHelper PACKET_HELPER;
//
//    static {
//        final String version = Bukkit.getVersion();
//        if (version.contains("1.16")) {
//            PACKET_HELPER = new PacketHelper_1_16_R3();
//        } else if (version.contains("1.17")) {
//            PACKET_HELPER = new PacketHelper_1_17_R1();
//        } else if (version.contains("1.18")) {
//            PACKET_HELPER = new PacketHelper_1_18_R1();
//        } else {
//            PACKET_HELPER = null;
//        }
//    }

    public static void sendArmorStandMetaContainer(final int armorStandId, final Collection<? extends Player> sendTo) {
        sendArmorStandMetaContainer(armorStandId, sendTo.toArray(new Player[0]));
    }

    public static void sendArmorStandMetaContainer(final int armorStandId, final Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityMetadata(
                    armorStandId,
                    List.of(
                            new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                            new EntityData(15, EntityDataTypes.BYTE, (byte) 0x10)
                    )
            ));
        }
    }

    public static void sendCloudMetaData(int entityId, Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityMetadata(
                    entityId,
                    List.of(
                            new EntityData(0, EntityDataTypes.BYTE, (byte) 0),
                            new EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.empty()),
                            new EntityData(6, EntityDataTypes.ENTITY_POSE, EntityPose.STANDING),
                            new EntityData(4, EntityDataTypes.BOOLEAN, false),
                            new EntityData(8, EntityDataTypes.FLOAT, 0.0f),
                            new EntityData(9, EntityDataTypes.INT, 0),
                            new EntityData(11, EntityDataTypes.PARTICLE, 21),
                            new EntityData(10, EntityDataTypes.BOOLEAN, false),
                            new EntityData(1, EntityDataTypes.INT, 300),
                            new EntityData(3, EntityDataTypes.BOOLEAN, false),
                            new EntityData(7, EntityDataTypes.INT, 0),
                            new EntityData(5, EntityDataTypes.BOOLEAN, false)
                    )
            ));
        }
    }

    public static void sendVelocityPacket(final int entityId, Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityVelocity(
                    entityId,
                    new Vector3d(0.5, 0.5, 0.5)
            ));
        }
    }

    public static void sendRelativeMovePacket(final int entityId, Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityRelativeMove(
                    entityId,
                    0.0,
                    0.0,
                    0.0,
                    false
            ));
        }
    }

    public static void sendHeadLookPacket(int entityId, float yaw, Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityHeadLook(
                    entityId,
                    yaw
            ));
        }
    }

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Collection<? extends Player> sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, sendTo.toArray(new Player[0]));
    }

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Player... sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, UUID.randomUUID(), sendTo);
    }

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final Collection<? extends Player> sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, uuid, sendTo.toArray(new Player[0]));
    }

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerSpawnLivingEntity(
                    entityId,
                    uuid,
                    entityType,
                    new Vector3d(location.getX(), location.getY(), location.getZ()),
                    location.getYaw(),
                    location.getPitch(),
                    0f,
                    Vector3d.zero(),
                    Collections.emptyList()
            ));
        }
    }

    public static void sendEntityNotLivingSpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            int data,
            final Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerSpawnEntity(
                    entityId,
                    Optional.of(uuid),
                    entityType,
                    new Vector3d(location.getX(), location.getY(), location.getZ()),
                    0,
                    0,
                    0f,
                    data,
                    Optional.of(Vector3d.zero())
            ));
        }
    }

    public static void sendInvisibilityPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendInvisibilityPacket(entityId, sendTo.toArray(new Player[0]));
    }

    public static void sendInvisibilityPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityMetadata(
                    entityId,
                    List.of(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20))
            ));
        }
    }

    public static void sendTeleportPacket(
            final int entityId,
            final Location location,
            boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        sendTeleportPacket(entityId, location, onGround, sendTo.toArray(new Player[0]));
    }

    public static void sendTeleportPacket(
            final int entityId,
            final Location location,
            boolean onGround,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityTeleport(
                    entityId,
                    new Vector3d(location.getX(), location.getY(), location.getZ()),
                    location.getYaw(),
                    location.getPitch(),
                    onGround
            ));
        }
    }

    public static void sendMovePacket(
            final int entityId,
            final Location from,
            final Location to,
            final boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        sendMovePacket(entityId, from, to, onGround, sendTo.toArray(new Player[0]));
    }

    public static void sendMovePacket(
            final int entityId,
            final Location from,
            final Location to,
            final boolean onGround,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityRelativeMove(
                    entityId,
                    to.getX() - from.getX(),
                    to.getY() - from.getY(),
                    to.getZ() - from.getZ(),
                    onGround
            ));
        }
    }

    public static void sendLeashPacket(
            final int balloonId,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        sendLeashPacket(balloonId, entityId, sendTo.toArray(new Player[0]));
    }

    public static void sendLeashPacket(
            final int balloonId,
            final int entityId,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerAttachEntity(
                    balloonId,
                    entityId,
                    true
            ));
        }
    }

    public static void sendEquipmentPacket(
            final List<Equipment> equipmentList,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        sendEquipmentPacket(equipmentList, entityId, sendTo.toArray(new Player[0]));
    }

    public static void sendEquipmentPacket(
            final List<Equipment> equipmentList,
            final int entityId,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityEquipment(
                    entityId,
                    equipmentList
            ));
        }
    }

    public static void sendRotationPacket(
            final int entityId,
            final Location location,
            final boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        sendRotationPacket(entityId, location, onGround, sendTo.toArray(new Player[0]));
    }

    public static void sendRotationPacket(
            final int entityId,
            final Location location,
            final boolean onGround,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityRotation(
                    entityId,
                    location.getYaw(),
                    location.getPitch(),
                    onGround
            ));
        }
    }

    public static void sendLookPacket(
            final int entityId,
            final Location location,
            final Collection<? extends Player> sendTo
    ) {
        sendLookPacket(entityId, location, sendTo.toArray(new Player[0]));
    }

    public static void sendLookPacket(
            final int entityId,
            final Location location,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityHeadLook(
                    entityId,
                    location.getYaw()
            ));
        }
    }

    public static void sendRidingPacket(
            final int mountId,
            final int passengerId,
            final Collection<? extends Player> sendTo
    ) {
        sendRidingPacket(mountId, passengerId, sendTo.toArray(new Player[0]));
    }

    public static void sendRidingPacket(
            final int mountId,
            final int passengerId,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerSetPassengers(
                    mountId,
                    new int[]{passengerId}
            ));
        }
    }

    public static void sendEntityDestroyPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendEntityDestroyPacket(entityId, sendTo.toArray(new Player[0]));
    }

    public static void sendEntityDestroyPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerDestroyEntities(entityId));
        }
    }

    public static void sendCameraPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerCamera(entityId));
        }
    }

    public static void sendCameraPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendCameraPacket(entityId, sendTo.toArray(new Player[0]));
    }

//    public static void sendSoundPacket(
//            final Player player,
//            final Location location,
//            final MinecraftKey name,
//            final float volume,
//            final float pitch,
//            final EnumWrappers.SoundCategory soundCategory,
//            final Player...sendTo
//    ) {
//        final var packet = new WrapperPlayServerSoundEffect(
//
//        );
//        final var manager = ProtocolLibrary.getProtocolManager();
//        final var packet = manager.createPacket(PacketType.Play.Server.CUSTOM_SOUND_EFFECT);
//
//        packet.getMinecraftKeys()
//                .write(
//                        0,
//                        name
//                );
//
//        packet.getSoundCategories()
//                .write(0, EnumWrappers.SoundCategory.valueOf(soundCategory.name()));
//
//        packet.getIntegers()
//                .write(0, location.getBlockX() * 8)
//                .write(
//                        1, location.getBlockY() * 8
//                )
//                .write(2, location.getBlockZ() * 8);
//
//        packet.getFloat()
//                .write(0, volume)
//                .write(1, pitch);
//
//        return packet;
//    }

    public static void sendFakePlayerSpawnPacket(
            final Location location,
            final UUID uuid,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        sendFakePlayerSpawnPacket(location, uuid, entityId, sendTo.toArray(new Player[0]));
    }

    public static void sendFakePlayerSpawnPacket(
            final Location location,
            final UUID uuid,
            final int entityId,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerSpawnPlayer(
                    entityId,
                    uuid,
                    new com.github.retrooper.packetevents.protocol.world.Location(
                            location.getX(),
                            location.getY(),
                            location.getZ(),
                            location.getYaw(),
                            location.getPitch()
                    )
            ));
        }
    }

    public static void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        sendFakePlayerInfoPacket(skinnedPlayer, uuid, sendTo.toArray(new Player[0]));
    }

    public static void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final UUID uuid,
            final Player... sendTo
    ) {
        final List<TextureProperty> textures = PacketEvents.getAPI().getPlayerManager().getUser(skinnedPlayer).getProfile().getTextureProperties();
        final WrapperPlayServerPlayerInfo.PlayerData data = new WrapperPlayServerPlayerInfo.PlayerData(
                Component.text(""),
                new UserProfile(
                        uuid,
                        "",
                        textures
                ),
                GameMode.CREATIVE,
                0
        );
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerPlayerInfo(
                    WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
                    data
            ));
        }
    }

    public static void sendPlayerOverlayPacket(
            final int playerId,
            final Collection<? extends Player> sendTo
    ) {
        sendPlayerOverlayPacket(playerId, sendTo.toArray(new Player[0]));
    }

    public static void sendPlayerOverlayPacket(
            final int playerId,
            final Player... sendTo
    ) {
        final byte mask = 0x01 | 0x02 | 0x04 | 0x08 | 0x010 | 0x020 | 0x40;
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerEntityMetadata(
                    playerId,
                    List.of(
                            new EntityData(17, EntityDataTypes.BYTE, mask),
                            new EntityData(15, EntityDataTypes.BYTE, (byte) 0x10)
                    )
            ));
        }
    }

    public static void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        sendRemovePlayerPacket(player, uuid, sendTo.toArray(new Player[0]));
    }

    public static void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            sendPacketAsync(p, new WrapperPlayServerPlayerInfo(
                    WrapperPlayServerPlayerInfo.Action.REMOVE_PLAYER,
                    new WrapperPlayServerPlayerInfo.PlayerData(
                            Component.empty(),
                            new UserProfile(
                                    uuid,
                                    player.getDisplayName()
                            ),
                            com.github.retrooper.packetevents.protocol.player.GameMode.SURVIVAL,
                            0
                    )
            ));
        }
    }

    public static void sendPacketAsync(final Player player, final PacketWrapper<?> packet) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmetics.getPlugin(HMCCosmetics.class), () ->
                PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet)
        );
    }

    public static void sendPacketAsyncSilently(final Player player, final PacketWrapper<?> packet) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmetics.getPlugin(HMCCosmetics.class), () ->
                PacketEvents.getAPI().getPlayerManager().sendPacketSilently(player, packet)
        );
    }


    public static com.github.retrooper.packetevents.protocol.player.Equipment getEquipment(
            final ItemStack itemStack,
            final org.bukkit.inventory.EquipmentSlot slot
    ) {
        return getEquipment(SpigotConversionUtil.fromBukkitItemStack(itemStack), slot);
    }

    public static com.github.retrooper.packetevents.protocol.player.Equipment getEquipment(
            final com.github.retrooper.packetevents.protocol.item.ItemStack itemStack,
            final org.bukkit.inventory.EquipmentSlot slot
    ) {
        return new com.github.retrooper.packetevents.protocol.player.Equipment(
                PacketManager.fromBukkitSlot(slot),
                itemStack
        );
    }

    public static EquipmentSlot fromBukkitSlot(final org.bukkit.inventory.EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EquipmentSlot.HELMET;
            case CHEST -> EquipmentSlot.CHEST_PLATE;
            case LEGS -> EquipmentSlot.LEGGINGS;
            case FEET -> EquipmentSlot.BOOTS;
            case HAND -> EquipmentSlot.MAIN_HAND;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;
        };
    }

}
