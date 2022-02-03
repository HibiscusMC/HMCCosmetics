package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.events.PacketContainer;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Wardrobe extends User {

    private final UUID ownerUUID;
    private final int entityId;
    private boolean active;

    private Location currentLocation;

    public Wardrobe(
            final UUID uuid,
            final UUID ownerUUID,
            final PlayerArmor playerArmor,
            final int armorStandId,
            final int entityId,
            final boolean active) {
        super(uuid, playerArmor, armorStandId);
        this.ownerUUID = ownerUUID;
        this.entityId = entityId;
        this.active = active;
        this.wardrobe = this;
    }

    public void spawnFakePlayer(final Player viewer, final HMCCosmetics plugin) {
        this.currentLocation = viewer.getLocation().clone();
        this.currentLocation.setPitch(0);
        this.currentLocation.setYaw(0);
        final PacketContainer playerSpawnPacket = PacketManager.getFakePlayerSpawnPacket(
                this.currentLocation,
                this.getUuid(),
                this.entityId
        );
        final PacketContainer playerInfoPacket = PacketManager.getFakePlayerInfoPacket(
                viewer,
                this.getUuid()
        );
        PacketManager.sendPacket(viewer, playerInfoPacket, playerSpawnPacket);
        this.spawnArmorStand(viewer);
        this.updateArmorStand(viewer, plugin.getSettings(), this.currentLocation);
    }

    @Override
    public void updateArmorStand(final Player player, final Settings settings) {
        this.updateArmorStand(player, settings, this.currentLocation);
    }

    public void despawnFakePlayer(final Player viewer) {
        PacketManager.sendPacket(viewer, PacketManager.getEntityDestroyPacket(this.getEntityId()));
        this.despawnAttached();
        this.active = false;
    }

    @Override
    public int getEntityId() {
        return this.entityId;
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

    @Override
    public Player getPlayer() {
        return Bukkit.getPlayer(this.ownerUUID);
    }

}
