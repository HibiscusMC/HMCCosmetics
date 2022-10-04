package io.github.fisher2911.hmccosmetics.packet.versions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerNamedEntitySpawn;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerPlayerInfo;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerRelEntityMove;
import io.github.fisher2911.hmccosmetics.packet.wrappers.WrapperPlayServerRelEntityMoveLook;
import io.github.fisher2911.hmccosmetics.user.Equipment;
import io.github.fisher2911.hmccosmetics.util.PlayerUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PacketBase {

    // This is a PacketBase made to be in parity with the latest version. Other classes can override certain methods.

    List<String> version;

    protected PacketBase(@NotNull final List<String> version) {
        this.version = version;

        PacketManager.addPacketBase(this);
    }

    public List<String> getVersion() {
        return version;
    }

    public void sendArmorStandMetaContainer(final int armorStandId, final Player... sendTo) {
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
    public void sendHeadLookPacket(int entityId, float yaw, Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
            WrapperPlayServerRelEntityMoveLook wrapper = new WrapperPlayServerRelEntityMoveLook(packet);
            wrapper.setYaw(yaw);
            wrapper.setEntityID(entityId);
            sendPacketAsync(p, wrapper.getHandle());
        }
    }
    public void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Collection<? extends Player> sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, sendTo.toArray(new Player[0]));
    }
    public void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final Player... sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, UUID.randomUUID(), sendTo);
    }
    public void sendEntitySpawnPacket(
            final Location location,
            final int entityId,
            final EntityType entityType,
            final UUID uuid,
            final Collection<? extends Player> sendTo) {
        sendEntitySpawnPacket(location, entityId, entityType, uuid, sendTo.toArray(new Player[0]));
    }

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
    @Deprecated
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
    public void sendInvisibilityPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendInvisibilityPacket(entityId, sendTo.toArray(new Player[0]));
    }

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
    public void sendTeleportPacket(
            final int entityId,
            final Location location,
            boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        sendTeleportPacket(entityId, location, onGround, sendTo.toArray(new Player[0]));
    }


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
    public void sendMovePacket(
            final int entityId,
            final Location from,
            final Location to,
            final boolean onGround,
            final Collection<? extends Player> sendTo
    ) {
        sendMovePacket(entityId, from, to, onGround, sendTo.toArray(new Player[0]));
    }

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
    public void sendLeashPacket(
            final int balloonId,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        sendLeashPacket(balloonId, entityId, sendTo.toArray(new Player[0]));
    }

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
        if (list == null) return;
        packet.getSlotStackPairLists().write(0, list);
        for (Player p : sendTo) {
            sendPacketAsync(p, packet);
        }
    }

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
    public void sendLookPacket(
            final int entityId,
            final Location location,
            final Collection<? extends Player> sendTo
    ) {
        sendLookPacket(entityId, location, sendTo.toArray(new Player[0]));
    }

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

    public void sendEntityDestroyPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendEntityDestroyPacket(entityId, sendTo.toArray(new Player[0]));
    }

    public void sendEntityDestroyPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
            packet.getModifier().write(0, new IntArrayList(new int[]{entityId}));
            sendPacketAsync(p, packet);
        }
    }

    public void sendCameraPacket(final int entityId, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CAMERA);
            packet.getIntegers().write(0, entityId);
            sendPacketAsync(p, packet);
        }
    }

    public void sendCameraPacket(final Entity entity, final Player... sendTo) {
        for (final Player p : sendTo) {
            PacketContainer packet = new PacketContainer(PacketType.Play.Server.CAMERA);
            packet.getEntityModifier(p.getWorld()).write(0, entity);
            sendPacketAsync(p, packet);
        }
    }


    public void sendCameraPacket(final int entityId, final Collection<? extends Player> sendTo) {
        sendCameraPacket(entityId, sendTo.toArray(new Player[0]));
    }

    public void sendFakePlayerSpawnPacket(
            final Location location,
            final UUID uuid,
            final int entityId,
            final Collection<? extends Player> sendTo
    ) {
        sendFakePlayerSpawnPacket(location, uuid, entityId, sendTo.toArray(new Player[0]));
    }
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
    public void sendFakePlayerInfoPacket(
            final Player skinnedPlayer,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        sendFakePlayerInfoPacket(skinnedPlayer, uuid, sendTo.toArray(new Player[0]));
    }

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
    public void sendPlayerOverlayPacket(
            final int playerId,
            final Collection<? extends Player> sendTo
    ) {
        sendPlayerOverlayPacket(playerId, sendTo.toArray(new Player[0]));
    }

    public void sendPlayerOverlayPacket(
            final int playerId,
            final Player... sendTo
    ) {
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
    public void sendRemovePlayerPacket(
            final Player player,
            final UUID uuid,
            final Collection<? extends Player> sendTo
    ) {
        sendRemovePlayerPacket(player, uuid, sendTo.toArray(new Player[0]));
    }
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
    public void sendGameModeChange(
            final Player player,
            final GameMode gamemode
    ) {
        sendGameModeChange(player, PlayerUtils.convertGamemode(gamemode));
    }
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

    public void sendNewTeam(
            final Player skinnedPlayer,
            final Player... sendTo)
    {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
        packet.getStrings().write(0, "npcteam");
        packet.getBytes().write(0, (byte) 0);
        packet.getStrings().write(1, " "); // Team Displayname?
        packet.getStrings().write(2, "always");

        String name = "Mannequin-" + skinnedPlayer.getEntityId();
        while (name.length() > 16) {
            name = name.substring(16);
        }

        String[] nameList = new String[]{name};
        packet.getStringArrays().write(0, nameList);
        for (Player player : sendTo) {
            sendPacketAsync(player, packet);
        }
    }

    public void sendPacketAsync(final Player player, final PacketContainer packet) {
        Bukkit.getScheduler().runTaskAsynchronously(HMCCosmetics.getPlugin(HMCCosmetics.class),
                () -> ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet));
    }
}
