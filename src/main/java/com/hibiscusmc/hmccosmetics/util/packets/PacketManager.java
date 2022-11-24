package com.hibiscusmc.hmccosmetics.util.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticArmorType;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.InventoryUtils;
import com.hibiscusmc.hmccosmetics.util.PlayerUtils;
import com.hibiscusmc.hmccosmetics.util.ServerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.wrappers.WrapperPlayServerNamedEntitySpawn;
import com.hibiscusmc.hmccosmetics.util.packets.wrappers.WrapperPlayServerPlayerInfo;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PacketManager extends BasePacket {

    public static void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final List<Player> sendTo
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

    public static void gamemodeChangePacket(
            Player player,
            int gamemode
    ) {
        sendPacket(player, new ClientboundGameEventPacket(ClientboundGameEventPacket.CHANGE_GAME_MODE, (float) gamemode));
        HMCCosmeticsPlugin.getInstance().getLogger().info("Gamemode Change sent to " + player + " to be " + gamemode);
    }

    public static void ridingMountPacket(
            int mountId,
            int passengerId,
            List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, mountId);
        packet.getIntegerArrays().write(0, new int[]{passengerId});
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void equipmentSlotUpdate(
            Player player,
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
        equipmentSlotUpdate(user.getPlayer().getEntityId(), user, cosmeticSlot, sendTo);
    }

    public static void equipmentSlotUpdate(
            int entityId,
            CosmeticUser user,
            CosmeticSlot cosmeticSlot,
            List<Player> sendTo
    ) {
        EquipmentSlot nmsSlot = null;
        ItemStack nmsItem = null;

        if (cosmeticSlot == CosmeticSlot.BACKPACK || cosmeticSlot == CosmeticSlot.BALLOON) return;

        if (!(user.getCosmetic(cosmeticSlot) instanceof CosmeticArmorType)) {

            nmsSlot = CraftEquipmentSlot.getNMS(InventoryUtils.getEquipmentSlot(cosmeticSlot));
            nmsItem = CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(Material.AIR));

            Pair<EquipmentSlot, ItemStack> pair = new Pair<>(nmsSlot, nmsItem);

            List<Pair<EquipmentSlot, ItemStack>> pairs = Collections.singletonList(pair);

            ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, pairs);
            for (Player p : sendTo) sendPacket(p, packet);
            return;
        }
        CosmeticArmorType cosmeticArmor = (CosmeticArmorType) user.getCosmetic(cosmeticSlot);

        // Converting EquipmentSlot and ItemStack to NMS ones.
        nmsSlot = CraftEquipmentSlot.getNMS(cosmeticArmor.getEquipSlot());
        nmsItem = CraftItemStack.asNMSCopy(cosmeticArmor.getCosmeticItem());

        if (nmsSlot == null) return;

        Pair<EquipmentSlot, ItemStack> pair = new Pair<>(nmsSlot, nmsItem);

        List<Pair<EquipmentSlot, ItemStack>> pairs = Collections.singletonList(pair);

        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(entityId, pairs);
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void armorStandMetaPacket(
            Entity entity,
            List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        packet.getModifier().writeDefaults();
        packet.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher metadata = new WrappedDataWatcher();
        if (metadata == null) return;
        // 0x10 & 0x20
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
        metadata.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x10);
        packet.getWatchableCollectionModifier().write(0, metadata.getWatchableObjects());
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendInvisibilityPacket(
            int entityId,
            List<Player> sendTo
    ) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            packet.getModifier().writeDefaults();
            packet.getIntegers().write(0, entityId);
            WrappedDataWatcher wrapper = new WrappedDataWatcher();
            wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20);
            packet.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects());
            for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendLookPacket(
             int entityId,
             Location location,
             List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        packet.getIntegers().write(0, entityId);
        packet.getBytes().write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendRotationPacket(
             int entityId,
             Location location,
             boolean onGround,
             List<Player> sendTo
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


    /**
     * Mostly to deal with backpacks, this deals with entities riding other entities.
     * @param mountId The entity that is the "mount", ex. a player
     * @param passengerId The entity that is riding the mount, ex. a armorstand for a backpack
     * @param sendTo Whom to send the packet to
     */
    public static void sendRidingPacket(
            final int mountId,
            final int passengerId,
            final List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, mountId);
        packet.getIntegerArrays().write(0, new int[]{passengerId});
        for (final Player p : sendTo) {
            sendPacket(p, packet);
        }
    }

    /**
     * Destroys an entity from a player
     * @param entityId The entity to delete for a player
     * @param sendTo The players the packet should be sent to
     */
    public static void sendEntityDestroyPacket(final int entityId, List<Player> sendTo) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        packet.getModifier().write(0, new IntArrayList(new int[]{entityId}));
        for (final Player p : sendTo) sendPacket(p, packet);
    }

    /**
     * Sends a camera packet
     * @param entityId The Entity ID that camera will go towards
     * @param sendTo The players that will be sent this packet
     */
    public static void sendCameraPacket(final int entityId, List<Player> sendTo) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.CAMERA);
        packet.getIntegers().write(0, entityId);
        for (final Player p : sendTo) sendPacket(p, packet);
        HMCCosmeticsPlugin.getInstance().getLogger().info(sendTo + " | " + entityId + " has had a camera packet on them!");
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
            final List<Player> sendTo
    ) {
        WrapperPlayServerNamedEntitySpawn wrapper = new WrapperPlayServerNamedEntitySpawn();
        wrapper.setEntityID(entityId);
        wrapper.setPlayerUUID(uuid);
        wrapper.setPosition(location.toVector());
        wrapper.setPitch(location.getPitch());
        wrapper.setYaw(location.getYaw());
        for (final Player p : sendTo) sendPacket(p, wrapper.getHandle());
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
            final List<Player> sendTo
    ) {
        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
        info.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);

        String name = "Mannequin-" + entityId;
        while (name.length() > 16) {
            name = name.substring(16);
        }

        WrappedGameProfile wrappedGameProfile = new WrappedGameProfile(uuid, name);
        WrappedSignedProperty skinData = PlayerUtils.getSkin(skinnedPlayer);
        if (skinData != null) wrappedGameProfile.getProperties().put("textures", skinData);
        info.setData(List.of(new PlayerInfoData(wrappedGameProfile, 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(name))));
        for (final Player p : sendTo) sendPacket(p, info.getHandle());

    }

    /**
     * Generates the overlay packet for entities.
     * @param playerId The entity the packet is about
     * @param sendTo Whom is sent the packet.
     */
    public static void sendPlayerOverlayPacket(
            final int playerId,
            final List<Player> sendTo
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
        WrappedDataWatcher wrapper = new WrappedDataWatcher();
        wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(17, WrappedDataWatcher.Registry.get(Byte.class)), mask);
        wrapper.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x10);
        packet.getWatchableCollectionModifier().write(0, wrapper.getWatchableObjects());
        for (final Player p : sendTo) {
            sendPacket(p, packet);
        }
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
            final List<Player> sendTo
    ) {
        WrapperPlayServerPlayerInfo info = new WrapperPlayServerPlayerInfo();
        info.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);

        String name = "Mannequin-" + player.getEntityId();
        while (name.length() > 16) {
            name = name.substring(16);
        }

        info.setData(List.of(new PlayerInfoData(new WrappedGameProfile(uuid, player.getName()), 0, EnumWrappers.NativeGameMode.CREATIVE, WrappedChatComponent.fromText(name))));
        for (final Player p : sendTo) sendPacket(p, info.getHandle());
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
            final List<Player> sendTo
    ) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ATTACH_ENTITY);
            packet.getIntegers().write(0, leashedEntity);
            packet.getIntegers().write(1, entityId);
            sendPacket(p, packet);
        }
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
            final List<Player> sendTo
    ) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        packet.getIntegers().write(0, entityId);
        packet.getDoubles().write(0, location.getX());
        packet.getDoubles().write(1, location.getY());
        packet.getDoubles().write(2, location.getZ());
        packet.getBytes().write(0, (byte) (location.getYaw() * 256.0F / 360.0F));
        packet.getBytes().write(1, (byte) (location.getPitch() * 256.0F / 360.0F));
        packet.getBooleans().write(0, onGround);
        for (final Player p : sendTo) {
            sendPacket(p, packet);
        }
    }
}
