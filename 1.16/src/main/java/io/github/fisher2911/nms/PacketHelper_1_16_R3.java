package io.github.fisher2911.nms;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketHelper_1_16_R3 implements PacketHelper {

    @Override
    public PacketContainer getPlayerSpawnPacket(final Location location, UUID uuid, final int entityId) {
        final PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPacket.getUUIDs().write(0, uuid);
        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getDoubles().
                write(0, location.getX()).
                write(1, location.getY()).
                write(2, location.getZ());
        spawnPacket.getBytes().write(0, (byte)(((location.getYaw() * 256.0F) / 360.0F)));

        return spawnPacket;
    }

    @Override
    public PacketContainer getPlayerInfoPacket(final Player player, final UUID uuid) {
        final GameProfile profile = this.getCopyProfile(player, uuid);
        final PacketContainer playerInfoPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        final StructureModifier<EnumWrappers.PlayerInfoAction> action = playerInfoPacket.getPlayerInfoAction();
        final StructureModifier<List<PlayerInfoData>> infoData = playerInfoPacket.getPlayerInfoDataLists();

        final List<PlayerInfoData> playerInfoData = new ArrayList<>();

        playerInfoData.add(new PlayerInfoData(WrappedGameProfile
                .fromHandle(profile),
                0,
                EnumWrappers.NativeGameMode.fromBukkit(GameMode.CREATIVE),
                WrappedChatComponent.fromText(profile.getName())));

        action.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        infoData.write(0, playerInfoData);

        return playerInfoPacket;
    }

    @Override
    public PacketContainer getPlayerRemovePacket(final Player player, final UUID uuid, final int entityId) {
        final PacketContainer playerPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        final StructureModifier<EnumWrappers.PlayerInfoAction> action = playerPacket.getPlayerInfoAction();
        final StructureModifier<List<PlayerInfoData>> infoData = playerPacket.getPlayerInfoDataLists();

        final List<PlayerInfoData> playerInfoData = new ArrayList<>();

        final GameProfile profile = this.getCopyProfile(player, uuid);

        playerInfoData.add(new PlayerInfoData(WrappedGameProfile
                .fromHandle(profile),
                0,
                EnumWrappers.NativeGameMode.fromBukkit(GameMode.CREATIVE),
                WrappedChatComponent.fromText("")));

        action.write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        infoData.write(0, playerInfoData);

        return playerPacket;
    }

    @Override
    public PacketContainer getPlayerOverlayPacket(final int entityId) {
        final PacketContainer metaContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        WrappedDataWatcher metaData = new WrappedDataWatcher();

        final WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);

        final byte mask = 0x01 | 0x02 | 0x04 | 0x08 | 0x010 | 0x020 | 0x40;

        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(16, byteSerializer), mask);

        metaContainer.getIntegers().write(0, entityId);
        metaContainer.getWatchableCollectionModifier().write(0, metaData.getWatchableObjects());
        return metaContainer;
    }

    private GameProfile getCopyProfile(final Player player, final UUID uuid) {
        final GameProfile playerProfile = ((CraftPlayer) player).getProfile();
        final GameProfile profile = new GameProfile(
                uuid,
                player.getDisplayName()
        );

        profile.getProperties().removeAll("textures");
        Property textureProperty = playerProfile.getProperties().get("textures").iterator().next();
        String texture = textureProperty.getValue();
        String signature = textureProperty.getSignature();
        profile.getProperties().put("textures", new Property("textures", texture, signature));

        return profile;
    }

    @Override
    public PacketContainer getArmorStandMeta(final int armorStandId) {
        final PacketContainer metaContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

        WrappedDataWatcher metaData = new WrappedDataWatcher();

        final WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);

        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer), (byte) (0x20));
        metaData.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(14, byteSerializer), (byte) (0x10));

        metaContainer.getIntegers().write(0, armorStandId);
        metaContainer.getWatchableCollectionModifier().write(0, metaData.getWatchableObjects());
        return metaContainer;
    }

    @Override
    public PacketContainer getDestroyPacket(final int entityId) {
        final PacketContainer destroyPacket = new PacketContainer(
                PacketType.Play.Server.ENTITY_DESTROY);
        destroyPacket.getIntegerArrays().write(0, new int[]{entityId});

        return destroyPacket;
    }

}
