package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Wardrobe;
import com.hibiscusmc.hmccosmetics.config.WardrobeLocation;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.Cosmetic;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.cosmetic.types.CosmeticBalloonType;
import com.hibiscusmc.hmccosmetics.gui.Menu;
import com.hibiscusmc.hmccosmetics.gui.Menus;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.HMCCInventoryUtils;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import lombok.Getter;
import lombok.Setter;
import me.lojosho.hibiscuscommons.nms.NMSHandlers;
import me.lojosho.hibiscuscommons.util.packets.PacketManager;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UserWardrobeManager {

    @Getter private final int NPC_ID;
    @Getter private final int ARMORSTAND_ID;
    @Getter private final UUID WARDROBE_UUID;
    @Getter private String npcName;
    @Getter private GameMode originalGamemode;
    @Getter private final CosmeticUser user;
    @Getter private final Wardrobe wardrobe;
    @Getter private final WardrobeLocation wardrobeLocation;
    @Getter private final Location viewingLocation;
    @Getter private final Location npcLocation;
    @Getter private Location exitLocation;
    @Getter private BossBar bossBar;
    @Getter private boolean active;
    @Setter @Getter private WardrobeStatus wardrobeStatus;
    @Getter @Setter private Menu lastOpenMenu;

    // controls the Folia repeating update loop
    private final AtomicBoolean updateLoopActive = new AtomicBoolean(false);

    public UserWardrobeManager(CosmeticUser user, Wardrobe wardrobe) {
        NPC_ID = me.lojosho.hibiscuscommons.util.ServerUtils.getNextEntityId();
        ARMORSTAND_ID = me.lojosho.hibiscuscommons.util.ServerUtils.getNextEntityId();
        WARDROBE_UUID = UUID.randomUUID();
        this.user = user;

        this.wardrobe = wardrobe;
        this.wardrobeLocation = wardrobe.getLocation();

        this.exitLocation = wardrobeLocation.getLeaveLocation();
        this.viewingLocation = wardrobeLocation.getViewerLocation();
        this.npcLocation = wardrobeLocation.getNpcLocation();

        String defaultMenu = wardrobe.getDefaultMenu();
        if (defaultMenu != null && Menus.hasMenu(defaultMenu)) this.lastOpenMenu = Menus.getMenu(defaultMenu);
        else this.lastOpenMenu = Menus.getDefaultMenu();

        wardrobeStatus = WardrobeStatus.SETUP;
    }

    public void start() {
        setWardrobeStatus(WardrobeStatus.STARTING);
        final Player player = user.getPlayer();
        if (player == null) return;

        this.originalGamemode = player.getGameMode();
        if (WardrobeSettings.isReturnLastLocation()) {
            this.exitLocation = player.getLocation().clone();
        }

        user.hidePlayer();
        if (!Bukkit.getServer().getAllowFlight()) player.setAllowFlight(true);
        final List<Player> viewer = Collections.singletonList(player);

        MessagesUtil.sendMessage(player, "opened-wardrobe");

        Runnable startSequence = () -> {
            if (!player.isOnline()) {
                end();
                return;
            }

            // Armorstand
            HMCCPacketManager.sendEntitySpawnPacket(viewingLocation, ARMORSTAND_ID, EntityType.ARMOR_STAND, UUID.randomUUID(), viewer);
            HMCCPacketManager.sendArmorstandMetadata(ARMORSTAND_ID, viewer);
            NMSHandlers.getHandler().getPacketHandler().sendTeleportPacket(ARMORSTAND_ID,
                    viewingLocation.getX(), viewingLocation.getY(), viewingLocation.getZ(),
                    viewingLocation.getYaw(), viewingLocation.getPitch(), false, viewer);
            HMCCPacketManager.sendRotateHeadPacket(ARMORSTAND_ID, viewingLocation, viewer);

            // Player
            player.teleport(viewingLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setInvisible(true);
            HMCCPacketManager.gamemodeChangePacket(player, GameMode.SPECTATOR);
            HMCCPacketManager.sendCameraPacket(ARMORSTAND_ID, viewer);

            // NPC
            npcName = "WardrobeNPC-" + NPC_ID;
            while (npcName.length() > 16) {
                npcName = npcName.substring(16);
            }
            HMCCPacketManager.sendFakePlayerInfoPacket(player, NPC_ID, WARDROBE_UUID, npcName, viewer);

            // Spawn NPC entity a few ticks later (still on player's entity thread)
            HMCCosmeticsPlugin.getInstance().scheduler().runForLater(player, 4L, () -> {
                if (!user.isInWardrobe()) return; // exited instantly
                HMCCPacketManager.sendFakePlayerSpawnPacket(npcLocation, WARDROBE_UUID, NPC_ID, viewer);
                HMCCPacketManager.sendPlayerOverlayPacket(NPC_ID, viewer);
                MessagesUtil.sendDebugMessages("Spawned Fake Player on " + npcLocation);
                NMSHandlers.getHandler().getPacketHandler().sendScoreboardHideNamePacket(player, npcName);
                AttributeInstance scaleAttribute = user.getPlayer().getAttribute(Attribute.GENERIC_SCALE);
                if (scaleAttribute != null) {
                    HMCCPacketManager.sendEntityScalePacket(NPC_ID, scaleAttribute.getValue(), viewer);
                }
            });

            // Orient NPC
            HMCCPacketManager.sendRotateHeadPacket(NPC_ID, npcLocation, viewer);
            HMCCPacketManager.sendRotationPacket(NPC_ID, npcLocation, true, viewer);

            // Backpack handling in wardrobe
            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                if (user.getUserBackpackManager() == null) user.respawnBackpack();
                if (user.isBackpackSpawned()) {
                    user.getUserBackpackManager().getEntityManager().teleport(npcLocation.clone().add(0, 2, 0));
                    PacketManager.equipmentSlotUpdate(
                            user.getUserBackpackManager().getFirstArmorStandId(),
                            EquipmentSlot.HEAD,
                            user.getUserCosmeticItem(user.getCosmetic(CosmeticSlot.BACKPACK)),
                            viewer
                    );
                    HMCCPacketManager.ridingMountPacket(NPC_ID, user.getUserBackpackManager().getFirstArmorStandId(), viewer);
                }
            }

            // Balloon handling in wardrobe
            if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
                if (user.getBalloonManager() == null) user.respawnBalloon();
                if (user.isBalloonSpawned()) {
                    CosmeticBalloonType cosmetic = (CosmeticBalloonType) user.getCosmetic(CosmeticSlot.BALLOON);
                    user.getBalloonManager().sendRemoveLeashPacket(viewer);
                    user.getBalloonManager().sendLeashPacket(NPC_ID);

                    Location balloonLocation = npcLocation.clone().add(cosmetic.getBalloonOffset());
                    HMCCPacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), balloonLocation, false, viewer);
                    user.getBalloonManager().getModelEntity().teleport(balloonLocation);
                    user.getBalloonManager().setLocation(balloonLocation);
                }
            }

            // Bossbar
            if (WardrobeSettings.isEnabledBossbar()) {
                float progress = WardrobeSettings.getBossbarProgress();
                Component message = MessagesUtil.processStringNoKey(player, WardrobeSettings.getBossbarMessage());

                bossBar = BossBar.bossBar(message, progress, WardrobeSettings.getBossbarColor(), WardrobeSettings.getBossbarOverlay());
                Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);
                target.showBossBar(bossBar);
            }

            // Open default menu on enter
            if (WardrobeSettings.isEnterOpenMenu()) {
                Menu menu = Menus.getDefaultMenu();
                if (menu != null) menu.openMenu(user);
            }

            this.active = true;
            updateLoopActive.set(true);
            scheduleUpdateLoop();
            setWardrobeStatus(WardrobeStatus.RUNNING);
        };

        if (WardrobeSettings.isEnabledTransition()) {
            MessagesUtil.sendTitle(
                    user.getPlayer(),
                    WardrobeSettings.getTransitionText(),
                    WardrobeSettings.getTransitionFadeIn(),
                    WardrobeSettings.getTransitionStay(),
                    WardrobeSettings.getTransitionFadeOut()
            );
            HMCCosmeticsPlugin.getInstance().scheduler().runForLater(player, WardrobeSettings.getTransitionDelay(), startSequence);
        } else {
            HMCCosmeticsPlugin.getInstance().scheduler().runFor(player, startSequence);
        }
    }

    public void end() {
        setWardrobeStatus(WardrobeStatus.STOPPING);
        final Player player = user.getPlayer();
        if (player == null) return;

        final List<Player> viewer = Collections.singletonList(player);

        HMCCosmeticsPlugin.getInstance().scheduler().runFor(player, () -> {
            this.active = false;
            updateLoopActive.set(false);

            if (!Bukkit.getServer().getAllowFlight()) player.setAllowFlight(false);
            MessagesUtil.sendMessage(player, "closed-wardrobe");

            // For Wardrobe Temp Cosmetics
            for (Cosmetic cosmetic : user.getCosmetics()) {
                MessagesUtil.sendDebugMessages("Checking... " + cosmetic.getId());
                if (!user.canEquipCosmetic(cosmetic)) {
                    MessagesUtil.sendDebugMessages("Unable to keep " + cosmetic.getId());
                    user.removeCosmeticSlot(cosmetic.getSlot());
                }
            }

            // NPC cleanup
            if (user.isBalloonSpawned()) user.getBalloonManager().sendRemoveLeashPacket();
            HMCCPacketManager.sendEntityDestroyPacket(NPC_ID, viewer);
            HMCCPacketManager.sendRemovePlayerPacket(player, WARDROBE_UUID, viewer);

            // Return camera and visibility
            HMCCPacketManager.sendCameraPacket(player.getEntityId(), viewer);
            user.getPlayer().setInvisible(false);

            // Armorstand cleanup
            HMCCPacketManager.sendEntityDestroyPacket(ARMORSTAND_ID, viewer);

            // Gamemode restoration
            if (WardrobeSettings.isForceExitGamemode()) {
                MessagesUtil.sendDebugMessages("Force Exit Gamemode " + WardrobeSettings.getExitGamemode());
                player.setGameMode(WardrobeSettings.getExitGamemode());
                HMCCPacketManager.gamemodeChangePacket(player, WardrobeSettings.getExitGamemode());
            } else {
                MessagesUtil.sendDebugMessages("Original Gamemode " + this.originalGamemode);
                player.setGameMode(this.originalGamemode);
                HMCCPacketManager.gamemodeChangePacket(player, this.originalGamemode);
            }

            user.showPlayer();

            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                user.respawnBackpack();
            }

            // Teleport out
            player.teleport(Objects.requireNonNullElseGet(exitLocation, () -> player.getWorld().getSpawnLocation()), PlayerTeleportEvent.TeleportCause.PLUGIN);

            // Restore equipment visuals
            HashMap<EquipmentSlot, ItemStack> items = new HashMap<>();
            for (EquipmentSlot slot : HMCCInventoryUtils.getPlayerArmorSlots()) {
                ItemStack item = player.getInventory().getItem(slot);
                items.put(slot, item);
            }
            HMCCPacketManager.equipmentSlotUpdate(player.getEntityId(), items, viewer);

            // Clear bossbar
            if (WardrobeSettings.isEnabledBossbar()) {
                Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);
                target.hideBossBar(bossBar);
            }

            user.updateCosmetic();
        });
    }

    /**
     * Folia-safe, self-rescheduling update loop bound to the player's entity thread.
     * Replaces the old BukkitRunnable timer (period: 2 ticks).
     */
    private void scheduleUpdateLoop() {
        final Player player = user.getPlayer();
        if (player == null) return;

        HMCCosmeticsPlugin.getInstance().scheduler().runForLater(player, 2L, () -> {
            if (!updateLoopActive.get()) return;

            if (!active || player == null) {
                MessagesUtil.sendDebugMessages("WardrobeEnd[user=" + user.getUniqueId() + ",reason=Active is false]");
                updateLoopActive.set(false);
                return;
            }

            MessagesUtil.sendDebugMessages("WardrobeUpdate[user=" + user.getUniqueId() + ",status=" + getWardrobeStatus() + "]");
            List<Player> viewer = Collections.singletonList(player);
            List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
            outsideViewers.remove(player);

            // We mutate a local copy for yaw to avoid racing across ticks
            Location location = npcLocation.clone();

            // Maintain per-loop yaw state using PDC-like counter kept here
            int yaw = loopYaw.get();
            location.setYaw(yaw);

            HMCCPacketManager.sendRotateHeadPacket(NPC_ID, location, viewer);
            user.hidePlayer();

            int rotationSpeed = WardrobeSettings.getRotationSpeed();
            int newYaw = HMCCServerUtils.getNextYaw(yaw - 30, rotationSpeed);
            location.setYaw(newYaw);
            NMSHandlers.getHandler().getPacketHandler().sendRotationPacket(NPC_ID, newYaw, 0, false, viewer);
            HMCCPacketManager.sendRotationPacket(NPC_ID, newYaw, true, viewer);
            int nextYaw = HMCCServerUtils.getNextYaw(yaw, rotationSpeed);
            loopYaw.set(nextYaw);

            for (CosmeticSlot slot : CosmeticSlot.values().values()) {
                HMCCPacketManager.equipmentSlotUpdate(NPC_ID, user, slot, viewer);
            }

            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && user.getUserBackpackManager() != null) {
                HMCCPacketManager.sendTeleportPacket(user.getUserBackpackManager().getFirstArmorStandId(), location, false, viewer);
                HMCCPacketManager.ridingMountPacket(NPC_ID, user.getUserBackpackManager().getFirstArmorStandId(), viewer);
                user.getUserBackpackManager().getEntityManager().setRotation(nextYaw);
                HMCCPacketManager.sendEntityDestroyPacket(user.getUserBackpackManager().getFirstArmorStandId(), outsideViewers);
            }

            if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON) && user.isBalloonSpawned()) {
                user.getBalloonManager().sendRemoveLeashPacket(outsideViewers);
                if (user.getBalloonManager().getBalloonType() != UserBalloonManager.BalloonType.MODELENGINE) {
                    HMCCPacketManager.sendEntityDestroyPacket(user.getBalloonManager().getModelId(), outsideViewers);
                }
                user.getBalloonManager().sendLeashPacket(NPC_ID);
            }

            if (WardrobeSettings.isEquipPumpkin()) {
                PacketManager.equipmentSlotUpdate(user.getPlayer().getEntityId(), EquipmentSlot.HEAD, new ItemStack(Material.CARVED_PUMPKIN), viewer);
            } else {
                HMCCPacketManager.equipmentSlotUpdate(user.getPlayer(), true, viewer); // Optifine compatibility
            }

            // re-schedule next tick slice
            scheduleUpdateLoop();
        });
    }

    // yaw storage for the update loop (replaces local int in the old BukkitRunnable)
    private final AtomicInteger loopYaw = new AtomicInteger(0);

    public enum WardrobeStatus {
        SETUP,
        STARTING,
        RUNNING,
        STOPPING,
    }
}
