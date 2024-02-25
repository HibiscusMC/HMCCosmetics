package com.hibiscusmc.hmccosmetics.user.manager;

import com.hibiscusmc.hmccosmetics.HMCCosmeticsPlugin;
import com.hibiscusmc.hmccosmetics.config.Settings;
import com.hibiscusmc.hmccosmetics.user.CosmeticUser;
import com.hibiscusmc.hmccosmetics.util.HMCCServerUtils;
import com.hibiscusmc.hmccosmetics.util.MessagesUtil;
import com.hibiscusmc.hmccosmetics.util.packets.HMCCPacketManager;
import com.ticxo.playeranimator.api.model.player.PlayerModel;
import me.lojosho.hibiscuscommons.util.ServerUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class UserEmoteModel extends PlayerModel {

    private final CosmeticUser user;
    private String emotePlaying;
    private final int armorStandId;
    private GameMode originalGamemode;

    public UserEmoteModel(@NotNull CosmeticUser user) {
        super(user.getPlayer());
        this.user = user;
        armorStandId = ServerUtils.getNextEntityId();
        getRangeManager().setRenderDistance(Settings.getViewDistance());
    }

    @Override
    public void playAnimation(@NotNull String id) {
        super.playAnimation(id);

        emotePlaying = id;

        // Add config option that either allows player to move or forces them into a spot.
        Player player = user.getPlayer();
        List<Player> viewer = Collections.singletonList(user.getPlayer());
        List<Player> outsideViewers = HMCCPacketManager.getViewers(player.getLocation());
        // Send equipment packet to the player as well (Fixes Optifine still rendering armor when emoting)
        HMCCPacketManager.equipmentSlotUpdate(player, true, outsideViewers);
        outsideViewers.remove(player);

        user.getPlayer().setInvisible(true);
        user.hideCosmetics(CosmeticUser.HiddenReason.EMOTE);

        originalGamemode = player.getGameMode();

        if (Settings.isEmoteCameraEnabled()) {
            Location newLocation = player.getLocation().clone();
            newLocation.setPitch(0);

            double DISTANCE = Settings.getEmoteDistance();

            Location thirdPersonLocation = newLocation.add(newLocation.getDirection().normalize().multiply(DISTANCE));
            if (DISTANCE > 0) {
                MessagesUtil.sendDebugMessages("Yaw " + (int) thirdPersonLocation.getYaw());
                MessagesUtil.sendDebugMessages("New Yaw " + ServerUtils.getNextYaw((int) thirdPersonLocation.getYaw(), 180));
                thirdPersonLocation.setYaw(ServerUtils.getNextYaw((int) thirdPersonLocation.getYaw(), 180));
            }
            if (Settings.isCosmeticEmoteBlockCheck() && thirdPersonLocation.getBlock().getType().isOccluding()) {
                stopAnimation();
                MessagesUtil.sendMessage(player, "emote-blocked");
                return;
            }
            // Check if block below player is an air block
            if (Settings.isEmoteAirCheck() && newLocation.clone().subtract(0, 1, 0).getBlock().getType().isAir()) {
                stopAnimation();
                MessagesUtil.sendMessage(player, "emote-blocked");
            }

            HMCCPacketManager.sendEntitySpawnPacket(thirdPersonLocation, armorStandId, EntityType.ARMOR_STAND, UUID.randomUUID(), viewer);
            HMCCPacketManager.sendInvisibilityPacket(armorStandId, viewer);
            HMCCPacketManager.sendLookPacket(armorStandId, thirdPersonLocation, viewer);

            HMCCPacketManager.gamemodeChangePacket(player, 3);
            HMCCPacketManager.sendCameraPacket(armorStandId, viewer);
        }


        MessagesUtil.sendDebugMessages("playAnimation run");
    }

    @Override
    public boolean update() {
        if (super.getAnimationProperty() == null) {
            stopAnimation();
            return false;
        }
        boolean update = (super.update() && isPlayingAnimation());
        if (!update) {
            stopAnimation();
        }
        return update;
    }

    public void stopAnimation() {
        emotePlaying = null;
        despawn();
        Bukkit.getScheduler().runTask(HMCCosmeticsPlugin.getInstance(), () -> {
            Player player = user.getPlayer();
            if (player == null) return;

            List<Player> viewer = Collections.singletonList(player);
            List<Player> outsideViewers = HMCCPacketManager.getViewers(player.getLocation());
            // Send Equipment packet to all (Fixes Optifine Issue)
            HMCCPacketManager.equipmentSlotUpdate(player, false, outsideViewers);
            outsideViewers.remove(player);

            int entityId = player.getEntityId();
            HMCCPacketManager.sendCameraPacket(entityId, viewer);
            HMCCPacketManager.sendEntityDestroyPacket(armorStandId, viewer);
            if (this.originalGamemode != null) {
                HMCCPacketManager.gamemodeChangePacket(player, HMCCServerUtils.convertGamemode(this.originalGamemode));
                player.setGameMode(this.originalGamemode);
            }

            if (user.getPlayer() != null) player.setInvisible(false);
            user.getUserEmoteManager().despawnTextEntity();
            user.showPlayer();
            user.showCosmetics(CosmeticUser.HiddenReason.EMOTE);
        });
    }

    public boolean isPlayingAnimation() {
        return emotePlaying != null;
    }
}
