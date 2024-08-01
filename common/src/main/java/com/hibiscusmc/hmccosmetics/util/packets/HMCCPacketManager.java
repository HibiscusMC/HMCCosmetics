package com.hibiscusmc.hmccosmetics.util.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.HMCCPlayerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.wrappers.WrapperPlayServerNamedEntitySpawn;
import com.hibiscusmc.hmccosmetics.util.packets.wrappers.WrapperPlayServerPlayerInfo;
import com.hibiscusmc.hmccosmetics.util.packets.wrappers.WrapperPlayServerRelEntityMove;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class HMCCPacketManager extends PacketManager {

    public static void sendEntitySpawnPacket(
            final @NotNull Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid
            ) {
        sendEntitySpawnPacket(location, entityId, entityType, uuid, getViewers(location));
    }

    public static void sendEntitySpawnPacket(
            final @NotNull Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final @NotNull List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        packet.getModifier().writeDefaults();
        packet.getUUIDs().write(0, uuid);
        packet.getIntegers().write(0, entityId);
        packet.getEntityTypeModifier().write(0, entityType);
        packet.getDoubles().
                write(0, location.getX()).
                write(1, location.getY()).
                write(2, location.getZ());
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void equipmentSlotUpdate(
            Player player,
            boolean empty,
            List<Player> sendTo
    ) {
        HashMap<EquipmentSlot, ItemStack> items = new HashMap<>();
        for (EquipmentSlot slot : HMCCInventoryUtils.getPlayerArmorSlots()) {
            ItemStack item = player.getInventory().getItem(slot);
            if (empty) item = new ItemStack(Material.AIR);
            items.put(slot, item);
        }
        equipmentSlotUpdate(player.getEntityId(), items, sendTo);
    }
    public static void equipmentSlotUpdate(
            @NotNull Player player,
            CosmeticSlot cosmetic,
            List<Player> sendTo
    ) {
        CosmeticUser user = CosmeticUsers.getUser(player.getUniqueId());
        equipmentSlotUpdate(player.getEntityId(), user, cosmetic, sendTo);
    }
    public static void equipmentSlotUpdate(
            CosmeticUser user,
            CosmeticSlot cosmeticSlot,
            List<Player> sendTo
    ) {
        equipmentSlotUpdate(user.getEntity().getEntityId(), user, cosmeticSlot, sendTo);
    }

    public static void equipmentSlotUpdate(
            int entityId,
            CosmeticUser user,
            CosmeticSlot cosmeticSlot,
            List<Player> sendTo
    ) {
        if (cosmeticSlot == CosmeticSlot.BACKPACK || cosmeticSlot == CosmeticSlot.CUSTOM || cosmeticSlot == CosmeticSlot.BALLOON || cosmeticSlot == CosmeticSlot.EMOTE) return;

        equipmentSlotUpdate(entityId, HMCCInventoryUtils.getEquipmentSlot(cosmeticSlot), user.getUserCosmeticItem(cosmeticSlot), sendTo);
    }

    public static void sendArmorstandMetadata(
            int entityId,
            List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, entityId);
        final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();

        // 0x21 = Invisible + Fire (Aka, burns to make it not take the light of the block its in, avoiding turning it black)
        wrappedDataValueList.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x21));
        wrappedDataValueList.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x10));
        packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendInvisibilityPacket(
            int entityId,
            List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, entityId);

        final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        wrappedDataValueList.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20));
        packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendCloudEffect(
            int entityId,
            List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, entityId);

        final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        wrappedDataValueList.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20));
        wrappedDataValueList.add(new WrappedDataValue(8, WrappedDataWatcher.Registry.get(Float.class), 0f));
        //wrappedDataValueList.add(new WrappedDataValue(11, WrappedDataWatcher.Registry.get(Integer.class), 21));
        packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);

        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendRotationPacket(
            int entityId,
            Location location,
            boolean onGround
    ) {
        sendRotationPacket(entityId, location, onGround, getViewers(location));
    }

    public static void sendRotationPacket(
            int entityId,
            @NotNull Location location,
            boolean onGround,
            @NotNull List<Player> sendTo
    ) {
        float ROTATION_FACTOR = 256.0F / 360.0F;
        float yaw = location.getYaw() * ROTATION_FACTOR;
        float pitch = location.getPitch() * ROTATION_FACTOR;
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        packet.getIntegers().write(0, entityId);
        packet.getBytes().write(0, (byte) yaw);
        packet.getBytes().write(1, (byte) pitch);

        //Bukkit.getLogger().info("DEBUG: Yaw: " + (location.getYaw() * ROTATION_FACTOR) + " | Original Yaw: " + location.getYaw());
        packet.getBooleans().write(0, onGround);
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendRotationPacket(
            int entityId,
            int yaw,
            boolean onGround,
            @NotNull List<Player> sendTo
    ) {
        float ROTATION_FACTOR = 256.0F / 360.0F;
        float yaw2 = yaw * ROTATION_FACTOR;
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        packet.getIntegers().write(0, entityId);
        packet.getBytes().write(0, (byte) yaw2);
        packet.getBytes().write(1, (byte) 0);

        //Bukkit.getLogger().info("DEBUG: Yaw: " + (location.getYaw() * ROTATION_FACTOR) + " | Original Yaw: " + location.getYaw());
        packet.getBooleans().write(0, onGround);
        for (Player p : sendTo) sendPacket(p, packet);
    }


    /**
     * Mostly to deal with backpacks, this deals with entities riding other entities.
     * @param mountId The entity that is the "mount", ex. a player
     * @param passengerId The entity that is riding the mount, ex. a armorstand for a backpack
     */
    public static void sendRidingPacket(
            final int mountId,
            final int passengerId,
            final Location location
    ) {
        sendRidingPacket(mountId, passengerId, getViewers(location));
    }

    /**
     * Mostly to deal with backpacks, this deals with entities riding other entities.
     * @param mountId The entity that is the "mount", ex. a player
     * @param passengerIds The entities that are riding the mount, ex. a armorstand for a backpack
     * @param sendTo Whom to send the packet to
     */
    public static void sendRidingPacket(
            final int mountId,
            final int[] passengerIds,
            final @NotNull List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, mountId);
        packet.getIntegerArrays().write(0, passengerIds);
        for (final Player p : sendTo) {
            sendPacket(p, packet);
        }
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
            final @NotNull List<Player> sendTo
    ) {
        sendRidingPacket(mountId, new int[] {passengerId}, sendTo);
    }

    /**
     *
     * @param location Location of the fake player.
     * @param uuid UUID of the fake player. Should be random.
     * @param entityId The entityID that the entity will take on.
     * @param sendTo Who should it send the packet to?
     */
    public static void sendFakePlayerSpawnPacket(
            final @NotNull Location location,
            final UUID uuid,
            final int entityId,
            final @NotNull List<Player> sendTo
    ) {
        if (HMCCosmeticsAPI.getNMSVersion().contains("v1_19_R3") || HMCCosmeticsAPI.getNMSVersion().contains("v1_20_R1")) {
            WrapperPlayServerNamedEntitySpawn wrapper = new WrapperPlayServerNamedEntitySpawn();
            wrapper.setEntityID(entityId);
            wrapper.setPlayerUUID(uuid);
            wrapper.setPosition(location.toVector());
            wrapper.setPitch(location.getPitch());
            wrapper.setYaw(location.getYaw());
            for (final Player p : sendTo) sendPacket(p, wrapper.getHandle());
            return;
        }
        sendEntitySpawnPacket(location, entityId, EntityType.PLAYER, uuid);
    }

    /**
     * Creates a fake player entity.
     * @param skinnedPlayer The original player it bases itself off of.
     * @param uuid UUID of the fake entity.
     * @param sendTo Whom to send the packet to
     */
    public static void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final int entityId,
            final UUID uuid,
            final String NPCName,
            final List<Player> sendTo
    ) {
        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
        info.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        String name = NPCName;
        while (name.length() > 16) {
            name = name.substring(16);
        }

        WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uuid, name);
        WrappedSignedProperty skinData = HMCCPlayerUtils.getSkin(skinnedPlayer);
        if (skinData != null) wrappedGameProfile.getProperties().put("textures", skinData);

        info.getHandle().getPlayerInfoDataLists().write(1, Collections.singletonList(new PlayerInfoData(
                wrappedGameProfile,
                0,
                EnumWrappers.NativeGameMode.CREATIVE,
                WrappedChatComponent.fromText(name)
        )));
        for (final Player p : sendTo) sendPacket(p, info.getHandle());
    }

    /**
     * Generates the overlay packet for entities.
     * @param playerId The entity the packet is about
     * @param sendTo Whom is sent the packet.
     */
    public static void sendPlayerOverlayPacket(
            final int playerId,
            final @NotNull List<Player> sendTo
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
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, playerId);

        final List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();
        wrappedDataValueList.add(new WrappedDataValue(17, WrappedDataWatcher.Registry.get(Byte.class), mask));
        packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);

        for (final Player p : sendTo) sendPacket(p, packet);
    }

    /**
     * Removes a fake player from being seen by players.
     * @param player Which gameprofile to wrap for removing the player.
     * @param uuid What is the fake player UUID
     * @param sendTo Whom to send the packet to
     */
    @SuppressWarnings("deprecation")
    public static void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO_REMOVE);
        packet.getUUIDLists().write(0, List.of(uuid));
        for (final Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendLeashPacket(
            final int leashedEntity,
            final int entityId,
            final Location location
    ) {
        sendLeashPacket(leashedEntity, entityId, getViewers(location));
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
            final @NotNull Location from,
            final @NotNull Location to,
            final boolean onGround,
            @NotNull List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
        WrapperPlayServerRelEntityMove wrapper = new WrapperPlayServerRelEntityMove(packet);
        wrapper.setEntityID(entityId);
        wrapper.setDx(to.getX() - from.getX());
        wrapper.setDy(to.getY() - from.getY());
        wrapper.setDz(to.getZ() - from.getZ());
        wrapper.setOnGround(onGround);
        for (final Player p : sendTo) {
            sendPacket(p, wrapper.getHandle());
        }
    }

    @NotNull
    public static List<Player> getViewers(Location location) {
        ArrayList<Player> viewers = new ArrayList<>();
        if (Settings.getViewDistance() <= 0) {
            viewers.addAll(location.getWorld().getPlayers());
        } else {
            viewers.addAll(HMCCPlayerUtils.getNearbyPlayers(location));
        }
        return viewers;
    }

    public static void sendPacket(Player player, PacketContainer packet) {
        if (player == null) return;
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, null,false);
    }
}
