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
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UserWardrobeManager {

    @Getter
    private final int NPC_ID;
    @Getter
    private final int ARMORSTAND_ID;
    @Getter
    private final UUID WARDROBE_UUID;
    @Getter
    private String npcName;
    @Getter
    private GameMode originalGamemode;
    @Getter
    private final CosmeticUser user;
    @Getter
    private final Wardrobe wardrobe;
    @Getter
    private final WardrobeLocation wardrobeLocation;
    @Getter
    private final Location viewingLocation;
    @Getter
    private final Location npcLocation;
    @Getter
    private Location exitLocation;
    @Getter
    private BossBar bossBar;
    @Getter
    private boolean active;
    @Setter
    @Getter
    private WardrobeStatus wardrobeStatus;
    @Getter
    @Setter
    private Menu lastOpenMenu;

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
        Player player = user.getPlayer();

        this.originalGamemode = player.getGameMode();
        if (WardrobeSettings.isReturnLastLocation()) {
            this.exitLocation = player.getLocation().clone();
        }

        user.hidePlayer();
        if (!Bukkit.getServer().getAllowFlight()) player.setAllowFlight(true);
        List<Player> viewer = Collections.singletonList(player);
        List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
        outsideViewers.remove(player);

        MessagesUtil.sendMessage(player, "opened-wardrobe");

        Runnable run = () -> {
            if (!player.isOnline()) {
                end();
                return;
            }

            // Armorstand
            HMCCPacketManager.sendEntitySpawnPacket(viewingLocation, ARMORSTAND_ID, EntityType.ARMOR_STAND, UUID.randomUUID(), viewer);
            HMCCPacketManager.sendArmorstandMetadata(ARMORSTAND_ID, viewer);
            HMCCPacketManager.sendLookPacket(ARMORSTAND_ID, viewingLocation, viewer);

            // Player
            player.teleport(viewingLocation, PlayerTeleportEvent.TeleportCause.PLUGIN);
            player.setInvisible(true);
            HMCCPacketManager.gamemodeChangePacket(player, 3);
            HMCCPacketManager.sendCameraPacket(ARMORSTAND_ID, viewer);

            // NPC
            npcName = "WardrobeNPC-" + NPC_ID;
            while (npcName.length() > 16) {
                npcName = npcName.substring(16);
            }
            HMCCPacketManager.sendFakePlayerInfoPacket(player, NPC_ID, WARDROBE_UUID, npcName, viewer);

            // NPC 2
            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
                if (!user.isInWardrobe()) return; // If a player exits the wardrobe right away, no need to spawn the NPC
                HMCCPacketManager.sendFakePlayerSpawnPacket(npcLocation, WARDROBE_UUID, NPC_ID, viewer);
                HMCCPacketManager.sendPlayerOverlayPacket(NPC_ID, viewer);
                MessagesUtil.sendDebugMessages("Spawned Fake Player on " + npcLocation);
                NMSHandlers.getHandler().getPacketHandler().sendScoreboardHideNamePacket(player, npcName);
            }, 4);

            // Location
            HMCCPacketManager.sendLookPacket(NPC_ID, npcLocation, viewer);
            HMCCPacketManager.sendRotationPacket(NPC_ID, npcLocation, true, viewer);

            // Misc
            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                // Maybe null as backpack maybe despawned before entering
                if (user.getUserBackpackManager() == null) user.respawnBackpack();
                if (user.isBackpackSpawned()) {
                    user.getUserBackpackManager().getEntityManager().teleport(npcLocation.clone().add(0, 2, 0));
                    PacketManager.equipmentSlotUpdate(user.getUserBackpackManager().getFirstArmorStandId(), EquipmentSlot.HEAD, user.getUserCosmeticItem(user.getCosmetic(CosmeticSlot.BACKPACK)), viewer);
                    HMCCPacketManager.ridingMountPacket(NPC_ID, user.getUserBackpackManager().getFirstArmorStandId(), viewer);
                }
            }

            if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
                if (user.getBalloonManager() == null) user.respawnBalloon();
                if (user.isBalloonSpawned()) {
                    CosmeticBalloonType cosmetic = (CosmeticBalloonType) user.getCosmetic(CosmeticSlot.BALLOON);
                    user.getBalloonManager().sendRemoveLeashPacket(viewer);
                    user.getBalloonManager().sendLeashPacket(NPC_ID);
                    //PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getModelId(), NPC_ID, viewer);

                    Location balloonLocation = npcLocation.clone().add(cosmetic.getBalloonOffset());
                    HMCCPacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), balloonLocation, false, viewer);
                    user.getBalloonManager().getModelEntity().teleport(balloonLocation);
                    user.getBalloonManager().setLocation(balloonLocation);
                }
            }

            if (WardrobeSettings.isEnabledBossbar()) {
                float progress = WardrobeSettings.getBossbarProgress();
                Component message = MessagesUtil.processStringNoKey(player, WardrobeSettings.getBossbarMessage());

                bossBar = BossBar.bossBar(message, progress, WardrobeSettings.getBossbarColor(), WardrobeSettings.getBossbarOverlay());
                Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

                target.showBossBar(bossBar);
            }

            if (WardrobeSettings.isEnterOpenMenu()) {
                Menu menu = Menus.getDefaultMenu();
                if (menu != null) menu.openMenu(user);
            }

            this.active = true;
            update();
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
            Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), run, WardrobeSettings.getTransitionDelay());
        } else {
            run.run();
        }

    }

    public void end() {
        setWardrobeStatus(WardrobeStatus.STOPPING);
        Player player = user.getPlayer();

        List<Player> viewer = Collections.singletonList(player);
        List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
        outsideViewers.remove(player);

        if (player == null) return;
        if (!Bukkit.getServer().getAllowFlight()) player.setAllowFlight(false);
        MessagesUtil.sendMessage(player, "closed-wardrobe");

        Runnable run = () -> {
            this.active = false;

            // For Wardrobe Temp Cosmetics
            for (Cosmetic cosmetic : user.getCosmetics()) {
                MessagesUtil.sendDebugMessages("Checking... " + cosmetic.getId());
                if (!user.canEquipCosmetic(cosmetic)) {
                    MessagesUtil.sendDebugMessages("Unable to keep " + cosmetic.getId());
                    user.removeCosmeticSlot(cosmetic.getSlot());
                }
            }

            // NPC
            if (user.isBalloonSpawned()) user.getBalloonManager().sendRemoveLeashPacket();
            HMCCPacketManager.sendEntityDestroyPacket(NPC_ID, viewer); // Success
            HMCCPacketManager.sendRemovePlayerPacket(player, WARDROBE_UUID, viewer); // Success

            // Player
            HMCCPacketManager.sendCameraPacket(player.getEntityId(), viewer);
            user.getPlayer().setInvisible(false);

            // Armorstand
            HMCCPacketManager.sendEntityDestroyPacket(ARMORSTAND_ID, viewer); // Sucess

            //PacketManager.sendEntityDestroyPacket(player.getEntityId(), viewer); // Success
            if (WardrobeSettings.isForceExitGamemode()) {
                MessagesUtil.sendDebugMessages("Force Exit Gamemode " + WardrobeSettings.getExitGamemode());
                player.setGameMode(WardrobeSettings.getExitGamemode());
                HMCCPacketManager.gamemodeChangePacket(player, HMCCServerUtils.convertGamemode(WardrobeSettings.getExitGamemode())); // Success
            } else {
                MessagesUtil.sendDebugMessages("Original Gamemode " + this.originalGamemode);
                player.setGameMode(this.originalGamemode);
                HMCCPacketManager.gamemodeChangePacket(player, HMCCServerUtils.convertGamemode(this.originalGamemode)); // Success
            }
            user.showPlayer();

            if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                user.respawnBackpack();
                //PacketManager.ridingMountPacket(player.getEntityId(), VIEWER.getBackpackEntity().getEntityId(), viewer);
            }

            if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
                //user.respawnBalloon();
                //PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getPufferfishBalloonId(), player.getEntityId(), viewer);
            }

            player.teleport(Objects.requireNonNullElseGet(exitLocation, () -> player.getWorld().getSpawnLocation()), PlayerTeleportEvent.TeleportCause.PLUGIN);

            HashMap<EquipmentSlot, ItemStack> items = new HashMap<>();
            for (EquipmentSlot slot : HMCCInventoryUtils.getPlayerArmorSlots()) {
                ItemStack item = player.getInventory().getItem(slot);
                items.put(slot, item);
            }
            /*
            if (WardrobeSettings.isEquipPumpkin()) {
                items.put(EquipmentSlot.HEAD, player.getInventory().getHelmet());
            }
             */
            HMCCPacketManager.equipmentSlotUpdate(player.getEntityId(), items, viewer);

            if (WardrobeSettings.isEnabledBossbar()) {
                Audience target = BukkitAudiences.create(HMCCosmeticsPlugin.getInstance()).player(player);

                target.hideBossBar(bossBar);
            }

            user.updateCosmetic();
        };
        run.run();
    }

    private void update() {
        final AtomicInteger data = new AtomicInteger();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                Player player = user.getPlayer();
                if (!active || player == null) {
                    MessagesUtil.sendDebugMessages("WardrobeEnd[user=" + user.getUniqueId() + ",reason=Active is false]");
                    this.cancel();
                    return;
                }
                MessagesUtil.sendDebugMessages("WardrobeUpdate[user=" + user.getUniqueId() + ",status=" + getWardrobeStatus() + "]");
                List<Player> viewer = Collections.singletonList(player);
                List<Player> outsideViewers = HMCCPacketManager.getViewers(viewingLocation);
                outsideViewers.remove(player);

                Location location = npcLocation;
                int yaw = data.get();
                location.setYaw(yaw);

                HMCCPacketManager.sendLookPacket(NPC_ID, location, viewer);
                user.hidePlayer();
                int rotationSpeed = WardrobeSettings.getRotationSpeed();
                location.setYaw(HMCCServerUtils.getNextYaw(yaw - 30, rotationSpeed));
                HMCCPacketManager.sendRotationPacket(NPC_ID, location, true, viewer);
                int nextyaw = HMCCServerUtils.getNextYaw(yaw, rotationSpeed);
                data.set(nextyaw);

                for (CosmeticSlot slot : CosmeticSlot.values().values()) {
                    HMCCPacketManager.equipmentSlotUpdate(NPC_ID, user, slot, viewer);
                }

                if (user.hasCosmeticInSlot(CosmeticSlot.BACKPACK) && user.getUserBackpackManager() != null) {
                    HMCCPacketManager.sendTeleportPacket(user.getUserBackpackManager().getFirstArmorStandId(), location, false, viewer);
                    HMCCPacketManager.ridingMountPacket(NPC_ID, user.getUserBackpackManager().getFirstArmorStandId(), viewer);
                    user.getUserBackpackManager().getEntityManager().setRotation(nextyaw);
                    HMCCPacketManager.sendEntityDestroyPacket(user.getUserBackpackManager().getFirstArmorStandId(), outsideViewers);
                }

                if (user.hasCosmeticInSlot(CosmeticSlot.BALLOON) && user.isBalloonSpawned()) {
                    // The two lines below broke, solved by listening to PlayerCosmeticPostEquipEvent
                    //PacketManager.sendTeleportPacket(user.getBalloonManager().getPufferfishBalloonId(), npcLocation.add(Settings.getBalloonOffset()), false, viewer);
                    //user.getBalloonManager().getModelEntity().teleport(npcLocation.add(Settings.getBalloonOffset()));
                    user.getBalloonManager().sendRemoveLeashPacket(outsideViewers);
                    if (user.getBalloonManager().getBalloonType() != UserBalloonManager.BalloonType.MODELENGINE) {
                        HMCCPacketManager.sendEntityDestroyPacket(user.getBalloonManager().getModelId(), outsideViewers);
                    }
                    user.getBalloonManager().sendLeashPacket(NPC_ID);
                }

                if (WardrobeSettings.isEquipPumpkin()) {
                    PacketManager.equipmentSlotUpdate(user.getPlayer().getEntityId(), EquipmentSlot.HEAD, new ItemStack(Material.CARVED_PUMPKIN), viewer);
                } else {
                    HMCCPacketManager.equipmentSlotUpdate(user.getPlayer(), true, viewer); // Optifine dumbassery
                }
            }
        };

        runnable.runTaskTimer(HMCCosmeticsPlugin.getInstance(), 0, 2);
    }

    public enum WardrobeStatus {
        SETUP,
        STARTING,
        RUNNING,
        STOPPING,
    }

}
