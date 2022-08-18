package io.github.fisher2911.hmccosmetics.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerNamedEntitySpawn;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerPlayerInfo;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerRelEntityMove;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerRelEntityMoveLook;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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

    /**
     * Sends meta data for armor stands.
     * @param armorStandId
     * @param sendTo
     */
    public static void sendArmorStandMetaContainer(final int armorStandId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, armorStandId);
            WrappedDataWatcher metadata = new WrappedDataWatcher();
            //final WrappedDataWatcher.Serializer serializer = WrappedDataWatcher.Registry.get(Byte.class);
            if (metadata == null) return;
            // 0x10 & 0x20
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
            metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x10);
            packet.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects());

            sendPacketAsync(p, packet);
        }
    }

    /**
     * Sends cloud meta data about an entity to a player (No idea what this means?)
     * @param entityId
     * @param sendTo
     */
    public static void sendCloudMetaData(int entityId, Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, entityId);
            WrappedDataWatcher wrapper = new WrappedDataWatcher();
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(Optional.class)), Optional.empty());
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(6, WrappedDataWatcher.Registry.get(EnumWrappers.EntityPose.class)), EnumWrappers.EntityPose.STANDING);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(4, WrappedDataWatcher.Registry.get(Boolean.class)), false);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(8, WrappedDataWatcher.Registry.get(Float.class)), 0.0f);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(9, WrappedDataWatcher.Registry.get(int.class)), 0);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(11, WrappedDataWatcher.Registry.get(EnumWrappers.Particle.class)), 21);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(10, WrappedDataWatcher.Registry.get(Boolean.class)), false);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), false);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(4, WrappedDataWatcher.Registry.get(Boolean.class)), false);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), false);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(1, WrappedDataWatcher.Registry.get(int.class)), 300);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(7, WrappedDataWatcher.Registry.get(int.class)), 0);
            packet.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects());
            sendPacketAsync(p, packet);
        }
    }

    /**
     * Sends a movement packet relative to a position.
     * @param entityId
     * @param sendTo
     */

    public static void sendRelativeMovePacket(final int entityId, Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
            WrapperPlayServerRelEntityMove wrapper = new WrapperPlayServerRelEntityMove(packet);
            wrapper.setDx(0.0);
            wrapper.setDy(0.0);
            wrapper.setDz(0.0);
            wrapper.setOnGround(false);

            sendPacketAsync(p, wrapper.getHandle());
        }
    }

    public static void sendHeadLookPacket(int entityId, float yaw, Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
            WrapperPlayServerRelEntityMoveLook wrapper = new WrapperPlayServerRelEntityMoveLook(packet);
            wrapper.setYaw(yaw);
            wrapper.setEntityID(entityId);
            sendPacketAsync(p, wrapper.getHandle());
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
            packet.getModifier().writeDefaults();
            packet.getUUIDs().write(0, uuid);
            packet.getIntegers().write(0, entityId);
            packet.getEntityTypeModifier().write(0, entityType);
            packet.getDoubles().
                    write(0, location.getX()).
                    write(1, location.getY()).
                    write(2, location.getZ());
            //packet.getIntegers().write(1, 1);
            //p.sendMessage("Packet sent");
            //packet.getIntegers().write(2, 0);
            //packet.getIntegers().write(3, 0);
            //packet.getIntegers().write(4, 0);
            //packet.getIntegers().write(5, 0);
            //packet.getIntegers().write(4, (int)(((location.getYaw() * 256.0F) / 360.0F)));
            //packet.getIntegers().write(5, (int)(((location.getPitch() * 256.0F) / 360.0F)));
            sendPacketAsync(p, packet);
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

            /*
            sendPacketAsync(p, new WrapperPlayServerSpawnEntity(
                    entityId,
                    Optional.of(uuid),
                    entityType,
                    new Vector3d(location.getX(), location.getY(), location.getZ()),
                    0, // pitch
                    0, // yaw
                    0f, // headyaw
                    data,
                    Optional.of(Vector3d.zero())
            ));
             */
        }
    }

    public static void sendInvisibilityPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendInvisibilityPacket(entityId, sendTo.toArray(new Player[0]));
    }

    public static void sendInvisibilityPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {

            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, entityId);
            WrappedDataWatcher wrapper = new WrappedDataWatcher();
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
            packet.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects());
            //packet.setMeta(String.valueOf(entityId), List.of(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20)));
            /*
            sendPacketAsync(p, new WrapperPlayServerEntityMetadata(
                    entityId,
                    List.of(new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20))
            ));
             */
            sendPacketAsync(p, packet);
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
            packet.getIntegers().write(0, entityId);
            packet.getDoubles().write(0, location.getX());
            packet.getDoubles().write(1, location.getY());
            packet.getDoubles().write(2, location.getZ());
            packet.getBytes().write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
            packet.getBytes().write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
            packet.getBooleans().write(0, onGround);
            sendPacketAsync(p, packet);
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
            WrapperPlayServerRelEntityMove wrapper = new WrapperPlayServerRelEntityMove(packet);
            wrapper.setEntityID(entityId);
            wrapper.setDx(to.getX() - from.getX());
            wrapper.setDy(to.getY() - from.getY());
            wrapper.setDz(to.getZ() - from.getZ());
            wrapper.setOnGround(onGround);
            sendPacketAsync(p, wrapper.getHandle());
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
            packet.getIntegers().write(0, balloonId);
            packet.getIntegers().write(1, entityId);
            // Leash?
            packet.getBooleans().write(0, true);
            /*
            sendPacketAsync(p, new WrapperPlayServerAttachEntity(
                    balloonId,
                    entityId,
                    true
            ));
             */
            sendPacketAsync(p, packet);
        }
    }

    public static void sendEquipmentPacket(
            final io.github.fisher2911.hmccosmetics.user.Equipment equipment,
            final int entityID,
            final Player... sendTo) {
        // DONE: Fix Packet causing player disconnect! https://i.imgur.com/l4D6lmM.png
        //List<PacketContainer> packets = new ArrayList<>();
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, entityID);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
        for (EquipmentSlot slot : equipment.keys()) {
            if (itemBukkitSlot(slot) != null) list.add(new Pair<>(itemBukkitSlot(slot), equipment.getItem(slot)));
        }
        if (list == null) return;
        packet.getSlotStackPairLists().write(0, list);
        for (Player p : sendTo) {
            //p.sendMessage("Offhand Cosmetic: " + equipment.getItem(EquipmentSlot.OFF_HAND));
            sendPacketAsync(p, packet);
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
            packet.getIntegers().write(0, entityId);
            packet.getBytes().write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
            packet.getBytes().write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
            packet.getBooleans().write(0, onGround);
            sendPacketAsync(p, packet);
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            packet.getIntegers().write(0, entityId);
            packet.getBytes().write(0, (byte) location.getYaw());
            sendPacketAsync(p, packet);
        }
    }

    public static void sendRidingPacket(
            final int mountId,
            final int passengerId,
            final Player... sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, mountId);
        packet.getIntegerArrays().write(0, new int[]{passengerId});
        for (final Player p : sendTo) {
            p.sendMessage("MountID: " + mountId + " Passenger ID: " + new int[]{passengerId} + " / Raw Passenger: " + passengerId);
            sendPacketAsync(p, packet);
        }
    }

    public static void sendEntityDestroyPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendEntityDestroyPacket(entityId, sendTo.toArray(new Player[0]));
    }

    public static void sendEntityDestroyPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getModifier().write(0, new IntArrayList(new int[]{entityId}));
            sendPacketAsync(p, packet);
        }
    }

    public static void sendCameraPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CAMERA);
            packet.getIntegers().write(0, entityId);
            sendPacketAsync(p, packet);
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
            // Needs testing!!!
            //PacketContainer packet = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
            WrapperPlayServerNamedEntitySpawn wrapper = new WrapperPlayServerNamedEntitySpawn();
            wrapper.setEntityID(entityId);
            wrapper.setPlayerUUID(uuid);
            wrapper.setPosition(location.toVector());
            wrapper.setPitch(location.getPitch());
            wrapper.setYaw(location.getYaw());
            /*
            packet.getModifier().writeDefaults();
            //packet.getEntityTypeModifier().write(0, EntityType.PLAYER);
            packet.getIntegers().write(0, entityId);
            packet.getUUIDs().write(0, uuid);
            packet.getDoubles().write(0, location.getX())
                    .write(1, location.getY())
                    .write(2, location.getZ());

            /*
            WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity();
            wrapper.setUniqueId(uuid);
            wrapper.setEntityID(entityId);
            wrapper.setType(EntityType.PLAYER);
            wrapper.setY(location.getY());
            wrapper.setX(location.getX());
            wrapper.setZ(location.getZ());
             */
            //wrapper.setPitch((location.getPitch() * 360.F) / 256.0F);
            //wrapper.setYaw((location.getYaw() * 360.F) / 256.0F);
            /*
            packet.getIntegers().write(0, entityId);
            packet.getUUIDs().write(0, uuid);
            packet.getDoubles().write(0, location.getX())
                               .write(1, location.getY())
                               .write(2, location.getZ());
            packet.getIntegers().write(3, (int) ((location.getPitch() * 360.F) / 256.0F)).
                    write(4, (int) ((location.getYaw() * 360.F) / 256.0F));
             */
            sendPacketAsync(p, wrapper.getHandle());
            /*
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
             */
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
        //PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
        info.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        info.setData(List.of(new PlayerInfoData(new WrappedGameProfile(uuid, skinnedPlayer.getName()), 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(skinnedPlayer.getName() + "-NPC"))));
        /*
        packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        PlayerInfoData data = new PlayerInfoData(WrappedGameProfile.fromPlayer(skinnedPlayer), -1, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(""));
        packet.getPlayerInfoDataLists().write(0, List.of(
                data
        ));

         */
        /*
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
         */
        for (final Player p : sendTo) {
            sendPacketAsync(p, info.getHandle());
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

            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, playerId);
            WrappedDataWatcher wrapper = new WrappedDataWatcher();
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(17, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x10);
            packet.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects());
            sendPacketAsync(p, packet);
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
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
            packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
            //packet.getUUIDs().write(0, uuid);
            packet.getPlayerInfoDataLists().write(0, List.of(
                    new PlayerInfoData(new WrappedGameProfile(uuid, uuid.toString()), 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(player.getName()))
            ));
            sendPacketAsync(p, packet);
            /*
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
             */
        }
    }

    public static void sendPacketAsync(final Player player, final PacketContainer packet) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmetics.getPlugin(HMCCosmetics.class), () -> {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        });
    }

    public static io.github.fisher2911.hmccosmetics.user.Equipment getEquipment(
            final ItemStack itemStack,
            final org.bukkit.inventory.EquipmentSlot slot
    ) {
        Equipment equip = new Equipment();
        equip.setItem(slot, itemStack);
        return equip;
    }
    /*
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
     */


    public static EnumWrappers.ItemSlot itemBukkitSlot(final org.bukkit.inventory.EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> EnumWrappers.ItemSlot.HEAD;
            case CHEST -> EnumWrappers.ItemSlot.CHEST;
            case LEGS -> EnumWrappers.ItemSlot.LEGS;
            case FEET -> EnumWrappers.ItemSlot.FEET;
            case HAND -> EnumWrappers.ItemSlot.MAINHAND;
            case OFF_HAND -> EnumWrappers.ItemSlot.OFFHAND;
            default -> null;
        };
    }
}
