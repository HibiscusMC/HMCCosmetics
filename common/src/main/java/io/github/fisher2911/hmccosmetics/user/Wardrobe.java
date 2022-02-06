package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.events.PacketContainer;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.config.WardrobeSettings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.util.UUID;

public class Wardrobe extends User {

    private final HMCCosmetics plugin;
    private final UUID ownerUUID;
    private final int entityId;
    private boolean active;

    private boolean spawned;

    private Location currentLocation;

    public Wardrobe(
            final HMCCosmetics plugin,
            final UUID uuid,
            final UUID ownerUUID,
            final PlayerArmor playerArmor,
            final int armorStandId,
            final int entityId,
            final boolean active) {
        super(uuid, playerArmor, armorStandId);
        this.plugin = plugin;
        this.ownerUUID = ownerUUID;
        this.entityId = entityId;
        this.active = active;
        this.wardrobe = this;
    }

    public void spawnFakePlayer(final Player viewer) {
        final WardrobeSettings settings = this.plugin.getSettings().getWardrobeSettings();
        if (settings.inDistanceOfStatic(viewer.getLocation())) {
            this.currentLocation = settings.getLocation();
        } else if (this.currentLocation == null) {
            this.currentLocation = viewer.getLocation().clone();
            this.currentLocation.setPitch(0);
            this.currentLocation.setYaw(0);
        } else if (this.spawned) {
            return;
        }

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
        this.spawned = true;
    }

    @Override
    public void updateArmorStand(final Player player, final Settings settings) {
        this.updateArmorStand(player, settings, this.currentLocation);
    }

    public void despawnFakePlayer(final Player viewer) {
        final WardrobeSettings settings = this.plugin.getSettings().getWardrobeSettings();
        PacketManager.sendPacket(viewer, PacketManager.getEntityDestroyPacket(this.getEntityId()));
        this.despawnAttached();
        this.active = false;
        this.spawned = false;
        this.currentLocation = null;
        this.getPlayerArmor().clear();

        if (settings.isAlwaysDisplay()) {
            this.currentLocation = settings.getLocation();
            if (this.currentLocation == null) return;
            this.spawnFakePlayer(viewer);
        }
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

    public void setCurrentLocation(final Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    @Nullable
    public Location getCurrentLocation() {
        return currentLocation;
    }

    @Override
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(this.ownerUUID);
    }

}
