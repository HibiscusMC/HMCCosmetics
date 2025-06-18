package com.hibiscusmc.hmccosmetics.util.packets;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.user.CosmeticUsers;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.HMCCPlayerUtils;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HMCCPacketManager extends PacketManager {

    private static final List<CosmeticSlot> EQUIPMENT_SLOTS = List.of(CosmeticSlot.HELMET, CosmeticSlot.CHESTPLATE, CosmeticSlot.LEGGINGS, CosmeticSlot.BOOTS, CosmeticSlot.MAINHAND, CosmeticSlot.OFFHAND);

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
        WrapperPlayServerSpawnEntity packet = new WrapperPlayServerSpawnEntity(
            entityId,
            uuid,
            SpigotConversionUtil.fromBukkitEntityType(entityType),
            SpigotConversionUtil.fromBukkitLocation(location),
            0,
            0,
            new Vector3d(0, 0, 0)
        );

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
        if (!EQUIPMENT_SLOTS.contains(cosmeticSlot)) return;
        equipmentSlotUpdate(entityId, HMCCInventoryUtils.getEquipmentSlot(cosmeticSlot), user.getUserCosmeticItem(cosmeticSlot), sendTo);
    }

    public static void sendArmorstandMetadata(
            int entityId,
            List<Player> sendTo
    ) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
            entityId,
            List.of(
                new EntityData(0, EntityDataTypes.BYTE, (byte) 0x21),
                new EntityData(15, EntityDataTypes.BYTE, (byte) 0x10)
            )
        );

        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendScalePacket(
            int entityId,
            double scale,
            List<Player> sendTo
    ) {
        WrapperPlayServerUpdateAttributes packet = new WrapperPlayServerUpdateAttributes(
            entityId,
            List.of(
                new WrapperPlayServerUpdateAttributes.Property(
                    Attributes.GENERIC_SCALE,
                    scale,
                    Collections.emptyList()
                )
            )
        );

        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendInvisibilityPacket(
            int entityId,
            List<Player> sendTo
    ) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
            entityId,
            List.of(
                new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20)
            )
        );

        for (Player p : sendTo) sendPacket(p, packet);
    }

    public static void sendCloudEffect(
            int entityId,
            List<Player> sendTo
    ) {
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
            entityId,
            List.of(
                new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                new EntityData(8, EntityDataTypes.FLOAT, 0f)
            )
        );

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

        // TODO: Verify that is the right packet (ProtocolLib called the packet type: ENTITY_LOOK)
        WrapperPlayServerEntityRotation packet = new WrapperPlayServerEntityRotation(
            entityId,
            yaw,
            pitch,
            onGround
        );

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

        // TODO: Verify that is the right packet (ProtocolLib called the packet type: ENTITY_LOOK)
        WrapperPlayServerEntityRotation packet = new WrapperPlayServerEntityRotation(
            entityId,
            yaw2,
            0,
            onGround
        );

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
        WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(
            mountId,
            passengerIds
        );

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
            WrapperPlayServerSpawnPlayer packet = new WrapperPlayServerSpawnPlayer(
                entityId,
                uuid,
                SpigotConversionUtil.fromBukkitLocation(location)
            );

            for (final Player p : sendTo) sendPacket(p, packet);
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
        String name = NPCName;
        while (name.length() > 16) {
            name = name.substring(16);
        }

        UserProfile userProfile = new UserProfile(uuid, name);
        TextureProperty skinData = HMCCPlayerUtils.getSkin(skinnedPlayer);
        if (skinData != null) {
            userProfile.setTextureProperties(List.of(skinData));
        }

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(
            WrapperPlayServerPlayerInfo.Action.ADD_PLAYER,
            new WrapperPlayServerPlayerInfo.PlayerData(
                Component.text(name),
                userProfile,
                GameMode.CREATIVE,
                0
            )
        );


        for (final Player p : sendTo) sendPacket(p, packet);
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
        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
            playerId,
            List.of(new EntityData(17, EntityDataTypes.BYTE, mask))
        );

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
        WrapperPlayServerPlayerInfoRemove packet = new WrapperPlayServerPlayerInfoRemove(
            List.of(uuid)
        );

        for (final Player p : sendTo) {
            sendPacket(p, packet);
        }
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
        WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(
            entityId,
            to.getX() - from.getX(),
            to.getY() - from.getY(),
            to.getZ() - from.getZ(),
            onGround
        );

        for (final Player p : sendTo) {
            sendPacket(p, packet);
        }
    }

    /**
     * Gets the nearby players (or viewers) of a location through the view distance set in the config. If the view distance is 0, it will return all players in the world.
     * @param location
     * @return
     */
    @NotNull
    public static List<Player> getViewers(@NotNull Location location) {
        return PacketManager.getViewers(location, Settings.getViewDistance());
    }

    public static void sendPacket(Player player, PacketWrapper<?> packet) {
        if (player == null) return;
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static @Nullable User findUser(int entityId) {
        return PacketEvents.getAPI().getProtocolManager().getUsers().stream()
            .filter(foundUser -> foundUser.getEntityId() == entityId)
            .findFirst()
            .orElse(null);
    }

    public static @Nullable UUID findUUID(int entityId) {
        User user = findUser(entityId);
        return user != null ? user.getUUID() : null;
    }
}
