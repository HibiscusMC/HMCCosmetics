package io.github.fisher2911.hmccosmetics.packet.versions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import io.github.fisher2911.hmccosmetics.packet.wrappers.*;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import io.github.fisher2911.hmccosmetics.util.PlayerUtils;
import net.minecraft.server.v1_16_R3.EnumItemSlot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PacketManager_1_19_2 extends PacketBase {


    public PacketManager_1_19_2() {
        super(List.of("1.19", "1.19.1", "1.19.2"));
    }

    /**
     * Sends meta data for armor stands.
     * @param armorStandId
     * @param sendTo
     */
    @Override
    public void sendArmorStandMetaContainer(final int armorStandId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, armorStandId);
            WrappedDataWatcher metadata = new WrappedDataWatcher();
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
    @Override
    public void sendCloudMetaData(int entityId, Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, entityId);
            WrappedDataWatcher wrapper = new WrappedDataWatcher();
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
            //wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.get(Optional.class)), Optional.empty());
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
    @Override
    public void sendRelativeMovePacket(final int entityId, Player... sendTo) {
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
    @Override
    public void sendHeadLookPacket(int entityId, float yaw, Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
            WrapperPlayServerRelEntityMoveLook wrapper = new WrapperPlayServerRelEntityMoveLook(packet);
            wrapper.setYaw(yaw);
            wrapper.setEntityID(entityId);
            sendPacketAsync(p, wrapper.getHandle());
        }
    }
    @Override
    public void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Collection<? extends Player> sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, sendTo.toArray(new Player[0]));
    }
    @Override
    public void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Player... sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, UUID.randomUUID(), sendTo);
    }
    @Override
    public void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final Collection<? extends Player> sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, uuid, sendTo.toArray(new Player[0]));
    }

    /**
     * "Spawns" an entity for players. This entity is fake and is mearly client side.
     * @param location Location to spawn the entity.
     * @param entityId The entityID that is being spawned.
     * @param entityType The entity type that is being spawned.
     * @param uuid The UUID of the entity being spawned.
     * @param sendTo Whom to send the packet to.
     */
    @Override
    public void sendEntitySpawnPacket(
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
            sendPacketAsync(p, packet);
        }
    }
    @Deprecated @Override
    public void sendEntityNotLivingSpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            int data,
            final Player... sendTo) {
        for (final Player p : sendTo) {

        }
    }
    @Override
    public void sendInvisibilityPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendInvisibilityPacket(entityId, sendTo.toArray(new Player[0]));
    }

    /**
     * Will make a entity invisible
     * @param entityId Which entity will this affect?
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendInvisibilityPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, entityId);
            WrappedDataWatcher wrapper = new WrappedDataWatcher();
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
            packet.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects());
            sendPacketAsync(p, packet);
        }
    }
    @Override
    public void sendTeleportPacket(
            final int entityId,
            final Location location,
            boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        sendTeleportPacket(entityId, location, onGround, sendTo.toArray(new Player[0]));
    }

    /**
     * Used when a player is sent 8+ blocks.
     * @param entityId Entity this affects
     * @param location Location a player is being teleported to
     * @param onGround If the packet is on the ground
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendTeleportPacket(
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
    @Override
    public void sendMovePacket(
            final int entityId,
            final Location from,
            final Location to,
            final boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        sendMovePacket(entityId, from, to, onGround, sendTo.toArray(new Player[0]));
    }

    /**
     * Sends a movement packet from one location to another
     * @param entityId Entity this will affect
     * @param from Previous location
     * @param to New location
     * @param onGround If the movement is on the ground
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendMovePacket(
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
    @Override
    public void sendLeashPacket(
            final int balloonId,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        sendLeashPacket(balloonId, entityId, sendTo.toArray(new Player[0]));
    }

    /**
     * Sends a leash packet, useful for balloons!
     * @param leashedEntity Entity being leashed (ex. a horse)
     * @param entityId Entity this is affecting (ex. a player)
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendLeashPacket(
            final int leashedEntity,
            final int entityId,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
            packet.getIntegers().write(0, leashedEntity);
            packet.getIntegers().write(1, entityId);
            // Leash?
            //packet.getBooleans().write(0, true);
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

    /**
     * Useful for updating packet equipment on a player
     * @param equipment The equipment that is being equiped for the player. THIS IS NOT REAL ARMOR, mearly packets. If a player attempts to remove their cosmetics, it will disappear.
     * @param entityID Entity this will affect
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendEquipmentPacket(
            final Equipment equipment,
            final int entityID,
            final Player... sendTo) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, entityID);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = new ArrayList<>();
        for (EquipmentSlot slot : equipment.keys()) {
            if (PlayerUtils.itemBukkitSlot(slot) != null) list.add(new Pair<>(PlayerUtils.itemBukkitSlot(slot), equipment.getItem(slot)));
        }
        if (list.isEmpty() || list == null) return;
        packet.getSlotStackPairLists().write(0, list);
        for (Player p : sendTo) {
            sendPacketAsync(p, packet);
        }
    }

    /**
     * Sends a rotation packet for an entity
     * @param entityId EntityID that rotates their body
     * @param location Location/Vector that will be looked at
     * @param onGround Whether it is on the ground or not.
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendRotationPacket(
            final int entityId,
            final Location location,
            final boolean onGround,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
            packet.getIntegers().write(0, entityId);
            float ROTATION_FACTOR = 256.0F / 360.0F;
            packet.getBytes().write(0, (byte) (location.getYaw() * ROTATION_FACTOR));
            packet.getBytes().write(1, (byte) (location.getPitch() * ROTATION_FACTOR));

            //Bukkit.getLogger().info("DEBUG: Yaw: " + (location.getYaw() * ROTATION_FACTOR) + " | Original Yaw: " + location.getYaw());
            packet.getBooleans().write(0, onGround);
            sendPacketAsync(p, packet);
        }
    }
    @Override
    public void sendLookPacket(
            final int entityId,
            final Location location,
            final Collection<? extends Player> sendTo
    ) {
        sendLookPacket(entityId, location, sendTo.toArray(new Player[0]));
    }

    /**
     * Sends a look packet at a location
     * @param entityId EntityID this packet affects
     * @param location Location/Vector that an entity looks at.
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendLookPacket(
            final int entityId,
            final Location location,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
            packet.getIntegers().write(0, entityId);
            packet.getBytes().write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
            sendPacketAsync(p, packet);
        }
    }

    /**
     * Mostly to deal with backpacks, this deals with entities riding other entities.
     * @param mountId The entity that is the "mount", ex. a player
     * @param passengerId The entity that is riding the mount, ex. a armorstand for a backpack
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendRidingPacket(
            final int mountId,
            final int passengerId,
            final Player... sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, mountId);
        packet.getIntegerArrays().write(0, new int[]{passengerId});
        for (final Player p : sendTo) {
            //p.sendMessage("MountID: " + mountId + " / Raw Passenger: " + passengerId + " | Next Entity: " + Database.getNextEntityId());
            sendPacketAsync(p, packet);
        }
    }
    /**
     * Destroys an entity from a player
     * @param entityId The entity to delete for a player
     * @param sendTo The players the packet should be sent to
     */
    @Override
    public void sendEntityDestroyPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendEntityDestroyPacket(entityId, sendTo.toArray(new Player[0]));
    }

    /**
     * Destroys an entity from a player
     * @param entityId The entity to delete for a player
     * @param sendTo The players the packet should be sent to
     */
    @Override
    public void sendEntityDestroyPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getModifier().write(0, new IntArrayList(new int[]{entityId}));
            sendPacketAsync(p, packet);
        }
    }

    /**
     * Sends a camera packet
     * @param entityId The Entity ID that camera will go towards
     * @param sendTo The players that will be sent this packet
     */
    @Override
    public void sendCameraPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CAMERA);
            packet.getIntegers().write(0, entityId);
            sendPacketAsync(p, packet);
        }
    }

    /**
     * Sends a camera packet
     * @param entity The Entity that camera will go towards
     * @param sendTo The players that will be sent this packet
     */
    @Override
    public void sendCameraPacket(final Entity entity, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CAMERA);
            packet.getEntityModifier(p.getWorld()).write(0, entity);
            sendPacketAsync(p, packet);
        }
    }

    /**
     * Sends a camera packet
     * @param entityId The Entity ID that camera will go towards
     * @param sendTo The players that will be sent this packet
     */
    @Override
    public void sendCameraPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendCameraPacket(entityId, sendTo.toArray(new Player[0]));
    }
