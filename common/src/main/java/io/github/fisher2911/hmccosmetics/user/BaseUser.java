package io.github.fisher2911.hmccosmetics.user;


import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import io.github.fisher2911.hmccosmetics.config.CosmeticSettings;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.BalloonItem;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.ModelEngineHook;
import io.github.fisher2911.hmccosmetics.hook.entity.BalloonEntity;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.retrooper.packetevents.util.SpigotDataHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class BaseUser<T> {

    protected final T id;
    protected final EntityIds entityIds;
    protected final BalloonEntity balloon;
    protected final PlayerArmor playerArmor;
    // for setting multiple items
    protected boolean armorUpdated;

    protected ArmorItem lastSetItem = ArmorItem.empty(ArmorItem.Type.HAT);

    // List of players that are currently viewing the armor stand
    protected final Set<UUID> viewingArmorStand = new HashSet<>();
    protected final Set<UUID> viewingBalloon = new HashSet<>();

    public BaseUser(final T id, final PlayerArmor playerArmor, final EntityIds entityIds) {
        this.id = id;
        this.entityIds = entityIds;
        this.playerArmor = playerArmor;
        if (!HookManager.getInstance().isEnabled(ModelEngineHook.class)) {
            this.balloon = null;
        } else {
            this.balloon = new BalloonEntity(UUID.randomUUID(), -1, EntityType.PUFFERFISH);
        }
    }

    @Nullable
    public abstract Location getLocation();

    @Nullable
    public abstract Vector getVelocity();

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

    public void despawnBalloon() {
        final HookManager hookManager = HookManager.getInstance();
        if (!hookManager.isEnabled(ModelEngineHook.class)) return;
        PacketManager.sendEntityDestroyPacket(this.getBalloonId(), Bukkit.getOnlinePlayers());
        this.balloon.remove();
        this.viewingBalloon.clear();
        this.balloon.setAlive(false);
    }

    protected void despawnBalloon(final Player other) {
        final HookManager hookManager = HookManager.getInstance();
        if (!hookManager.isEnabled(ModelEngineHook.class)) return;
        this.balloon.removePlayerFromModel(other);
        PacketManager.sendEntityDestroyPacket(this.getBalloonId(), other);
        this.viewingBalloon.remove(other.getUniqueId());
        if (this.viewingBalloon.isEmpty()) {
            this.despawnBalloon();
        }
    }

    private void spawnBalloon(final Player other, final Location location, final CosmeticSettings settings) {
        final Location actual = location.clone().add(settings.getBalloonOffset());
        final World world = location.getWorld();
        if (world == null) return;
        final BalloonItem balloonItem = (BalloonItem) this.playerArmor.getItem(ArmorItem.Type.BALLOON);
        final String id = balloonItem.getModelId();
        final HookManager hookManager = HookManager.getInstance();
        if (id.isBlank() || !hookManager.isEnabled(ModelEngineHook.class)) return;
        if (this.balloon.isAlive()) {
            this.updateBalloon(other, actual, settings);
            return;
        }
        this.balloon.setAlive(true);
        if (!this.viewingBalloon.contains(other.getUniqueId())) {
            this.viewingBalloon.add(other.getUniqueId());
            this.balloon.setLocation(actual);
            this.balloon.spawnModel(id);
            this.balloon.addPlayerToModel(other, id);
        }
        final int balloonId = this.getBalloonId();
        PacketManager.sendEntitySpawnPacket(actual, balloonId, EntityTypes.PUFFERFISH, other);
        PacketManager.sendInvisibilityPacket(balloonId, other);
        PacketManager.sendLeashPacket(balloonId, this.getEntityId(), other);
        this.updateBalloon(other, location, settings);
    }

    protected void updateBalloon(final Player other, final Location location, final CosmeticSettings settings) {
        final HookManager hookManager = HookManager.getInstance();
        if (!hookManager.isEnabled(ModelEngineHook.class)) return;
        final BalloonItem balloonItem = (BalloonItem) this.playerArmor.getItem(ArmorItem.Type.BALLOON);
        if (balloonItem.isEmpty()) return;
        if (!this.viewingBalloon.contains(other.getUniqueId())) {
            if (!this.balloon.isAlive()) {
                this.spawnBalloon(other, location, settings);
                return;
            }
            this.viewingBalloon.add(other.getUniqueId());
            this.balloon.addPlayerToModel(other, balloonItem.getModelId());
            return;
        }
        final Location actual = location.clone().add(settings.getBalloonOffset());
        final Location previous = this.balloon.getLocation();
        final Vector vector = this.getVelocity();
        if (vector != null) actual.add(this.getVelocity().multiply(-1));
        this.balloon.setLocation(actual);
        this.balloon.setVelocity(actual.clone().subtract(previous.clone()).toVector());
        this.balloon.updateModel();
        final int balloonId = this.getBalloonId();
        PacketManager.sendTeleportPacket(balloonId, actual, false, other);
        PacketManager.sendLeashPacket(balloonId, this.getEntityId(), other);
    }

    private void spawnArmorStand(final Player other, final Location location) {
        PacketManager.sendEntitySpawnPacket(location, this.getArmorStandId(), EntityTypes.ARMOR_STAND, other);
        PacketManager.sendArmorStandMetaContainer(this.getArmorStandId(), other);
    }

    public void updateOutsideCosmetics(final Settings settings) {
        final Location location = this.getLocation();
        if (location == null) return;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.spawnOutsideCosmetics(player, location, settings);
        }
    }

    public void updateBackpack(final Player other, final Settings settings) {
        final Location location = this.getLocation();
        if (location == null) return;
        final List<com.github.retrooper.packetevents.protocol.player.Equipment> equipment = new ArrayList<>();
        final boolean hidden = !this.shouldShow(other);
        final int lookDownPitch = settings.getCosmeticSettings().getLookDownPitch();
        final boolean isLookingDown =
                this.id.equals(other.getUniqueId()) && lookDownPitch
                        != -1 &&
                        this.isFacingDown(location, lookDownPitch);
        if (hidden || isLookingDown) {
            equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                    EquipmentSlot.HELMET,
                    new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder().
                            type(ItemTypes.AIR).
                            build()
            ));
        } else {
            final com.github.retrooper.packetevents.protocol.item.ItemStack itemStack =
                    SpigotDataHelper.fromBukkitItemStack(this.playerArmor.getBackpack().getItemStack(ArmorItem.Status.APPLIED));
            equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                    EquipmentSlot.HELMET,
                    itemStack
            ));
        }

        final int armorStandId = this.getArmorStandId();
        PacketManager.sendEquipmentPacket(equipment, armorStandId, other);
    }

    public void updateOutsideCosmetics(final Player other, final Location location, final Settings settings) {
        final boolean inViewDistance = settings.getCosmeticSettings().isInViewDistance(location, other.getLocation());
        final boolean shouldShow = shouldShow(other);
        final UUID otherUUID = other.getUniqueId();
        final boolean hasBackpack = !this.playerArmor.getItem(ArmorItem.Type.BACKPACK).isEmpty();
        if (!this.viewingArmorStand.contains(otherUUID)) {
            if (!inViewDistance || !shouldShow) {
                if (this.viewingBalloon.contains(otherUUID)) {
                    this.despawnBalloon(other);
                }
                return;
            }
            if (hasBackpack) {
                this.spawnArmorStand(other, location);
                this.viewingArmorStand.add(otherUUID);
            }
        } else if (!inViewDistance || !shouldShow) {
            this.despawnAttached(other);
            if (this.viewingBalloon.contains(otherUUID)) {
                this.despawnBalloon(other);
            }
            return;
        }

        if (!this.viewingBalloon.contains(otherUUID)) {
            this.spawnBalloon(other, location, settings.getCosmeticSettings());
        } else if (!this.hasBalloon()) {
            this.despawnBalloon(other);
        }

        final List<com.github.retrooper.packetevents.protocol.player.Equipment> equipment = new ArrayList<>();
        final boolean hidden = !this.shouldShow(other);
        final int lookDownPitch = settings.getCosmeticSettings().getLookDownPitch();
        final boolean isLookingDown =
                this.id.equals(other.getUniqueId()) && lookDownPitch
                        != -1 &&
                        this.isFacingDown(location, lookDownPitch);
        if (hidden || isLookingDown) {
            equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                    EquipmentSlot.HELMET,
                    new com.github.retrooper.packetevents.protocol.item.ItemStack.Builder().
                            type(ItemTypes.AIR).
                            build()
            ));
        } else {
            final com.github.retrooper.packetevents.protocol.item.ItemStack itemStack =
                    SpigotDataHelper.fromBukkitItemStack(this.playerArmor.getBackpack().getItemStack(ArmorItem.Status.APPLIED));
            equipment.add(new com.github.retrooper.packetevents.protocol.player.Equipment(
                    EquipmentSlot.HELMET,
                    itemStack
            ));
        }

        final int armorStandId = this.getArmorStandId();
        PacketManager.sendRotationPacket(armorStandId, location, false, other);
        PacketManager.sendLookPacket(armorStandId, location, other);
        PacketManager.sendRidingPacket(this.getEntityId(), armorStandId, other);

        if (hidden) return;
        this.updateBalloon(other, location, settings.getCosmeticSettings());
    }

    private boolean hasBalloon() {
        return (this.playerArmor.getItem(ArmorItem.Type.BALLOON) instanceof final BalloonItem balloonItem &&
                !balloonItem.getModelId().isBlank());
    }

    public abstract boolean shouldShow(final Player other);

    protected boolean isFacingDown(final Location location, final int pitchLimit) {
        return location.getPitch() > pitchLimit;
    }

    public void despawnAttached(final Player other) {
        PacketManager.sendEntityDestroyPacket(this.getArmorStandId(), other);
        this.viewingArmorStand.remove(other.getUniqueId());
    }

    public void despawnAttached() {
        PacketManager.sendEntityDestroyPacket(this.getArmorStandId(), Bukkit.getOnlinePlayers());
        this.viewingArmorStand.clear();
    }

    public ArmorItem getLastSetItem() {
        return lastSetItem;
    }

    public int getEntityId() {
        return this.entityIds.self();
    }

    public abstract Equipment getEquipment();

    public abstract boolean isWardrobeActive();

    public boolean isArmorUpdated() {
        return armorUpdated;
    }

    public void setArmorUpdated(boolean armorUpdated) {
        this.armorUpdated = armorUpdated;
    }
}
