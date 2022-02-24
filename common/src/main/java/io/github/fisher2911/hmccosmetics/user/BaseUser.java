package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import io.github.fisher2911.hmccosmetics.config.CosmeticSettings;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.EntityIds;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public abstract class BaseUser<T> {

    protected final T id;
    protected final EntityIds entityIds;
    protected final PlayerArmor playerArmor;

    protected ArmorItem lastSetItem = ArmorItem.empty(ArmorItem.Type.HAT);

    protected boolean hasArmorStand;
    protected boolean hasBallon;

    // List of players that are currently viewing the armor stand
    protected final Set<UUID> viewing = new HashSet<>();

    public BaseUser(final T id, final PlayerArmor playerArmor, final EntityIds entityIds) {
        this.id = id;
        this.entityIds = entityIds;
        this.playerArmor = playerArmor;
    }

    @Nullable
    public abstract Location getLocation();

    public T getId() {
        return this.id;
    }

    public PlayerArmor getPlayerArmor() {
        return playerArmor;
    }

    protected void setPlayerArmor(final PlayerArmor playerArmor) {
        for (final ArmorItem armorItem : playerArmor.getArmorItems()) {
            this.playerArmor.setItem(armorItem);
        }
    }

    protected void removeAllCosmetics() {
        for (final ArmorItem.Type type : ArmorItem.Type.values()) {
            this.removeItem(type);
        }
    }

    public int getArmorStandId() {
        return this.entityIds.armorStand();
    }

    public int getBalloonId() {
        return this.entityIds.balloon();
    }

    protected ArmorItem setItem(final ArmorItem armorItem) {
        this.lastSetItem = armorItem;
        return this.playerArmor.setItem(armorItem);
    }

    protected ArmorItem removeItem(final ArmorItem.Type type) {
        return this.setItem(ArmorItem.empty(type));
    }

    public void spawnOutsideCosmetics(final Player other, final Location location, final Settings settings) {
        if (this.getLocation() == null) return;
        this.updateOutsideCosmetics(other, location, settings);
    }

    private void despawnBalloon(final Player other) {
        final PacketContainer removePacket = PacketManager.getEntityDestroyPacket(this.getBalloonId());
        PacketManager.sendPacket(other, removePacket);
    }

    private void spawnBalloon(final Player other, final Location location, final CosmeticSettings settings) {
        final Location actual = location.add(0, 5, 0);
        final PacketContainer spawnPacket = PacketManager.getEntitySpawnPacket(actual, this.getBalloonId(), EntityType.PIG);
        final PacketContainer leashPacket = PacketManager.getLeashPacket(this.getBalloonId(), this.getEntityId());
        PacketManager.sendPacket(other, spawnPacket, leashPacket);
    }

    private void updateBalloon(final CosmeticSettings settings) {
        final Location location = this.getLocation();
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!this.hasBallon) {
                this.spawnBalloon(player, location, settings);
            } else {
                this.updateBalloon(player, location, settings);
            }
        }
        this.hasBallon = true;
    }

    private void updateBalloon(final Player other, final Location location, final CosmeticSettings settings) {
//        final PacketContainer spawnPacket = PacketManager.getEntitySpawnPacket(location, this.getBalloonId(), EntityType.PARROT);
//        PacketManager.sendPacket(other, spawnPacket, spawnPacket);
    }

    private void spawnArmorStand(final Player other, final Location location, final CosmeticSettings settings) {
//        if (!this.isInViewDistance(this.getLocation(), other.getLocation(), settings) || !shouldShow(other)) return;
        final PacketContainer packet = PacketManager.getEntitySpawnPacket(location, this.getArmorStandId(), EntityType.ARMOR_STAND);
        PacketManager.sendPacket(other, packet);
    }

//    private void spawnArmorStand(final Settings settings) {
//        if (this.hasArmorStand) {
//            this.updateArmorStand(settings);
//            return;
//        }
//
//        for (final Player p : Bukkit.getOnlinePlayers()) {
//            this.spawnArmorStand(p, this.getLocation(), settings.getCosmeticSettings());
//        }
//
//        this.hasArmorStand = true;
//    }

    public void updateOutsideCosmetics(final Player player, final Settings settings) {
        this.updateOutsideCosmetics(player, this.getLocation(), settings);
    }

    public void updateOutsideCosmetics(final Settings settings) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.spawnOutsideCosmetics(player, this.getLocation(), settings);
        }
    }

    public void updateOutsideCosmetics(final Player other, final Location location, final Settings settings) {
        final boolean inViewDistance = this.isInViewDistance(this.getLocation(), other.getLocation(), settings.getCosmeticSettings());
        if (!this.viewing.contains(other.getUniqueId())) {
            if (!inViewDistance || !shouldShow(other)) return;
            this.spawnArmorStand(other, location, settings.getCosmeticSettings());
            this.spawnBalloon(other, location, settings.getCosmeticSettings());
        } else if (!inViewDistance || !shouldShow(other)) {
            this.despawnAttached(other);
            this.despawnBalloon(other);
            this.viewing.remove(other.getUniqueId());
            return;
        }
        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();
        final boolean hidden = !this.shouldShow(other);
        if (hidden) {
            equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    new ItemStack(Material.AIR)
            ));
        } else {
            equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    this.playerArmor.getBackpack().getColored()
            ));
        }

        final int armorStandId = this.getArmorStandId();
        final PacketContainer armorPacket = PacketManager.getEquipmentPacket(equipmentList, armorStandId);
        final PacketContainer rotationPacket = PacketManager.getRotationPacket(armorStandId, location);
        final PacketContainer ridingPacket = PacketManager.getRidingPacket(this.getEntityId(), armorStandId);
        final PacketContainer armorStandMetaContainer = PacketManager.getArmorStandMetaContainer(armorStandId);

        PacketManager.sendPacket(other, armorPacket, armorStandMetaContainer, rotationPacket, ridingPacket);

        if (hidden) return;

        this.updateBalloon(other, location, settings.getCosmeticSettings());

        final int lookDownPitch = settings.getCosmeticSettings().getLookDownPitch();

        if (lookDownPitch != -1 &&
                this.isFacingDown(location, lookDownPitch)) {
            equipmentList.set(0, new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    new ItemStack(Material.AIR)
            ));

            if (!this.id.equals(other.getUniqueId())) return;
            PacketManager.sendPacket(other, PacketManager.getEquipmentPacket(equipmentList, armorStandId));
        }
    }

    public abstract boolean shouldShow(final Player other);

    protected boolean isInViewDistance(final Location location, final Location other, final CosmeticSettings settings) {
        if (!Objects.equals(other.getWorld(), location.getWorld())) return false;
        return !(other.distanceSquared(location) > settings.getViewDistance() * settings.getViewDistance());
    }

    protected boolean isFacingDown(final Location location, final int pitchLimit) {
        return location.getPitch() > pitchLimit;
    }

    public void despawnAttached(final Player other) {
        PacketManager.sendPacket(other, PacketManager.getEntityDestroyPacket(this.getArmorStandId()));
        this.viewing.remove(other.getUniqueId());
        this.hasArmorStand = false;
    }

    public void despawnAttached() {
        PacketManager.sendPacketToOnline(PacketManager.getEntityDestroyPacket(this.getArmorStandId()));
        this.hasArmorStand = false;
    }

    public boolean hasArmorStand() {
        return hasArmorStand;
    }

    public ArmorItem getLastSetItem() {
        return lastSetItem;
    }

    public int getEntityId() {
        return this.entityIds.self();
    }

    public abstract Equipment getEquipment();

    public abstract boolean isWardrobeActive();

}