@Override
    public void sendFakePlayerSpawnPacket(
            final Location location,
            final UUID uuid,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        sendFakePlayerSpawnPacket(location, uuid, entityId, sendTo.toArray(new Player[0]));
    }

    /**
     *
     * @param location Location of the fake player.
     * @param uuid UUID of the fake player. Should be random.
     * @param entityId The entityID that the entity will take on.
     * @param sendTo Who should it send the packet to?
     */
    @Override
    public void sendFakePlayerSpawnPacket(
            final Location location,
            final UUID uuid,
            final int entityId,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            WrapperPlayServerNamedEntitySpawn wrapper = new WrapperPlayServerNamedEntitySpawn();
            wrapper.setEntityID(entityId);
            wrapper.setPlayerUUID(uuid);
            wrapper.setPosition(location.toVector());
            wrapper.setPitch(location.getPitch());
            wrapper.setYaw(location.getYaw());
            sendPacketAsync(p, wrapper.getHandle());
        }
    }
    @Override
    public void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        sendFakePlayerInfoPacket(skinnedPlayer, uuid, sendTo.toArray(new Player[0]));
    }

    /**
     * Creates a fake player entity.
     * @param skinnedPlayer The original player it bases itself off of.
     * @param uuid UUID of the fake entity.
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final UUID uuid,
            final Player... sendTo
    ) {
        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
        info.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        String name = "Mannequin-" + skinnedPlayer.getEntityId();
        while (name.length() > 16) {
            name = name.substring(16);
        }

        WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uuid, name);
        wrappedGameProfile.getProperties().put("textures", PlayerUtils.getSkin(skinnedPlayer));
        info.setData(List.of(new PlayerInfoData(wrappedGameProfile, 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(name))));
        for (final Player p : sendTo) {
            sendPacketAsync(p, info.getHandle());
        }
    }
    @Override
    public void sendPlayerOverlayPacket(
            final int playerId,
            final Collection<? extends Player> sendTo
    ) {
        sendPlayerOverlayPacket(playerId, sendTo.toArray(new Player[0]));
    }

    /**
     * Generates the overlay packet for entities.
     * @param playerId The entity the packet is about
     * @param sendTo Whom is sent the packet.
     */
    @Override
    public void sendPlayerOverlayPacket(
            final int playerId,
            final Player... sendTo
    ) {
        /*
        0x01 = Is on fire
        0x02 = Is courching
        0x04 = Unusued
        0x08 = Sprinting
        0x10 = Is swimming
        0x20 = Invisibile
        0x40 = Is Glowing
        0x80 = Is flying with an elytra
         https://wiki.vg/Entity_metadata#Entity
         */
        final byte mask = 0x01 | 0x02 | 0x04 | 0x08 | 0x010 | 0x020 | 0x40;
        for (final Player p : sendTo) {

            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, playerId);
            WrappedDataWatcher wrapper = new WrappedDataWatcher();
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(17, WrappedDataWatcher.Registry.get(Byte.class)), mask);
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x10);
            packet.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects());
            sendPacketAsync(p, packet);
        }
    }
    @Override
    public void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        sendRemovePlayerPacket(player, uuid, sendTo.toArray(new Player[0]));
    }

    /**
     * Removes a fake player from being seen by players.
     * @param player Which gameprofile to wrap for removing the player.
     * @param uuid What is the fake player UUID
     * @param sendTo Whom to send the packet to
     */
    @Override
    public void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final Player... sendTo
    ) {
        for (final Player p : sendTo) {
            WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
            info.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

            String name = "Mannequin-" + player.getEntityId();
            while (name.length() > 16) {
                name = name.substring(16);
            }

            info.setData(List.of(new PlayerInfoData(new WrappedGameProfile(uuid, player.getName()), 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(name))));
            sendPacketAsync(p, info.getHandle());
        }
    }

    /**
     * Sends a gamemode change packet to a player.
     * @param player Player to change their gamemode.
     * @param gamemode Bukkit gamemode to change it to
     */
    @Override
    public void sendGameModeChange(
            final Player player,
            final GameMode gamemode
    ) {
        sendGameModeChange(player, PlayerUtils.convertGamemode(gamemode));
    }

    /**
     * Sends a gamemode change packet to a player.
     * @param player Player to change their gamemode.
     * @param gamemode Gamemode value to change it to
     */
    @Override
    public void sendGameModeChange(
            final Player player,
            final int gamemode
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        packet.getGameStateIDs().write(0, 3);
        // Tells what event this is. This is a change gamemode event.
        packet.getFloat().write(0, (float) gamemode);
        sendPacketAsync(player, packet);
    }

    @Override
    public void sendNewTeam(
            final Player skinnedPlayer,
            final Player... sendTo)
    {
        // This needs to be worked on. If you know how to do it, PR: https://wiki.vg/Protocol#Update_Teams
    }
}
