package com.hibiscusmc.hmccosmetics.user;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.config.WardrobeSettings;
import com.hibiscusmc.hmccosmetics.cosmetic.CosmeticSlot;
import com.hibiscusmc.hmccosmetics.nms.NMSHandlers;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.ServerUtils;
import com.hibiscusmc.hmccosmetics.util.packets.PacketManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Wardrobe {

    private int NPC_ID;
    private UUID WARDROBE_UUID;
    private int ARMORSTAND_ID;
    private GameMode originalGamemode;
    private CosmeticUser VIEWER;
    private Location viewingLocation;
    private Location npcLocation;
    private Location exitLocation;
    private boolean active;

    public Wardrobe(CosmeticUser user) {
        NPC_ID = NMSHandlers.getHandler().getNextEntityId();
        ARMORSTAND_ID = NMSHandlers.getHandler().getNextEntityId();
        WARDROBE_UUID = UUID.randomUUID();
        VIEWER = user;
    }

    public void start() {
        Player player = VIEWER.getPlayer();
        MessagesUtil.sendDebugMessages("start");
        MessagesUtil.sendDebugMessages("NPC ID " + NPC_ID);
        MessagesUtil.sendDebugMessages("armorstand id " + ARMORSTAND_ID);

        this.originalGamemode = player.getGameMode();
        if (WardrobeSettings.isReturnLastLocation()) {
            this.exitLocation = player.getLocation().clone();
        } else {
            this.exitLocation = WardrobeSettings.getLeaveLocation();
        }

        VIEWER.hidePlayer();
        List<Player> viewer = List.of(player);
        // Armorstand
        PacketManager.sendEntitySpawnPacket(WardrobeSettings.getViewerLocation(), ARMORSTAND_ID, EntityType.ARMOR_STAND, UUID.randomUUID(), viewer);
        PacketManager.sendInvisibilityPacket(ARMORSTAND_ID, viewer);
        PacketManager.sendLookPacket(ARMORSTAND_ID, WardrobeSettings.getViewerLocation(), viewer);

        // Player
        PacketManager.gamemodeChangePacket(player, 3);
        PacketManager.sendCameraPacket(ARMORSTAND_ID, viewer);

        // NPC
        PacketManager.sendFakePlayerInfoPacket(player, NPC_ID, WARDROBE_UUID, viewer);

        // NPC 2
        Bukkit.getScheduler().runTaskLater(HMCCosmeticsPlugin.getInstance(), () -> {
            PacketManager.sendFakePlayerSpawnPacket(WardrobeSettings.getWardrobeLocation(), WARDROBE_UUID, NPC_ID, viewer);
            MessagesUtil.sendDebugMessages("Spawned Fake Player on " + WardrobeSettings.getWardrobeLocation());
        }, 4);


        // Location
        PacketManager.sendLookPacket(NPC_ID, WardrobeSettings.getWardrobeLocation(), viewer);
        PacketManager.sendRotationPacket(NPC_ID, WardrobeSettings.getWardrobeLocation(), true, viewer);

        // Misc

        if (VIEWER.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            PacketManager.ridingMountPacket(NPC_ID, VIEWER.getBackpackEntity().getEntityId(), viewer);
        }

        if (VIEWER.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getPufferfishBalloonId(), -1, viewer);
            PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getPufferfishBalloonId(), NPC_ID, viewer); // This needs a possible fix
            PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getModelId(), NPC_ID, viewer);

            PacketManager.sendTeleportPacket(VIEWER.getBalloonEntity().getPufferfishBalloonId(), WardrobeSettings.getWardrobeLocation(), false, viewer);
            PacketManager.sendTeleportPacket(VIEWER.getBalloonEntity().getModelId(), WardrobeSettings.getWardrobeLocation().add(Settings.getBalloonOffset()), false, viewer);

        }

        MessagesUtil.sendMessage(player, "opened-wardrobe");
        this.active = true;
        update();
    }

    public void end() {
        this.active = false;

        Player player = VIEWER.getPlayer();
        MessagesUtil.sendDebugMessages("end");
        MessagesUtil.sendDebugMessages("NPC ID " + NPC_ID);
        MessagesUtil.sendDebugMessages("armorstand id " + ARMORSTAND_ID);

        List<Player> viewer = List.of(player);

        // NPC
        if (VIEWER.hasCosmeticInSlot(CosmeticSlot.BALLOON)) PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getModelId(), -1, viewer);
        PacketManager.sendEntityDestroyPacket(NPC_ID, viewer); // Success
        PacketManager.sendRemovePlayerPacket(player, player.getUniqueId(), viewer); // Success

        // Player
        PacketManager.sendCameraPacket(player.getEntityId(), viewer);
        PacketManager.gamemodeChangePacket(player, ServerUtils.convertGamemode(this.originalGamemode)); // Success

        // Armorstand
        PacketManager.sendEntityDestroyPacket(ARMORSTAND_ID, viewer); // Sucess

        //PacketManager.sendEntityDestroyPacket(player.getEntityId(), viewer); // Success
        player.setGameMode(this.originalGamemode);
        VIEWER.showPlayer();

        if (VIEWER.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
            PacketManager.ridingMountPacket(player.getEntityId(), VIEWER.getBackpackEntity().getEntityId(), viewer);
        }

        if (VIEWER.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
            PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getPufferfishBalloonId(), player.getEntityId(), viewer);
        }

        if (exitLocation == null) {
            player.teleport(player.getWorld().getSpawnLocation());
        } else {
            player.teleport(exitLocation);
        }
        if (!player.isOnline()) return;
        VIEWER.updateCosmetic();
        MessagesUtil.sendMessage(player, "closed-wardrobe");
    }

    public void update() {
        final AtomicInteger data = new AtomicInteger();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (active == false) {
                    MessagesUtil.sendDebugMessages("Active is false");
                    this.cancel();
                    return;
                }
                MessagesUtil.sendDebugMessages("Update ");
                List<Player> viewer = List.of(VIEWER.getPlayer());

                Location location = WardrobeSettings.getWardrobeLocation().clone();
                int yaw = data.get();
                location.setYaw(yaw);

                PacketManager.sendLookPacket(NPC_ID, location, viewer);
                VIEWER.updateCosmetic();
                int rotationSpeed = WardrobeSettings.getRotationSpeed();
                location.setYaw(getNextYaw(yaw - 30, rotationSpeed));
                PacketManager.sendRotationPacket(NPC_ID, location, true, viewer);
                int nextyaw = getNextYaw(yaw, rotationSpeed);
                data.set(nextyaw);

                for (CosmeticSlot slot : CosmeticSlot.values()) {
                    PacketManager.equipmentSlotUpdate(NPC_ID, VIEWER, slot, viewer);
                }

                if (VIEWER.hasCosmeticInSlot(CosmeticSlot.BACKPACK)) {
                    PacketManager.sendTeleportPacket(VIEWER.getArmorstandId(), location, false, viewer);
                    PacketManager.ridingMountPacket(NPC_ID, VIEWER.getBackpackEntity().getEntityId(), viewer);
                    VIEWER.getBackpackEntity().setRotation(nextyaw, 0);
                }

                if (VIEWER.hasCosmeticInSlot(CosmeticSlot.BALLOON)) {
                    PacketManager.sendTeleportPacket(VIEWER.getBalloonEntity().getPufferfishBalloonId(), WardrobeSettings.getWardrobeLocation(), false, viewer);
                    VIEWER.getBalloonEntity().getModelEntity().teleport(WardrobeSettings.getWardrobeLocation().add(Settings.getBalloonOffset()));
                    //PacketManager.sendLeashPacket(VIEWER.getBalloonEntity().getModelId(), NPC_ID, viewer); // Pufferfish goes away for some reason?
                }
            }
        };

        runnable.runTaskTimer(HMCCosmeticsPlugin.getInstance(), 0, 2);
    }

    private static int getNextYaw(final int current, final int rotationSpeed) {
        int nextYaw = current + rotationSpeed;
        if (nextYaw > 179) {
            nextYaw = (current + rotationSpeed) - 358;
            return nextYaw;
        }
        return nextYaw;
    }

    public int getArmorstandId() {
        return ARMORSTAND_ID;
    }
}
