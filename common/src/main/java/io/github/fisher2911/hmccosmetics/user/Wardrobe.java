package io.github.fisher2911.hmccosmetics.user;

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import io.github.fisher2911.hmccosmetics.HMCCosmetics;
import io.github.fisher2911.hmccosmetics.config.WardrobeSettings;
import io.github.fisher2911.hmccosmetics.gui.ArmorItem;
import io.github.fisher2911.hmccosmetics.inventory.PlayerArmor;
import io.github.fisher2911.hmccosmetics.packet.PacketManager;
import io.github.fisher2911.hmccosmetics.task.SupplierTask;
import io.github.fisher2911.hmccosmetics.task.Task;
import io.github.fisher2911.hmccosmetics.task.TaskChain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Wardrobe extends User {

    private final HMCCosmetics plugin;
    private final UUID ownerUUID;
    private boolean active;
    private boolean spawned;
    private Location currentLocation;

    public Wardrobe(
            final HMCCosmetics plugin,
            final UUID uuid,
            final UUID ownerUUID,
            final PlayerArmor playerArmor,
            final EntityIds entityIds,
            final boolean active) {
        super(uuid, playerArmor, entityIds);
        this.plugin = plugin;
        this.ownerUUID = ownerUUID;
        this.active = active;
        this.wardrobe = this;
    }

    public void spawnFakePlayer(final Player viewer) {
        final UserManager userManager = this.plugin.getUserManager();
        final WardrobeSettings settings = this.plugin.getSettings().getWardrobeSettings();

        Bukkit.getScheduler().runTaskLaterAsynchronously(
                this.plugin,
                () -> {
                    if (settings.inDistanceOfStatic(viewer.getLocation())) {
                        this.currentLocation = settings.getWardrobeLocation();
                        userManager.get(viewer.getUniqueId()).ifPresent(user -> userManager.sendUpdatePacket(user, userManager.getEmptyItemList()));
                        PacketManager.sendEntitySpawnPacket(
                                settings.getViewerLocation(),
                                this.entityIds.wardrobeViewer(),
                                EntityTypes.ARMOR_STAND,
                                viewer
                        );
                        PacketManager.sendCameraPacket(
                                this.entityIds.wardrobeViewer(),
                                viewer
                        );
                        PacketManager.sendInvisibilityPacket(
                                this.entityIds.wardrobeViewer(),
                                viewer
                        );
                        PacketManager.sendLookPacket(
                                this.entityIds.wardrobeViewer(),
                                settings.getViewerLocation(),
                                viewer
                        );
                        PacketManager.sendInvisibilityPacket(
                                viewer.getEntityId(),
                                viewer
                        );
                        this.hidePlayer();
                        this.setActive(true);
                    } else if (this.currentLocation == null) {
                        this.currentLocation = viewer.getLocation().clone();
                        this.currentLocation.setPitch(0);
                        this.currentLocation.setYaw(0);
                    } else if (this.spawned) {
                        return;
                    }
                    final int entityId = this.getEntityId();
                    PacketManager.sendFakePlayerInfoPacket(viewer, this.getId(), viewer);
                    PacketManager.sendFakePlayerSpawnPacket(this.currentLocation, this.getId(), entityId, viewer);
                    PacketManager.sendLookPacket(entityId, this.currentLocation, viewer);
                    PacketManager.sendRotationPacket(entityId, this.currentLocation, true, viewer);
                    PacketManager.sendPlayerOverlayPacket(entityId, viewer);
                    userManager.get(viewer.getUniqueId()).
                            ifPresent(user -> {
                                int index = 0;
                                final Collection<ArmorItem> armorItems = user.getPlayerArmor().getArmorItems();
                                for (final ArmorItem armorItem : armorItems) {
                                    index++;
                                    final boolean sendPacket = armorItems.size() == index;
                                    userManager.setItem(
                                            this,
                                            armorItem,
                                            sendPacket
                                    );
                                }
                            });
//                    PacketManager.sendEntitySpawnPacket(this.currentLocation, this.getEntityId(), EntityTypes.ZOMBIE, viewer);
                    this.spawned = true;
                    this.startSpinTask(viewer);
                },
                settings.getSpawnDelay()
        );
    }

    public void despawnFakePlayer(final Player viewer, final UserManager userManager) {
        this.active = false;
        final WardrobeSettings settings = this.plugin.getSettings().getWardrobeSettings();
        Bukkit.getScheduler().runTaskLaterAsynchronously(
                this.plugin,
                () -> {
                    this.spawned = false;
                    final int entityId = this.getEntityId();
                    this.despawnAttached();
                    this.despawnBalloon();
                    PacketManager.sendEntityDestroyPacket(entityId, viewer);
                    PacketManager.sendRemovePlayerPacket(viewer, this.id, viewer);
                    PacketManager.sendEntityDestroyPacket(
                            this.entityIds.wardrobeViewer(),
                            viewer
                    );
                    PacketManager.sendCameraPacket(
                            viewer.getEntityId(),
                            viewer
                    );
                    this.showPlayer(this.plugin.getUserManager());
                    final Collection<ArmorItem> armorItems = new ArrayList<>(this.getPlayerArmor().getArmorItems());
                    if (settings.isApplyCosmeticsOnClose()) {
                        final Optional<User> optionalUser = userManager.get(this.ownerUUID);
                        optionalUser.ifPresent(user -> Bukkit.getScheduler().runTask(
                                plugin,
                                () -> {
                                    int index = 0;
                                    for (final ArmorItem armorItem : armorItems) {
                                        index++;
                                        final boolean sendPacket = armorItems.size() == index;
                                        if (!user.hasPermissionToUse(armorItem)) continue;
                                        userManager.setItem(user, armorItem, sendPacket);
                                    }
                                }
                        ));
                    }
                    this.getPlayerArmor().clear();
                    new TaskChain(this.plugin).chain(
                                    () -> {
                                        if (!viewer.isOnline()) return;
                                        if (!this.currentLocation.equals(settings.getWardrobeLocation())) return;
                                        this.currentLocation = null;
                                        if (settings.isAlwaysDisplay()) {
                                            this.currentLocation = settings.getWardrobeLocation();
                                            if (this.currentLocation == null) return;
                                            this.spawnFakePlayer(viewer);
                                        }
                                    },
                                    true
                            ).chain(
                                    () -> viewer.teleport(settings.getLeaveLocation())
                            ).
                            execute();
                },
                settings.getDespawnDelay()
        );
    }

    private void startSpinTask(final Player player) {
        final AtomicInteger data = new AtomicInteger();
        final int rotationSpeed = this.plugin.getSettings().getWardrobeSettings().getRotationSpeed();
        final int entityId = this.getEntityId();
        final Task task = new SupplierTask(
                () -> {
                    if (this.currentLocation == null) return;
                    final Location location = this.currentLocation.clone();
                    final int yaw = data.get();
                    location.setYaw(yaw);
                    PacketManager.sendLookPacket(entityId, location, player);
                    this.updateOutsideCosmetics(player, location, this.plugin.getSettings());
                    location.setYaw(this.getNextYaw(yaw - 30, rotationSpeed));
                    PacketManager.sendRotationPacket(entityId, location, true, player);
                    data.set(this.getNextYaw(yaw, rotationSpeed));
                },
                () -> !this.spawned || this.currentLocation == null
        );
        this.plugin.getTaskManager().submit(task);
    }

    private int getNextYaw(final int current, final int rotationSpeed) {
        if (current + rotationSpeed > 179) return -179;
        return current + rotationSpeed;
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
    public @Nullable Vector getVelocity() {
        return new Vector();
    }

    @Override
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(this.ownerUUID);
    }

    private void hidePlayer() {
        Bukkit.getScheduler().runTask(this.plugin,
                () -> {
                    final Player player = this.getPlayer();
                    if (player == null) return;
                    for (final Player p : Bukkit.getOnlinePlayers()) {
                        p.hidePlayer(this.plugin, player);
                        player.hidePlayer(this.plugin, p);
                    }
                });
    }

    private void showPlayer(final UserManager userManager) {
        Bukkit.getScheduler().runTask(
                this.plugin,
                () -> {
                    final Player player = this.getPlayer();
                    if (player == null) return;
                    final Optional<User> optionalUser = userManager.get(player.getUniqueId());
                    for (final Player p : Bukkit.getOnlinePlayers()) {
                        final Optional<User> optional = userManager.get(p.getUniqueId());
                        if (optional.isEmpty()) continue;
                        if (optional.get().getWardrobe().isActive()) continue;
                        player.showPlayer(this.plugin, p);
                        p.showPlayer(this.plugin, player);
                        Bukkit.getScheduler().runTaskLaterAsynchronously(
                                this.plugin,
                                () -> {
//                                    optional.ifPresent(user -> userManager.updateCosmetics(user, player));
//                                    optionalUser.ifPresent(userManager::updateCosmetics);
                                },
                                1
                        );
                    }
                });
    }

    @Override
    public Equipment getEquipment() {
        return new Equipment();
    }
}
