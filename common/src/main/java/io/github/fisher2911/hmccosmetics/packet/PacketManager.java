package io.github.fisher2911.hmccosmetics.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.packet.versions.PacketBase;
import io.github.fisher2911.hmccosmetics.packet.versions.PacketManager_1_19_2;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PacketManager {
    private static BiMap<List<String>, PacketBase> packetHelper = HashBiMap.create();

    public static final PacketBase WildUpdate = new PacketManager_1_19_2();

    private static PacketBase packetVersion;

    public static void setupPackets() {
        for (PacketBase packet : packetHelper.values()) {
            for (String version : packet.getVersion()) {
                if (Bukkit.getVersion().contains(version)) {
                    packetVersion = packet;
                    HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().info("Found protocol support for your version. Choosing: " + packet.getVersion());
                    break;
                }
            }
        }
        if (packetVersion == null) {
            HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().severe("Unable to find proper support for your version. Defaulting packets to use 1.19.2 information. You can safely ignore this if you do not encounter any problems.");
            packetVersion = WildUpdate;
        }
    }

    public static void addPacketBase(PacketBase packet) {
        if (packetHelper.containsKey(packet.getVersion())) {
            HMCCosmetics.getPlugin(HMCCosmetics.class).getLogger().severe("A protocol version was duplicated! This has overriden the old packet support for that version. You can ignore if this is intentional.");
        }
        packetHelper.put(packet.getVersion(), packet);
    }

    /**
     * Sends meta data for armor stands.
     * @param armorStandId
     * @param sendTo
     */
    public static void sendArmorStandMetaContainer(final int armorStandId, final Player... sendTo) {
        packetVersion.sendArmorStandMetaContainer(armorStandId, sendTo);
    }

    /**
     * Sends cloud meta data about an entity to a player (No idea what this means?)
     * @param entityId
     * @param sendTo
     */
    public static void sendCloudMetaData(int entityId, Player... sendTo) {
        packetVersion.sendCloudMetaData(entityId, sendTo);
    }

    /**
     * Sends a movement packet relative to a position.
     * @param entityId
     * @param sendTo
     */

    public static void sendRelativeMovePacket(final int entityId, Player... sendTo) {
        packetVersion.sendRelativeMovePacket(entityId, sendTo);
    }

    public static void sendHeadLookPacket(int entityId, float yaw, Player... sendTo) {
        packetVersion.sendHeadLookPacket(entityId, yaw, sendTo);
    }

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Collection<? extends Player> sendTo) {
        packetVersion.sendEntitySpawnPacket(location, entityId, entityType, sendTo);
    }

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Player... sendTo) {
        packetVersion.sendEntitySpawnPacket(location, entityId, entityType, sendTo);
    }

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final Collection<? extends Player> sendTo) {
        packetVersion.sendEntitySpawnPacket(location, entityId, entityType, uuid, sendTo);
    }

    /**
     * "Spawns" an entity for players. This entity is fake and is mearly client side.
     * @param location Location to spawn the entity.
     * @param entityId The entityID that is being spawned.
     * @param entityType The entity type that is being spawned.
     * @param uuid The UUID of the entity being spawned.
     * @param sendTo Whom to send the packet to.
     */
    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final Player... sendTo) {
        packetVersion.sendEntitySpawnPacket(location, entityId, entityType, uuid, sendTo);
    }
    public static void sendEntityNotLivingSpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            int data,
            final Player... sendTo) {
        packetVersion.sendEntityNotLivingSpawnPacket(location, entityId, entityType, uuid, data, sendTo);
    }

    public static void sendInvisibilityPacket(final int entityId, final Collection<? extends Player> sendTo) {
        packetVersion.sendInvisibilityPacket(entityId, sendTo);
    }

    /**
     * Will make a entity invisible
     * @param entityId Which entity will this affect?
     * @param sendTo Whom to send the packet to
     */
    public static void sendInvisibilityPacket(final int entityId, final Player... sendTo) {
        packetVersion.sendInvisibilityPacket(entityId, sendTo);
    }

    public static void sendTeleportPacket(
            final int entityId,
            final Location location,
            boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendTeleportPacket(entityId, location, onGround, sendTo);
    }

    /**
     * Used when a player is sent 8+ blocks.
     * @param entityId Entity this affects
     * @param location Location a player is being teleported to
     * @param onGround If the packet is on the ground
     * @param sendTo Whom to send the packet to
     */
    public static void sendTeleportPacket(
            final int entityId,
            final Location location,
            boolean onGround,
            final Player... sendTo
    ) {
        packetVersion.sendTeleportPacket(entityId, location, onGround, sendTo);
    }

    public static void sendMovePacket(
            final int entityId,
            final Location from,
            final Location to,
            final boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendMovePacket(entityId, from, to, onGround, sendTo);
    }

    /**
     * Sends a movement packet from one location to another
     * @param entityId Entity this will affect
     * @param from Previous location
     * @param to New location
     * @param onGround If the movement is on the ground
     * @param sendTo Whom to send the packet to
     */
    public static void sendMovePacket(
            final int entityId,
            final Location from,
            final Location to,
            final boolean onGround,
            final Player... sendTo
    ) {
        packetVersion.sendMovePacket(entityId, from, to, onGround, sendTo);
    }

    public static void sendLeashPacket(
            final int balloonId,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendLeashPacket(balloonId, entityId, sendTo);
    }

    /**
     * Sends a leash packet, useful for balloons!
     * @param leashedEntity Entity being leashed (ex. a horse)
     * @param entityId Entity this is affecting (ex. a player)
     * @param sendTo Whom to send the packet to
     */
    public static void sendLeashPacket(
            final int leashedEntity,
            final int entityId,
            final Player... sendTo
    ) {
        packetVersion.sendLeashPacket(leashedEntity, entityId, sendTo);
    }

    /**
     * Useful for updating packet equipment on a player
     * @param equipment The equipment that is being equiped for the player. THIS IS NOT REAL ARMOR, mearly packets. If a player attempts to remove their cosmetics, it will disappear.
     * @param entityID Entity this will affect
     * @param sendTo Whom to send the packet to
     */
    public static void sendEquipmentPacket(
            final Equipment equipment,
            final int entityID,
            final Player... sendTo) {
        packetVersion.sendEquipmentPacket(equipment, entityID, sendTo);
    }

    /**
     * Sends a rotation packet for an entity
     * @param entityId EntityID that rotates their body
     * @param location Location/Vector that will be looked at
     * @param onGround Whether it is on the ground or not.
     * @param sendTo Whom to send the packet to
     */
    public static void sendRotationPacket(
            final int entityId,
            final Location location,
            final boolean onGround,
            final Player... sendTo
    ) {
        packetVersion.sendRotationPacket(entityId, location, onGround, sendTo);
    }

    public static void sendLookPacket(
            final int entityId,
            final Location location,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendLookPacket(entityId, location, sendTo);
    }

    /**
     * Sends a look packet at a location
     * @param entityId EntityID this packet affects
     * @param location Location/Vector that an entity looks at.
     * @param sendTo Whom to send the packet to
     */
    public static void sendLookPacket(
            final int entityId,
            final Location location,
            final Player... sendTo
    ) {
        packetVersion.sendLookPacket(entityId, location, sendTo);
    }

    /**
     * Mostly to deal with backpacks, this deals with entities riding other entities.
     * @param mountId The entity that is the "mount", ex. a player
     * @param passengerId The entity that is riding the mount, ex. a armorstand for a backpack
     * @param sendTo Whom to send the packet to
     */
    public static void sendRidingPacket(
            final int mountId,
            final int passengerId,
            final Player... sendTo
    ) {
        packetVersion.sendRidingPacket(mountId, passengerId, sendTo);
    }
    /**
     * Destroys an entity from a player
     * @param entityId The entity to delete for a player
     * @param sendTo The players the packet should be sent to
     */
    public static void sendEntityDestroyPacket(final int entityId, final Collection<? extends Player> sendTo) {
        packetVersion.sendEntityDestroyPacket(entityId, sendTo);
    }

    /**
     * Destroys an entity from a player
     * @param entityId The entity to delete for a player
     * @param sendTo The players the packet should be sent to
     */
    public static void sendEntityDestroyPacket(final int entityId, final Player... sendTo) {
        packetVersion.sendEntityDestroyPacket(entityId, sendTo);
    }

    /**
     * Sends a camera packet
     * @param entityId The Entity ID that camera will go towards
     * @param sendTo The players that will be sent this packet
     */
    public static void sendCameraPacket(final int entityId, final Player... sendTo) {
        packetVersion.sendCameraPacket(entityId, sendTo);
    }

    /**
     * Sends a camera packet
     * @param entity The Entity that camera will go towards
     * @param sendTo The players that will be sent this packet
     */
    public static void sendCameraPacket(final Entity entity, final Player... sendTo) {
        packetVersion.sendCameraPacket(entity, sendTo);
    }

    /**
     * Sends a camera packet
     * @param entityId The Entity ID that camera will go towards
     * @param sendTo The players that will be sent this packet
     */
    public static void sendCameraPacket(final int entityId, final Collection<? extends Player> sendTo) {
        packetVersion.sendCameraPacket(entityId, sendTo);
    }

    public static void sendFakePlayerSpawnPacket(
            final Location location,
            final UUID uuid,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendFakePlayerSpawnPacket(location, uuid, entityId, sendTo);
    }

    /**
     *
     * @param location Location of the fake player.
     * @param uuid UUID of the fake player. Should be random.
     * @param entityId The entityID that the entity will take on.
     * @param sendTo Who should it send the packet to?
     */
    public static void sendFakePlayerSpawnPacket(
            final Location location,
            final UUID uuid,
            final int entityId,
            final Player... sendTo
    ) {
        packetVersion.sendFakePlayerSpawnPacket(location, uuid, entityId, sendTo);
    }

    public static void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendFakePlayerInfoPacket(skinnedPlayer, uuid, sendTo);
    }

    /**
     * Creates a fake player entity.
     * @param skinnedPlayer The original player it bases itself off of.
     * @param uuid UUID of the fake entity.
     * @param sendTo Whom to send the packet to
     */
    public static void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final UUID uuid,
            final Player... sendTo
    ) {
        packetVersion.sendFakePlayerInfoPacket(skinnedPlayer, uuid, sendTo);
    }

    public static void sendPlayerOverlayPacket(
            final int playerId,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendPlayerOverlayPacket(playerId, sendTo);
    }

    /**
     * Generates the overlay packet for entities.
     * @param playerId The entity the packet is about
     * @param sendTo Whom is sent the packet.
     */
    public static void sendPlayerOverlayPacket(
            final int playerId,
            final Player... sendTo
    ) {
        packetVersion.sendPlayerOverlayPacket(playerId, sendTo);
    }

    public static void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        packetVersion.sendRemovePlayerPacket(player, uuid, sendTo);
    }

    /**
     * Removes a fake player from being seen by players.
     * @param player Which gameprofile to wrap for removing the player.
     * @param uuid What is the fake player UUID
     * @param sendTo Whom to send the packet to
     */
    public static void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final Player... sendTo
    ) {
        packetVersion.sendRemovePlayerPacket(player, uuid, sendTo);
    }

    /**
     * Sends a gamemode change packet to a player.
     * @param player Player to change their gamemode.
     * @param gamemode Bukkit gamemode to change it to
     */
    public static void sendGameModeChange(
            final Player player,
            final GameMode gamemode
            ) {
        packetVersion.sendGameModeChange(player, gamemode);
    }

    /**
     * Sends a gamemode change packet to a player.
     * @param player Player to change their gamemode.
     * @param gamemode Gamemode value to change it to
     */
    public static void sendGameModeChange(
            final Player player,
            final int gamemode
    ) {
        packetVersion.sendGameModeChange(player, gamemode);
    }

    public static void sendNewTeam(
            final Player skinnedPlayer,
            final Player... sendTo) {
        packetVersion.sendNewTeam(skinnedPlayer, sendTo);
    }

    /**
     * This sends a packet to a player asyncronously, preventing clogging on the main thread.
     * @param player Which player to send the packet to
     * @param packet What packet to send to the player
     */
    public static void sendPacketAsync(final Player player, final PacketContainer packet) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmetics.getPlugin(HMCCosmetics.class),
                () -> ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet));
    }
}
