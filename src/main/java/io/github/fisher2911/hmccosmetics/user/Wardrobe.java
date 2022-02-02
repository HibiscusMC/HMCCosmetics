package io.github.fisher2911.hmccosmetics.user;

import com.mojang.authlib.GameProfile;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Wardrobe extends User {

    private final int entityId;
    private boolean active;

    public Wardrobe(
            final UUID uuid,
            final PlayerArmor playerArmor,
            final int armorStandId,
            final int entityId,
            final boolean active) {
        super(uuid, playerArmor, armorStandId);
        this.entityId = entityId;
        this.active = active;
    }

    public void spawnFakePlayer(final Player viewer) {
        PacketManager.sendPacket(viewer, PacketManager.getFakePlayerPacket(viewer.getLocation(), viewer, this.getUuid(), this.entityId));
    }

    public void despawnFakePlayer(final Player viewer) {
        PacketManager.sendPacket(viewer, PacketManager.getRemovePlayerPacket(viewer, this.getUuid(), this.entityId));
    }

    @Override
    public boolean hasPermissionToUse(final ArmorItem armorItem) {
        return true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}
