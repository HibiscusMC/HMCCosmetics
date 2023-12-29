package com.hibiscusmc.hmccosmetics.util.packets.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.hibiscusmc.hmccosmetics.api.HMCCosmeticsAPI;

import java.util.List;
import java.util.Set;

public class WrapperPlayServerPlayerInfo extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_INFO;

    public WrapperPlayServerPlayerInfo() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerPlayerInfo(PacketContainer packet) {
        super(packet, TYPE);
    }

    public PlayerInfoAction getAction() {
        return handle.getPlayerInfoAction().read(0);
    }

    public void setAction(PlayerInfoAction value) {
        if (HMCCosmeticsAPI.getNMSVersion().contains("v1_17_R1") || HMCCosmeticsAPI.getNMSVersion().contains("v1_18_R2") || HMCCosmeticsAPI.getNMSVersion().contains("v1_19_R1")) {
            handle.getPlayerInfoAction().write(0, value);
            return;
        }
        // New way of handling it 1.19.3+
        handle.getPlayerInfoActions().write(0, Set.of(value));
    }

    public List<PlayerInfoData> getData() {
        return handle.getPlayerInfoDataLists().read(0);
    }

    public void setData(List<PlayerInfoData> value) {
        handle.getPlayerInfoDataLists().write(0, value);
    }
}