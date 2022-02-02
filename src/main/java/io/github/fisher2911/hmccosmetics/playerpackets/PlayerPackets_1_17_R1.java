package io.github.fisher2911.hmccosmetics.playerpackets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerPackets_1_17_R1 implements PlayerPackets {

    public PacketContainer[] getSpawnPacket(final Location location, final Player player, final UUID uuid, final int entityId) {
        final PacketContainer playerInfoPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        final StructureModifier<EnumWrappers.PlayerInfoAction> action = playerInfoPacket.getPlayerInfoAction();
        final StructureModifier<List<PlayerInfoData>> infoData = playerInfoPacket.getPlayerInfoDataLists();

        final List<PlayerInfoData> playerInfoData = new ArrayList<>();

        final GameProfile profile = this.getCopyProfile(player, uuid);

        playerInfoData.add(new PlayerInfoData(WrappedGameProfile
                .fromHandle(profile),
                0,
                EnumWrappers.NativeGameMode.fromBukkit(GameMode.CREATIVE),
                WrappedChatComponent.fromText("")));

        action.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        infoData.write(0, playerInfoData);

        final PacketContainer spawnPacket = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPacket.getUUIDs().write(0, uuid);
        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getDoubles().
                write(0, location.getX()).
                write(1, location.getY()).
                write(2, location.getZ());

        return new PacketContainer[]{playerInfoPacket, spawnPacket};
    }

    public PacketContainer getRemovePacket(final Player player, final UUID uuid, final int entityId) {
        final PacketContainer playerPacket = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        playerPacket.getIntegers().write(0, entityId);
        final StructureModifier<EnumWrappers.PlayerInfoAction> action = playerPacket.getPlayerInfoAction();
        final StructureModifier<List<PlayerInfoData>> infoData = playerPacket.getPlayerInfoDataLists();

        final List<PlayerInfoData> playerInfoData = new ArrayList<>();

        final GameProfile profile = this.getCopyProfile(player, uuid);

        playerInfoData.add(new PlayerInfoData(WrappedGameProfile
                .fromHandle(profile),
                0,
                EnumWrappers.NativeGameMode.fromBukkit(GameMode.CREATIVE),
                WrappedChatComponent.fromText(profile.getName())));

        action.write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        infoData.write(0, playerInfoData);

        return playerPacket;
    }

    private GameProfile getCopyProfile(final Player player, final UUID uuid) {
        final GameProfile playerProfile = ((CraftPlayer) player).getProfile();
        final GameProfile profile = new GameProfile(
                uuid,
                player.getDisplayName());

        for (final var entry : playerProfile.getProperties().entries()) {
            profile.getProperties().put(entry.getKey(), entry.getValue());
        }

        return profile;
    }

}
