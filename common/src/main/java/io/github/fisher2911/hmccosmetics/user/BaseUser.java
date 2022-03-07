package io.github.fisher2911.hmccosmetics.user;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.CosmeticSettings;
import io.github.fisher2911.hmccosmetics.config.Settings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.gui.BalloonItem;
import io.github.fisher2911.hmccosmetics.hook.HookManager;
import io.github.fisher2911.hmccosmetics.hook.ModelEngineHook;
import io.github.fisher2911.hmccosmetics.hook.entity.BalloonEntity;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

    private final HMCCosmetics plugin;
    protected final T id;
    protected final EntityIds entityIds;
    protected final BalloonEntity balloon;
    protected final PlayerArmor playerArmor;

    protected ArmorItem lastSetItem = ArmorItem.empty(ArmorItem.Type.HAT);

    // List of players that are currently viewing the armor stand
    protected final Set<UUID> viewingArmorStand = new HashSet<>();
    protected final Set<UUID> viewingBalloon = new HashSet<>();

    public BaseUser(final T id, final PlayerArmor playerArmor, final EntityIds entityIds) {
        this.id = id;
        this.entityIds = entityIds;
        this.playerArmor = playerArmor;
        this.plugin = HMCCosmetics.getPlugin(HMCCosmetics.class);
        this.balloon = new BalloonEntity(UUID.randomUUID(), -1);
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

    protected void despawnBalloon() {
        final HookManager hookManager = HookManager.getInstance();
        if (!hookManager.isEnabled(ModelEngineHook.class)) return;
        hookManager.getModelEngineHook().remove(this.balloon.getUniqueId());
        PacketManager.sendPacketToOnline(PacketManager.getEntityDestroyPacket(this.getBalloonId()));
        this.viewingBalloon.clear();
    }

    protected void despawnBalloon(final Player other) {
        final HookManager hookManager = HookManager.getInstance();
        if (!hookManager.isEnabled(ModelEngineHook.class)) return;
        hookManager.getModelEngineHook().removePlayerFromModel(other, this.balloon.getUniqueId());
        PacketManager.sendPacket(other, PacketManager.getEntityDestroyPacket(this.getBalloonId()));
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
        this.balloon.setAlive(true);
        if (!this.viewingBalloon.contains(other.getUniqueId())) {
            this.viewingBalloon.add(other.getUniqueId());
            this.balloon.setLocation(actual);
            final ModelEngineHook hook = hookManager.getModelEngineHook();
            hook.spawnModel(id, this.balloon);
            hook.addPlayerToModel(other, id, this.balloon);
        }
        this.updateBalloon(other, location, settings);
        PacketManager.sendPacket(
                other,
                PacketManager.getEntitySpawnPacket(
                        actual,
                        this.getBalloonId(),
                        this.balloon.getType()
                ),
                PacketManager.getInvisibilityPacket(this.getBalloonId()),
                PacketManager.getLeashPacket(
                        this.getBalloonId(),
                        this.getEntityId()
                )
        );
    }

    protected void updateBalloon(final Player other, final Location location, final CosmeticSettings settings) {
        if (!this.viewingBalloon.contains(other.getUniqueId())) {
            this.spawnBalloon(other, location, settings);
            return;
        }
        final Location actual = location.clone().add(settings.getBalloonOffset());
        final Location previous = this.balloon.getLocation();
        this.balloon.setLocation(actual);
        this.balloon.setVelocity(actual.clone().subtract(previous.clone()).toVector());
        PacketManager.sendPacket(
                other,
                PacketManager.getTeleportPacket(this.getBalloonId(), actual),
                PacketManager.getLeashPacket(this.getBalloonId(), this.getEntityId())
        );
    }

    private void spawnArmorStand(final Player other, final Location location) {
        final PacketContainer packet = PacketManager.getEntitySpawnPacket(location, this.getArmorStandId(), EntityType.ARMOR_STAND);
        PacketManager.sendPacket(other, packet);
    }

    public void updateOutsideCosmetics(final Settings settings) {
        final Location location = this.getLocation();
        if (location == null) return;
        for (final Player player : Bukkit.getOnlinePlayers()) {
            this.spawnOutsideCosmetics(player, location, settings);
        }
    }

    public void updateOutsideCosmetics(final Player other, final Location location, final Settings settings) {
        final boolean inViewDistance = this.isInViewDistance(location, other.getLocation(), settings.getCosmeticSettings());
        final boolean shouldShow = shouldShow(other);
        final UUID otherUUID = other.getUniqueId();
        final boolean hasBackpack = !this.playerArmor.getItem(ArmorItem.Type.BACKPACK).isEmpty();
        if (!this.viewingArmorStand.contains(otherUUID)) {
            if (!inViewDistance || !shouldShow || !hasBackpack) {
                if (this.viewingBalloon.contains(otherUUID)) {
                    this.despawnAttached(other);
                }
                return;
            }
            this.spawnArmorStand(other, location);
            this.viewingArmorStand.add(otherUUID);
        } else if (!inViewDistance || !shouldShow || !hasBackpack) {
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

        final List<Pair<EnumWrappers.ItemSlot, ItemStack>> equipmentList = new ArrayList<>();
        final boolean hidden = !this.shouldShow(other);
        if (hidden) {
            equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    new ItemStack(Material.AIR)
            ));
        } else {
            equipmentList.add(new Pair<>(EnumWrappers.ItemSlot.HEAD,
                    this.playerArmor.getBackpack().getItemStack(ArmorItem.Status.APPLIED)
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

    private boolean hasBalloon() {
        return (this.playerArmor.getItem(ArmorItem.Type.BALLOON) instanceof final BalloonItem balloonItem &&
                !balloonItem.getModelId().isBlank());
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
        this.viewingArmorStand.remove(other.getUniqueId());
    }

    public void despawnAttached() {
        PacketManager.sendPacketToOnline(PacketManager.getEntityDestroyPacket(this.getArmorStandId()));
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

}
